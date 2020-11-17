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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
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
    private RpcProtocol rpcProtocol;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
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
        super.userEventTriggered(ctx, evt);
    }

    public RpcFuture handle(RpcRequest request) throws Exception {
        RpcFuture rpcFuture = new RpcFuture(request);
        pendingRpc.put(request.getRequestId(), rpcFuture);
        try{
            // FIXME 如果是连续两个RpcEncoder，显然编码会错误，可是channelFuture是success
            ChannelFuture channelFuture = channel.writeAndFlush(request).sync();
            if(!channelFuture.isSuccess()){
                logger.error("Send request {} error", request.getRequestId());
            }
        }catch (InterruptedException ex){
            logger.error("Send request exception: " + ex.getMessage());
        }
        return rpcFuture;
    }

    /*write empty byteBuf 触发close事件*/
    public void close(){
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
