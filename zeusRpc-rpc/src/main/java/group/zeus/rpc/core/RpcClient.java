package group.zeus.rpc.core;

import group.zeus.rpc.consumer.IConsumer;
import group.zeus.rpc.nameservice.INamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: maodazhan
 * @Date: 2020/10/28 16:09
 */
@Component
public class RpcClient {

    /*用于通信连接*/
    @Autowired
    private IConsumer iConsumer;

    /*用于服务发现*/
    @Autowired
    private INamingService iNamingService;

}
