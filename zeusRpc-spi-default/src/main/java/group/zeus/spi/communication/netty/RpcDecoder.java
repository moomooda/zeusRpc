package group.zeus.spi.communication.netty;

import group.zeus.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author: maodazhan
 * @Date: 2020/11/4 21:03
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(RpcDecoder.class);
    private Class<?> genericClass;
    private Serializer serializer;

    public RpcDecoder(Class<?> genericClass, Serializer serializer){
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes()<4){
            return;
        }
        // 标记方便后续reset
        in.markReaderIndex();
        // data bytes length
        int dataLength =  in.readInt();
        if (in.readableBytes() < dataLength){
            in.resetReaderIndex();
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object obj = null;
        try{
            obj = serializer.deserialize(data, genericClass);
            out.add(obj);
        } catch (Exception ex){
            logger.error("Decode error: " + ex.toString());
        }
    }
}
