try {
    rs.status();
    print("Replica set for shard 2 already initialized");
} catch (e) {
    rs.initiate({
        _id: process.env.SHARD2_RS_NAME,
        members: [
            { _id: 0, host: `shard2a:${process.env.SHARD2_A_PORT}` },
            { _id: 1, host: `shard2b:${process.env.SHARD2_B_PORT}` },
            { _id: 2, host: `shard2c:${process.env.SHARD2_C_PORT}` }
        ]
    });
}