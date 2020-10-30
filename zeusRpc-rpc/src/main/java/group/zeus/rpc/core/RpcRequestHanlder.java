package group.zeus.rpc.core;

import group.zeus.rpc.dto.RpcRequest;

/**
 * 处理实际调用的Rpc连接
 * @Author: maodazhan
 * @Date: 2020/10/30 16:54
 */
public interface RpcRequestHanlder {

    RpcFuture handle(RpcRequest request);
}
