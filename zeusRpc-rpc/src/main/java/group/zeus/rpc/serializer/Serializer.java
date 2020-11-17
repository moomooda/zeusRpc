package group.zeus.rpc.serializer;

import group.zeus.spi.SPI;

/**
 * @Author: maodazhan
 * @Date: 2020/11/1 21:24
 */
@SPI("protostuff")
public interface Serializer {

    <T> byte[] serialize(T obj);

    <T> Object deserialize(byte[] bytes, Class<T> clazz);
}
