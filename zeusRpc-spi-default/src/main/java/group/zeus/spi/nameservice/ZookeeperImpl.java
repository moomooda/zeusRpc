package group.zeus.spi.nameservice;

import group.zeus.rpc.nameservice.AbstractNamingService;
import group.zeus.rpc.core.RpcProtocal;

import java.util.List;
import java.util.Map;

/**
 * @Author: maodazhan
 * @Date: 2020/10/30 15:53
 */
public class ZookeeperImpl extends AbstractNamingService {

    @Override
    protected List<RpcProtocal> lookup(String serviceKey) {
        return null;
    }

    @Override
    public void register(String providerAddress, Map<String, Object> serviceMap) {

    }
}
