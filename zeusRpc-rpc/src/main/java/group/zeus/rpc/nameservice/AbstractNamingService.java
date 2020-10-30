package group.zeus.rpc.nameservice;

import group.zeus.rpc.loadbalance.ILoadBalance;
import group.zeus.rpc.core.RpcProtocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 封装负载均衡的调用逻辑
 * @Author: maodazhan
 * @Date: 2020/10/30 15:36
 */
@Component
public abstract class AbstractNamingService implements INamingService{

    // TODO 一个优化点：用于注册中心也会注入这个负载均衡实例
    @Autowired
    private ILoadBalance iLoadBalance;

    /*封装负载均衡的过程*/
    public RpcProtocal discover(String serviceKey){
        List<RpcProtocal> protocals = lookup(serviceKey);
        RpcProtocal rpcProtocal = iLoadBalance.route(protocals);
        return rpcProtocal;
    }

    protected abstract List<RpcProtocal> lookup(String serviceKey);
}
