try {
    rs.status();
    print("Replica set already initialized");
} catch (e) {
    rs.initiate({
        _id: process.env.CONFIGRS_NAME,
        configsvr: true,
        members: [
            { _id: 0, host: `configsvr1:${process.env.CONFIG1_PORT}` },
            { _id: 1, host: `configsvr2:${process.env.CONFIG2_PORT}` },
            { _id: 2, host: `configsvr3:${process.env.CONFIG3_PORT}` }
        ]
    });
}