#!/usr/bin/env bash
set -euo pipefail

wait_for_mongo() {
  local host="$1"
  local port="$2"
  until mongosh --host "$host" --port "$port" --quiet --eval 'db.adminCommand({ ping: 1 }).ok' >/dev/null 2>&1; do
    sleep 2
  done
}

wait_for_primary() {
  local host="$1"
  local port="$2"
  until [[ "$(mongosh --host "$host" --port "$port" --quiet --eval 'db.hello().isWritablePrimary ? 1 : 0')" == "1" ]]; do
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

mongosh --host configsvr1 --port "$CONFIG1_PORT" --quiet --eval "
rs.initiate({
  _id: \"$CONFIGRS_NAME\",
  configsvr: true,
  members: [
    { _id: 0, host: \"configsvr1:$CONFIG1_PORT\" },
    { _id: 1, host: \"configsvr2:$CONFIG2_PORT\" },
    { _id: 2, host: \"configsvr3:$CONFIG3_PORT\" }
  ]
})
"

mongosh --host shard1a --port "$SHARD1_A_PORT" --quiet --eval "
rs.initiate({
  _id: \"$SHARD1_RS_NAME\",
  members: [
    { _id: 0, host: \"shard1a:$SHARD1_A_PORT\" },
    { _id: 1, host: \"shard1b:$SHARD1_B_PORT\" },
    { _id: 2, host: \"shard1c:$SHARD1_C_PORT\" }
  ]
})
"

mongosh --host shard2a --port "$SHARD2_A_PORT" --quiet --eval "
rs.initiate({
  _id: \"$SHARD2_RS_NAME\",
  members: [
    { _id: 0, host: \"shard2a:$SHARD2_A_PORT\" },
    { _id: 1, host: \"shard2b:$SHARD2_B_PORT\" },
    { _id: 2, host: \"shard2c:$SHARD2_C_PORT\" }
  ]
})
"

wait_for_primary configsvr1 "$CONFIG1_PORT"
wait_for_primary shard1a "$SHARD1_A_PORT"
wait_for_primary shard2a "$SHARD2_A_PORT"
wait_for_mongo mongos "$MONGODB_PORT"

mongosh --host mongos --port "$MONGODB_PORT" --quiet --eval "
sh.addShard('$SHARD1_RS_NAME/shard1a:$SHARD1_A_PORT,shard1b:$SHARD1_B_PORT,shard1c:$SHARD1_C_PORT');
sh.addShard('$SHARD2_RS_NAME/shard2a:$SHARD2_A_PORT,shard2b:$SHARD2_B_PORT,shard2c:$SHARD2_C_PORT');
db.adminCommand({ enableSharding: '$MONGODB_DATABASE' });
db.getSiblingDB('$MONGODB_DATABASE').createCollection('events');
db.adminCommand({
  shardCollection: '$MONGODB_DATABASE.events',
  key: { created_by: 'hashed' }
});
"

mongosh --host mongos --port "$MONGODB_PORT" --quiet --eval "
const appDb = db.getSiblingDB('$MONGODB_DATABASE');

if (appDb.getUser('$MONGODB_USER') == null) {
  appDb.createUser({
    user: '$MONGODB_USER',
    pwd: '$MONGODB_PASSWORD',
    roles: [
      { role: 'readWrite', db: '$MONGODB_DATABASE' },
      { role: 'dbAdmin', db: '$MONGODB_DATABASE' }
    ]
  });
}
"
