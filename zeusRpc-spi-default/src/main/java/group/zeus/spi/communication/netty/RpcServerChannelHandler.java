package group.zeus.spi.communication.netty;

import group.zeus.rpc.dto.RpcRequest;
import group.zeus.rpc.dto.RpcResponse;
import group.zeus.rpc.util.ServiceUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: maodazhan
 * @Date: 2020/11/8 11:30
 */
public class RpcServerChannelHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerChannelHandler.class);

    private final ThreadPoolExecutor serverHandlerPool;
    private final Map<String, Object> handleMap;

    public RpcServerChannelHandler(ThreadPoolExecutor threadPoolExecutor, Map<String, Object> handleMap){
        this.serverHandlerPool = threadPoolExecutor;
        this.handleMap = handleMap;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            // 直接关掉
            ctx.channel().close();
            logger.warn("Channel idle last {} seconds, close it", Beats.BEAT_TIMEOUT);
        } else{
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        // filter beat ping
        if (Beats.BEAT_ID.equalsIgnoreCase(msg.getRequestId())){
            logger.info("Server read heartbeat ping");
            return;
        }

        serverHandlerPool.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("Receive request " + msg.getRequestId());
                RpcResponse response = new RpcResponse();
                response.setRequestId(msg.getRequestId());
                try{
                    Object result = handle(msg);
                    response.setResult(result);
                } catch (Throwable t){
                    response.setError(t.toString());
                    logger.error("RPC Server handle request error");
                }
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.info("Send response for request " + msg.getRequestId());
                    }
                });
            }
        });
    }

    private Object handle(RpcRequest request) throws Throwable{
        String className = request.getClassName();
        String version = request.getVersion();
        String serviceKey = ServiceUtils.buildServiceKey(className, version);
        Object serviceBean = handleMap.get(serviceKey);
        if (serviceBean == null){
            logger.error("Cannot find service implementation with interface name and version: {}", className, version);
            return null;
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?> [] parameterTypes = request.getParameterTypes();
        Object[]  parameters = request.getParameters();

        logger.debug(serviceClass.getName());
        logger.debug(methodName);
        for (int i=0; i<parameterTypes.length;i++){
            logger.debug(parameterTypes[i].getName());
        }
        for (int i=0; i<parameters.length;i++){
            logger.debug(parameters[i].toString());
        }

        // CGLIB reflect
        FastClass fastClass = FastClass.create(serviceClass);

        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Server caught exception: " + cause.getMessage());
        ctx.close();
    }
}
