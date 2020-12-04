package group.zeus.spi.nameservice.zookeeper;

/**
 * @Author: maodazhan
 * @Date: 2020/11/1 11:02
 */
public interface ZkConstants {
    int ZK_SESSION_TIMEOUT = 5000;
    int ZK_CONNECTION_TIMEOUT = 5000;

    String ZK_RPC_PATH ="/registry";
    String ZK_DATA_PATH = ZK_RPC_PATH + "/data";

    String ZK_NAMESPACE = "zeus-rpc";
}
