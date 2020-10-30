package group.zeus.rpc.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: maodazhan
 * @Date: 2020/10/29 20:41
 */
public class ThreadPoolUtils {

    public static ThreadPoolExecutor makeServerThreadPool(int corePoolSize, int maxPoolSize) {
        ThreadPoolExecutor serverHandlerPool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "netty-RPC-" + r.hashCode());
                    }
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        return serverHandlerPool;
    }
}
