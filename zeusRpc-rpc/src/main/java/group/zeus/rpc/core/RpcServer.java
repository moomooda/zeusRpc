package group.zeus.rpc.core;

import group.zeus.rpc.annotation.RpcService;
import group.zeus.rpc.nameservice.INamingService;
import group.zeus.rpc.provider.IProvider;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * @Author: maodazhan
 * @Date: 2020/10/28 16:08
 */
// TODO 这里尽量屏蔽掉Netty的东西
@Component
public class RpcServer implements ApplicationContextAware, InitializingBean, DisposableBean {

    /*用于通信连接*/
    @Autowired
    private IProvider iProvider;

    /*用于服务注册*/
    @Autowired
    private INamingService iNamingService;

    @Override
    public void destroy() throws Exception {
        iProvider.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        iProvider.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        /*注册本地服务到Map中*/
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)){
            serviceBeanMap.forEach((name, serviceBean)-> {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                String interfaceName = rpcService.value().getName();
                String version = rpcService.version();
                iProvider.addService(interfaceName, version, serviceBean);
            });
        }

    }
}
