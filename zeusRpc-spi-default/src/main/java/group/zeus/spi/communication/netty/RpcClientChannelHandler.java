package group.zeus.spi.communication.netty;

import group.zeus.rpc.core.RpcFuture;
import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.dto.RpcRequest;
import group.zeus.rpc.dto.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: maodazhan
 * @Date: 2020/11/4 22:02
 */
public class RpcClientChannelHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(RpcClientChannelHandler.class);

    private ConcurrentMap<String, RpcFuture> pendingRpc = new ConcurrentHashMap<>(32);
    private volatile Channel channel;
    private SocketAddress remotePeer;
    private int expire = 0;

    private Map<RpcProtocol, RpcClientChannelHandler> VALID_CONNECT_NODES;
    private RpcProtocol rpcProtocol;

    public RpcClientChannelHandler(Map<RpcProtocol, RpcClientChannelHandler> VALID_CONNECT_NODES, RpcProtocol rpcProtocol){
        this.VALID_CONNECT_NODES = VALID_CONNECT_NODES;
        this.rpcProtocol = rpcProtocol;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
        // channel重新开始活跃，置0
        expire = 0;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        String requestId = msg.getRequestId();
        logger.debug("Receive response: " + requestId);
        RpcFuture rpcFuture = pendingRpc.get(requestId);
        if (rpcFuture!=null){
            pendingRpc.remove(requestId);
            rpcFuture.done(msg);
        } else{
            logger.warn("Can not get pending response for request id: " + requestId);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            expire += Beats.BEAT_INTERVAL;
            // 空闲时间超过 10 * BRAT_TIMEOUT
            if (expire > Beats.BEAT_EXPIRITION) {
                ctx.channel().close();
                VALID_CONNECT_NODES.remove(rpcProtocol);
                logger.info("Idle connection on rpcProtocol: {}:{} removed from connection pool", rpcProtocol.getHost(), rpcProtocol.getPort());
            }
            else {
                // Send ping
                handle(Beats.BEAT_PING);
                logger.debug("Client send beat-ping to " + remotePeer);
            }
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }

    public RpcFuture handle(RpcRequest request){
        RpcFuture rpcFuture = new RpcFuture(request);
        pendingRpc.put(request.getRequestId(), rpcFuture);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()){
                    logger.error("Send request {} error", request.getRequestId());
                    channel.close();
                    VALID_CONNECT_NODES.remove(rpcProtocol);
                }
            }
        });
        return rpcFuture;
    }

    /*write empty byteBuf 触发close事件*/
    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
