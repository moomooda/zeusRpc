package group.zeus.spi.nameservice;

import group.zeus.rpc.core.INameService;
import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.dto.RpcServiceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: maodazhan
 * @Date: 2020/11/17 22:23
 */
public class RedisImpl implements INameService {

    private static final List<RpcProtocol> RPC_PROTOCOLS = new ArrayList<>();
    private boolean registry = false;

    @Override
    public void register(String registryAddress, String serviceAddress, Map<String, Object> serviceMap) {
        registry = true;
        init(registryAddress);
        List<RpcServiceInfo> serviceInfoList = NameServiceUtils.getRpcServiceInfos(serviceMap);
        RpcProtocol rpcProtocol = NameServiceUtils.getRpcProtocol(serviceAddress,serviceInfoList);
        RedisOperator.getJedis().lpush(RedisConstants.REDIS_RPC_KEY, rpcProtocol.toJson());
    }

    @Override
    public void discover(String discoverAddress) {
        init(discoverAddress);
        List<String> serviceInfos = RedisOperator.getJedis().lrange(RedisConstants.REDIS_RPC_KEY, 0, -1);
        serviceInfos.forEach((serviceInfo) ->{
            RpcProtocol rpcProtocol = RpcProtocol.fromJson(serviceInfo);
            RPC_PROTOCOLS.add(rpcProtocol);
        });
    }

    @Override
    public List<RpcProtocol> getNewestProtocols() {
        return RPC_PROTOCOLS;
    }

    @Override
    public void stop() {
        if (RedisOperator.getJedis()!=null){
            if (registry)
                RedisOperator.getJedis().del(RedisConstants.REDIS_RPC_KEY);
            RedisOperator.returnResource(RedisOperator.getJedis());
        }
    }

    protected void init(String registryAddress){
        RedisOperator.init(registryAddress);
    }
}
