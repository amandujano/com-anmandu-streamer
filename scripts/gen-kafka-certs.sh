#!/usr/bin/env bash
# Generates a private CA and a server certificate for the public Kafka listener.
#
# Usage: scripts/gen-kafka-certs.sh <kafka-public-hostname>
#
# Output (certs/ is git-ignored, never commit it):
#   certs/ca.crt        public CA certificate -> distribute to Kafka clients (truststore)
#   certs/ca.key        CA private key        -> keep offline, needed to issue new certs
#   certs/server.pem    server key + chain    -> goes to the broker as base64
#   certs/server.pem.b64 single-line base64 of server.pem (value for KAFKA_SERVER_PEM_B64)
set -euo pipefail

HOST="${1:?usage: $0 <kafka-public-hostname>}"
OUT="$(cd "$(dirname "$0")/.." && pwd)/certs"
CA_DAYS=3650
SERVER_DAYS=825

mkdir -p "$OUT"
chmod 700 "$OUT"
cd "$OUT"

# Reuse the CA when it already exists so clients keep trusting new server certs.
if [[ ! -f ca.key || ! -f ca.crt ]]; then
  openssl genrsa -out ca.key 4096
  openssl req -x509 -new -nodes -key ca.key -sha256 -days "$CA_DAYS" \
    -subj "/CN=streamer-dev-kafka-ca" -out ca.crt
  echo "Created new CA (ca.crt)"
else
  echo "Reusing existing CA"
fi

openssl genrsa -out server.key 2048
openssl req -new -key server.key -subj "/CN=${HOST}" -out server.csr

cat > server.ext <<EOF
subjectAltName = DNS:${HOST}
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
EOF

openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out server.crt -days "$SERVER_DAYS" -sha256 -extfile server.ext

# Kafka's PEM keystore needs an unencrypted PKCS#8 key followed by the certificate chain.
openssl pkcs8 -topk8 -nocrypt -in server.key -out server.pkcs8.key
cat server.pkcs8.key server.crt ca.crt > server.pem
base64 < server.pem | tr -d '\n' > server.pem.b64

rm -f server.csr server.ext server.key server.pkcs8.key
chmod 600 ca.key server.pem server.pem.b64

echo
echo "Done. Files in: $OUT"
echo "Server certificate valid for: ${HOST} (${SERVER_DAYS} days)"
