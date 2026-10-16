#!/bin/sh
# Grants a service user read/write access to one topic and read access to one consumer group.
# Runs INSIDE the broker container, through the internal PLAINTEXT listener.
#
# Usage (from a machine with docker access to the broker container):
#   docker exec -i <broker-container> sh -s -- <user> <topic> <group> < scripts/kafka-acls.sh
#
# Example:
#   docker exec -i <broker-container> sh -s -- app test-topic test-group < scripts/kafka-acls.sh
set -eu

USER_NAME="${1:?usage: $0 <user> <topic> <group>}"
TOPIC="${2:?usage: $0 <user> <topic> <group>}"
GROUP="${3:?usage: $0 <user> <topic> <group>}"
BOOTSTRAP="localhost:19092"
ACLS=/opt/kafka/bin/kafka-acls.sh

"$ACLS" --bootstrap-server "$BOOTSTRAP" --add \
  --allow-principal "User:${USER_NAME}" \
  --operation Read --operation Write --operation Describe \
  --topic "$TOPIC"

"$ACLS" --bootstrap-server "$BOOTSTRAP" --add \
  --allow-principal "User:${USER_NAME}" \
  --operation Read \
  --group "$GROUP"

"$ACLS" --bootstrap-server "$BOOTSTRAP" --list --principal "User:${USER_NAME}"
