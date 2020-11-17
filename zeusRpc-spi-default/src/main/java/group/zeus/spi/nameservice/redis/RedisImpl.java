package group.zeus.spi.nameservice.redis;

import group.zeus.rpc.core.INameService;
import group.zeus.rpc.dto.RpcProtocol;

import java.util.List;
import java.util.Map;

/**
 * @Author: maodazhan
 * @Date: 2020/11/1 10:13
 */
public class RedisImpl implements INameService {

    private List<RpcProtocol> rpcProtocols;

    @Override
    public void register(String registryAddress, String serviceAddress, Map<String, Object> serviceMap) {

    }

    @Override
    public void discover(String discoverAddress) {

    }

    @Override
    public List<RpcProtocol> getNewestProtocols() {
        // 不更新
        return this.rpcProtocols;
    }

    @Override
    public void stop() {

    }
}
