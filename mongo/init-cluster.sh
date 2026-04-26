#!/usr/bin/env bash
set -euo pipefail

wait_for_mongo() {
  local host="$1"
  local port="$2"
  until mongosh --host "$host" --port "$port" --quiet --eval 'db.adminCommand({ ping: 1 }).ok' >/dev/null 2>&1; do
    sleep 2
  done
}

wait() {
  local host="$1"
  local port="$2"
  until [[ "$(mongosh --host "$host" --port "$port" --quiet --eval 'rs.status().members.some(m => m.stateStr === "PRIMARY") ? 1 : 0')" == "1" ]]; do
    sleep 2
  done
}

wait_for_mongo configsvr1 "$CONFIG1_PORT"
wait_for_mongo configsvr2 "$CONFIG2_PORT"
wait_for_mongo configsvr3 "$CONFIG3_PORT"
wait_for_mongo shard1a "$SHARD1_A_PORT"
wait_for_mongo shard1b "$SHARD1_B_PORT"
wait_for_mongo shard1c "$SHARD1_C_PORT"
wait_for_mongo shard2a "$SHARD2_A_PORT"
wait_for_mongo shard2b "$SHARD2_B_PORT"
wait_for_mongo shard2c "$SHARD2_C_PORT"

mongosh --host configsvr1 --port "$CONFIG1_PORT" /scripts/init-config-rs.js
mongosh --host shard1a --port "$SHARD1_A_PORT" /scripts/init-shard1-rs.js
mongosh --host shard2a --port "$SHARD2_A_PORT" /scripts/init-shard2-rs.js

wait configsvr1 "$CONFIG1_PORT"
wait shard1a "$SHARD1_A_PORT"
wait shard2a "$SHARD2_A_PORT"
wait_for_mongo mongos "$MONGODB_PORT"

mongosh --host mongos --port "$MONGODB_PORT" /scripts/init-mongo.js
