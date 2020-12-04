package group.zeus.spi.nameservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Author: maodazhan
 * @Date: 2020/11/21 13:32
 */
public class RedisOperator {

    private static Logger logger = LoggerFactory.getLogger(RedisOperator.class);
    private static JedisPool jedisPool = null;

    public static void init(String addr) {
        if (jedisPool == null) {
            synchronized (RedisOperator.class) {
                if (jedisPool == null) {
                    try {
                        String[] split = addr.split(":");
                        String host = split[0];
                        Integer port = Integer.parseInt(split[1]);
                        JedisPoolConfig config = new JedisPoolConfig();
                        config.setMaxTotal(RedisConstants.MAX_ACTIVE);
                        config.setMaxIdle(RedisConstants.MAX_IDLE);
                        config.setMaxWaitMillis(RedisConstants.MAX_WAIT);
                        config.setTestOnBorrow(RedisConstants.TEST_ON_BORROW);
                        jedisPool = new JedisPool(config, host, port, RedisConstants.TIMEOUT);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                throw new RuntimeException("call RedisOparetor.getJedis, connection is closed");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /***
     * 释放资源
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
        }
    }
}
