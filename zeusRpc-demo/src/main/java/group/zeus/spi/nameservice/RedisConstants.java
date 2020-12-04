package group.zeus.spi.nameservice;

/**
 * @Author: maodazhan
 * @Date: 2020/11/21 13:35
 */
public interface RedisConstants {

    int MAX_ACTIVE = 1024;
    int MAX_IDLE = 200;
    int MAX_WAIT = 10000;
    int TIMEOUT = 10000;
    boolean TEST_ON_BORROW = true;

    String REDIS_RPC_KEY = "registry";
}
