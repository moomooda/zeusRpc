package group.zeus.spi.communication.netty;

import group.zeus.rpc.core.IConsumer;
import group.zeus.rpc.core.RpcFuture;
import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.dto.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Netty实现通信交互，支持心跳发现
 * @Author: maodazhan
 * @Date: 2020/10/28 15:20
 */
public class NettyConsumerClient implements IConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NettyConsumerClient.class);
    /*可以复用的连接缓存*/
    // TODO 可能会有大量的无效Handler存在于Map缓存
    public static final Map<RpcProtocol, RpcClientChannelHandler> VALID_CONNECT_NODES = new ConcurrentHashMap<>(128);
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    public RpcClientChannelHandler doConnect(RpcProtocol rpcProtocol){
        final InetSocketAddress remotePeer = new InetSocketAddress(rpcProtocol.getHost(), rpcProtocol.getPort());
        logger.info("New connect to remotePeer {} has been builded", remotePeer);
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new RpcClientChannelInitializer());
        RpcClientChannelHandler handler = null;
        try {
            ChannelFuture channelFuture = b.connect(remotePeer).sync();
            handler = channelFuture.channel().pipeline().get(RpcClientChannelHandler.class);

        } catch (InterruptedException ex){
            logger.warn("Fail to connect to remote server, remote peer = " + remotePeer);
        }
        return handler;
    }


    @Override
    public void stop(){
        for(RpcProtocol rpcProtocol: VALID_CONNECT_NODES.keySet()){
            RpcClientChannelHandler handler = VALID_CONNECT_NODES.get(rpcProtocol);
            handler.close();
            VALID_CONNECT_NODES.remove(rpcProtocol);
        }
        logger.info("Connection with peer has been closed");
        eventLoopGroup.shutdownGracefully();
    }

    @Override
    public RpcFuture connect(RpcProtocol rpcProtocol, RpcRequest request){
        RpcClientChannelHandler rpcRequestHandler;
        rpcRequestHandler = VALID_CONNECT_NODES.get(rpcProtocol);
        if (rpcRequestHandler == null) {
            rpcRequestHandler = doConnect(rpcProtocol);
            VALID_CONNECT_NODES.putIfAbsent(rpcProtocol, rpcRequestHandler);
        }
        RpcFuture rpcFuture = null;
        try {
            rpcFuture = rpcRequestHandler.handle(request);
        } catch (Exception ex){
            logger.warn("Request error,connection {} : {} removed from cache", rpcProtocol.getHost(), rpcProtocol.getPort());
            // 请求失败，该连接需移除
            rpcRequestHandler.close();
            VALID_CONNECT_NODES.remove(rpcProtocol);
        }
        return rpcFuture;
    }
}
