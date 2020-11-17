package group.zeus.rpc.core;

import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.spi.SPI;

import java.util.List;

/**
 * 负载均衡策略
 * @Author: maodazhan
 * @Date: 2020/10/30 10:01
 */
@SPI("consistentHash")
public interface ILoadBalance {

    /**
     * 负载均衡
     * @param serviceKey 接口名+"#"+版本号
     * @param rpcProtocols 同一个serviceKey对应的服务地址
     * @return 负载均衡完返回选择的服务地址
     */
    RpcProtocol route(String serviceKey, List<RpcProtocol> rpcProtocols);
}
