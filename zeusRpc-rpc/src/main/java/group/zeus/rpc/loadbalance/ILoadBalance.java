package group.zeus.rpc.loadbalance;

import group.zeus.rpc.core.RpcProtocal;
import group.zeus.spi.SPI;

import java.util.List;

/**
 * 负载均衡策略
 * @Author: maodazhan
 * @Date: 2020/10/30 10:01
 */
@SPI("consistent-hash")
public interface ILoadBalance {

    /**
     * 负载均衡
     * @param rpcProtocals 同一个serviceKey对应的服务地址
     * @return 负载均衡完返回选择的服务地址
     */
    RpcProtocal route(List<RpcProtocal> rpcProtocals);
}
