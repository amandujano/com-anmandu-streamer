#!/usr/bin/env bash
# Grants a service user read/write access to one topic and read access to one consumer group,
# connecting from your machine to the public SASL_SSL listener as the `admin` super user.
# Needs Docker locally (it runs the Kafka CLI from the apache/kafka image).
#
# Usage:
#   KAFKA_ADMIN_PASSWORD='...' scripts/kafka-acls-remote.sh <user> <topic> <group>
#
# Optional environment:
#   KAFKA_BOOTSTRAP   default centauro.anmandu.com:9094
#   DOCKER_EXTRA_ARGS extra `docker run` arguments (e.g. --add-host for local tests)
set -euo pipefail

USER_NAME="${1:?usage: $0 <user> <topic> <group>}"
TOPIC="${2:?usage: $0 <user> <topic> <group>}"
GROUP="${3:?usage: $0 <user> <topic> <group>}"
: "${KAFKA_ADMIN_PASSWORD:?set KAFKA_ADMIN_PASSWORD}"

BOOTSTRAP="${KAFKA_BOOTSTRAP:-centauro.anmandu.com:9094}"
CERTS_DIR="$(cd "$(dirname "$0")/.." && pwd)/certs"
[[ -f "$CERTS_DIR/ca.crt" ]] || { echo "missing $CERTS_DIR/ca.crt (run scripts/gen-kafka-certs.sh)" >&2; exit 1; }

# The password is passed as an environment variable so it never appears in the process list.
# shellcheck disable=SC2086
docker run --rm ${DOCKER_EXTRA_ARGS:-} \
  -e KAFKA_ADMIN_PASSWORD \
  -e BOOTSTRAP="$BOOTSTRAP" -e ACL_USER="$USER_NAME" -e ACL_TOPIC="$TOPIC" -e ACL_GROUP="$GROUP" \
  -v "$CERTS_DIR/ca.crt:/certs/ca.crt:ro" \
  --entrypoint sh apache/kafka:4.3.1 -c '
set -eu
cat > /tmp/admin.properties <<EOF
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="${KAFKA_ADMIN_PASSWORD}";
ssl.truststore.type=PEM
ssl.truststore.location=/certs/ca.crt
EOF
ACLS=/opt/kafka/bin/kafka-acls.sh
"$ACLS" --bootstrap-server "$BOOTSTRAP" --command-config /tmp/admin.properties --add \
  --allow-principal "User:${ACL_USER}" \
  --operation Read --operation Write --operation Describe --topic "$ACL_TOPIC"
"$ACLS" --bootstrap-server "$BOOTSTRAP" --command-config /tmp/admin.properties --add \
  --allow-principal "User:${ACL_USER}" --operation Read --group "$ACL_GROUP"
"$ACLS" --bootstrap-server "$BOOTSTRAP" --command-config /tmp/admin.properties \
  --list --principal "User:${ACL_USER}"
'
