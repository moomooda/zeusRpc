package group.zeus.spi.communication.netty;

import group.zeus.rpc.dto.RpcRequest;

/**
 * 心跳包
 * @Author: maodazhan
 * @Date: 2020/11/1 22:14
 */
public final class Beats {

    public static final int BEAT_INTERVAL = 30;
    public static final int BRAT_TIMEOUT = 3 * BEAT_INTERVAL;
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static RpcRequest BEAT_PING;

    static{
        BEAT_PING = new RpcRequest();
        BEAT_PING.setRequestId(BEAT_ID);
    }
}
