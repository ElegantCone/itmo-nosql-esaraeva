try {
    rs.status();
    print("Replica set for shard 1 already initialized");
} catch (e) {
    rs.initiate({
        _id: process.env.SHARD1_RS_NAME,
        members: [
            { _id: 0, host: `shard1a:${process.env.SHARD1_A_PORT}` },
            { _id: 1, host: `shard1b:${process.env.SHARD1_B_PORT}` },
            { _id: 2, host: `shard1c:${process.env.SHARD1_C_PORT}` }
        ]
    });
}