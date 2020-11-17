package group.zeus.spi.communication.netty;

import group.zeus.rpc.core.IProvider;
import group.zeus.rpc.util.ThreadPoolUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: maodazhan
 * @Date: 2020/10/28 15:08
 */
public class NettyProviderServer implements IProvider {

    private static final Logger logger = LoggerFactory.getLogger(NettyProviderServer.class);
    private Thread thread;

    @Override
    public void start(String serverAddress, Map<String, Object> handleMap){
        thread = new Thread(new Runnable() {
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtils.makeServerThreadPool(16, 32);

            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                            .childHandler(new RpcServerChannelInitializer(threadPoolExecutor, handleMap))
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    ChannelFuture future = bootstrap.bind(host, port).sync();
                    logger.info("Server started on port {}", port);
                    future.channel().closeFuture().sync();
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) {
                        logger.info("Netty Rpc server stop");
                    } else {
                        logger.error("Netty Rpc server error", ex);
                    }
                } finally {
                    try {
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                        threadPoolExecutor.shutdownNow();
                    } catch(Exception ex){
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void stop() {
        //destroy server thread
        if (thread!=null && thread.isAlive()){
            thread.interrupt();
        }
    }

}
