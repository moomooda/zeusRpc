package group.zeus.spi.communication.netty;

import group.zeus.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Author: maodazhan
 * @Date: 2020/11/4 21:03
 */
public class RpcEncoder extends MessageToByteEncoder {

    private static final Logger logger = LoggerFactory.getLogger(RpcEncoder.class);
    private Class<?> genericClass;
    private Serializer serializer;

    public RpcEncoder(Class<?> genericClass, Serializer serializer){
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)){
            try {
                byte[] data = serializer.serialize(msg);
                out.writeInt(data.length);
                out.writeBytes(data);
            } catch (Exception ex){
                logger.error("Encoder error: " + ex.toString());
            }
        }
    }
}
