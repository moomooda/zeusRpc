package group.zeus.spi.loadbalance;

import group.zeus.rpc.core.ILoadBalance;
import group.zeus.rpc.dto.RpcProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * FIXME 多个线程同时触发route，线程不安全
 * @Author: maodazhan
 * @Date: 2020/11/17 22:22
 */
public class LruImpl implements ILoadBalance {

    private ConcurrentMap<String, LRUCache> jobLRUMap = new ConcurrentHashMap<>(64);
    private long CACHE_VALID_TIME = 0;

    @Override
    public RpcProtocol route(String serviceKey, List<RpcProtocol> rpcProtocols) {
        if(System.currentTimeMillis() > CACHE_VALID_TIME){
            jobLRUMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 *24;
        }

        // init lru
        LRUCache lruCache = jobLRUMap.get(serviceKey);
        if (lruCache == null){
            lruCache = new LRUCache(16);
        }
        jobLRUMap.putIfAbsent(serviceKey, lruCache);

        // put new
        for (RpcProtocol rpcProtocol: rpcProtocols){
            if (!lruCache.contains(rpcProtocol)){
                lruCache.put(rpcProtocol, rpcProtocol);
            }
        }

        // remove old
        List<RpcProtocol> delLKeys = new ArrayList<>();
        for(RpcProtocol existKey : lruCache.getKeySet()){
            if (!rpcProtocols.contains(existKey)){
                delLKeys.add(existKey);
            }
        }

        if (delLKeys.size() > 0){
            for(RpcProtocol delKey : delLKeys){
                lruCache.remove(delKey);
            }
        }

        // load balance
        RpcProtocol eldestKey = lruCache.getKeySet().iterator().next();
        RpcProtocol eldestValue = lruCache.get(eldestKey);
        return eldestValue;
    }

    private static class LRUCache {

        private Map<RpcProtocol, RpcProtocol> map;
        private LinkedList<RpcProtocol> cache;
        private int cap;

        public LRUCache(int capacity) {
            this.cap = capacity;
            this.map = new HashMap<>();
            this.cache = new LinkedList<>();
        }

        public RpcProtocol get(RpcProtocol key) {
            if (map.containsKey(key)) {
                RpcProtocol value = map.get(key);
                put(key, value);
                return value;
            }
            return null;
        }

        public void put(RpcProtocol key, RpcProtocol value) {
            if (map.containsKey(key)) {
                // 维护链表head是Eldest，tail是Youngest
                // 最近访问过的先remove()再addLast()
                cache.remove(key);
                cache.addLast(key);
                map.put(key, value);
            } else {
                // 空间满，淘汰Youngest Node
                if (cap == map.size()) {
                    RpcProtocol last = cache.removeLast();
                    map.remove(last);
                }
                cache.addLast(key);
                map.put(key, value);
            }
        }

        public boolean contains(RpcProtocol key){
            return map.containsKey(key);
        }

        public Set<RpcProtocol> getKeySet(){
            return map.keySet();
        }

        public void remove(RpcProtocol key){
            if (map.containsKey(key)){
                map.remove(key);
            }
        }
    }
}
