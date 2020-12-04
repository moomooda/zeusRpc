package group.zeus.spi.loadbalance;

import group.zeus.rpc.core.ILoadBalance;
import group.zeus.rpc.dto.RpcProtocol;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FIXME 多个线程同时触发route，线程不安全
 * @Author: maodazhan
 * @Date: 2020/11/22 14:50
 */
public class RoundRobinImpl implements ILoadBalance {

    ConcurrentMap<String, AtomicInteger> jobRoundRobinMap = new ConcurrentHashMap<>(64);

    @Override
    public RpcProtocol route(String serviceKey, List<RpcProtocol> rpcProtocols) {
        if(jobRoundRobinMap.get(serviceKey) == null){
            synchronized (serviceKey){
                if (jobRoundRobinMap.get(serviceKey) == null){
                    AtomicInteger atomicInteger = new AtomicInteger(0);
                    jobRoundRobinMap.put(serviceKey, atomicInteger);
                }
            }
        }
        // Round Robin
        int size = rpcProtocols.size();
        int index = (jobRoundRobinMap.get(serviceKey).getAndIncrement() + size) % size;
        return rpcProtocols.get(index);
    }

}
