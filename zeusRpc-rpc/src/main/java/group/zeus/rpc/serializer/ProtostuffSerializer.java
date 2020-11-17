package group.zeus.rpc.serializer;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: maodazhan
 * @Date: 2020/11/1 22:09
 */
public class ProtostuffSerializer implements Serializer{
    private Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private Objenesis objenesis = new ObjenesisStd(true);

    private <T> Schema<T> getSchema(Class<T> cls){
        return (Schema<T>) cachedSchema.computeIfAbsent(cls, RuntimeSchema::createFrom);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try{
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        }catch (Exception ex){
            throw new IllegalStateException(ex.getMessage(), ex);
        }finally {
            buffer.clear();
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        try{
            T message =(T) objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(bytes, message, schema);
            return message;
        } catch(Exception ex){
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }
}
