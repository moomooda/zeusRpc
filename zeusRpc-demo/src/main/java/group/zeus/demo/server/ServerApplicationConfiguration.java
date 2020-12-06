package group.zeus.demo.server;

import group.zeus.rpc.annotation.RpcService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * @Author: maodazhan
 * @Date: 2020/12/6 10:50
 */
@ComponentScan(value = "group.zeus.demo.server", includeFilters = {@ComponentScan.Filter(type =  FilterType.ANNOTATION, classes = {RpcService.class})})
public class ServerApplicationConfiguration {
}
