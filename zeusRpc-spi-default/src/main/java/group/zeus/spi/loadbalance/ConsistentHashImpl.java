package group.zeus.spi.loadbalance;

import com.google.common.hash.Hashing;
import group.zeus.rpc.core.ILoadBalance;
import group.zeus.rpc.dto.RpcProtocol;

import java.util.List;

/**
 * @Author: maodazhan
 * @Date: 2020/10/30 15:53
 */
public class ConsistentHashImpl implements ILoadBalance {

    @Override
    public RpcProtocol route(String serviceKey, List<RpcProtocol> rpcProtocols) {
        // 源码解析 <a href="https://www.jianshu.com/p/5e38e1085d75"/a>
        int index = Hashing.consistentHash(serviceKey.hashCode(), rpcProtocols.size());
        return rpcProtocols.get(index);
    }
}
