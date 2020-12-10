package group.zeus.spi.communication.netty;

import group.zeus.rpc.dto.RpcRequest;
import group.zeus.rpc.dto.RpcResponse;
import group.zeus.rpc.serializer.ProtostuffSerializer;
import group.zeus.rpc.serializer.Serializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: maodazhan
 * @Date: 2020/11/8 11:29
 */
public class RpcServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ThreadPoolExecutor serverHandlerPool;
    private final Map<String, Object> handleMap;

    public RpcServerChannelInitializer(ThreadPoolExecutor threadPoolExecutor, Map<String, Object> handleMap){
        this.serverHandlerPool = threadPoolExecutor;
        this.handleMap = handleMap;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        Serializer serializer = ProtostuffSerializer.class.newInstance();
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new IdleStateHandler(0,0, Beats.BEAT_TIMEOUT, TimeUnit.SECONDS));
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0,0));
        cp.addLast(new RpcDecoder(RpcRequest.class, serializer));
        cp.addLast(new RpcEncoder(RpcResponse.class, serializer));
        cp.addLast(new RpcServerChannelHandler(serverHandlerPool, handleMap));
    }
}
