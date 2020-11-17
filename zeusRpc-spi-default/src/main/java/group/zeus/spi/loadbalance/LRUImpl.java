package group.zeus.spi.loadbalance;

import group.zeus.rpc.core.ILoadBalance;
import group.zeus.rpc.dto.RpcProtocol;

import java.util.List;

/**
 * @Author: maodazhan
 * @Date: 2020/11/12 11:09
 */

public class LRUImpl implements ILoadBalance {

    @Override
    public RpcProtocol route(String serviceKey, List<RpcProtocol> rpcProtocols) {
        return null;
    }
}
