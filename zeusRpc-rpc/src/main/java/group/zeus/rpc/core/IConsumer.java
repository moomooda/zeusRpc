package group.zeus.rpc.core;

import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.dto.RpcRequest;
import group.zeus.spi.SPI;

@SPI("netty")
public interface IConsumer {

    /**
     * 和服务端建立连接
     * @param rpcProtocol 有效地址连接
     * @param request rpc请求
     * @return 返回结果
     */
    RpcFuture connect(RpcProtocol rpcProtocol, RpcRequest request) throws Exception;

    /**
     * 关闭连接
     */
    void stop() throws Exception;
}
