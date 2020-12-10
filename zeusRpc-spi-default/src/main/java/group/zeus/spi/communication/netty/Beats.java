package group.zeus.spi.communication.netty;

import group.zeus.rpc.dto.RpcRequest;

/**
 * 心跳包
 * @Author: maodazhan
 * @Date: 2020/11/1 22:14
 */
public final class Beats {

    // 发送心跳包的时间间隔
    public static final int BEAT_INTERVAL = 30;
    // 服务端空闲时间阈值
    public static final int BEAT_TIMEOUT = 3 * BEAT_INTERVAL;
    // 连续10次发送心跳包
    public static final int BEAT_EXPIRITION = 10 * BEAT_INTERVAL;
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static RpcRequest BEAT_PING;

    static{
        BEAT_PING = new RpcRequest();
        BEAT_PING.setRequestId(BEAT_ID);
    }
}
