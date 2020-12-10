package group.zeus.spi.communication.netty;

import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.serializer.ProtostuffSerializer;
import group.zeus.rpc.serializer.Serializer;
import group.zeus.rpc.dto.RpcRequest;
import group.zeus.rpc.dto.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: maodazhan
 * @Date: 2020/11/1 21:20
 */
public class RpcClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Map<RpcProtocol, RpcClientChannelHandler> VALID_CONNECT_NODES;
    private RpcProtocol rpcProtocol;

    public RpcClientChannelInitializer(Map<RpcProtocol, RpcClientChannelHandler> VALID_CONNECT_NODES, RpcProtocol rpcProtocol){
        this.VALID_CONNECT_NODES = VALID_CONNECT_NODES;
        this.rpcProtocol = rpcProtocol;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        // TODO 改进SPI不能依赖注入的问题，从而可以自定义扩展序列化方式
        Serializer serializer = ProtostuffSerializer.class.newInstance();
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new IdleStateHandler(0, 0, Beats.BEAT_INTERVAL, TimeUnit.SECONDS));
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RpcEncoder(RpcRequest.class,serializer));
        cp.addLast(new RpcDecoder(RpcResponse.class, serializer));
        cp.addLast(new RpcClientChannelHandler(VALID_CONNECT_NODES, rpcProtocol));
    }
}
