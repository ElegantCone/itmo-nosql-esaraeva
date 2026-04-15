const admin = db.getSiblingDB('admin');
appDb = db.getSiblingDB(process.env.MONGODB_DATABASE);

if (appDb.getUser(process.env.MONGODB_USER) == null) {
    appDb.createUser({
        user: process.env.MONGODB_USER,
        pwd: process.env.MONGODB_PASSWORD,
        roles: [
            {
                role: "dbOwner",
                db: process.env.MONGODB_DATABASE
            }
        ]
    });
}

const shards = admin.runCommand({ listShards: 1 }).shards.map(s => s._id);

if (!shards.includes(process.env.SHARD1_RS_NAME)) {
    sh.addShard(
        `${process.env.SHARD1_RS_NAME}/shard1a:${process.env.SHARD1_A_PORT},shard1b:${process.env.SHARD1_B_PORT},shard1c:${process.env.SHARD1_C_PORT}`
    );
}

if (!shards.includes(process.env.SHARD2_RS_NAME)) {
    sh.addShard(
        `${process.env.SHARD2_RS_NAME}/shard2a:${process.env.SHARD2_A_PORT},shard2b:${process.env.SHARD2_B_PORT},shard2c:${process.env.SHARD2_C_PORT}`
    );
}

try {
    admin.runCommand({ enableSharding: process.env.MONGODB_DATABASE });
} catch (e) {
    if (!String(e).includes('already enabled')) throw e;
}

if (appDb.getCollectionInfos({ name: 'events' }).length === 0) {
    appDb.createCollection('events');
}

try {
    admin.runCommand({
        shardCollection: `${process.env.MONGODB_DATABASE}.events`,
        key: { created_by: 'hashed' }
    });
} catch (e) {
    if (!String(e).includes('already sharded')) throw e;
}
