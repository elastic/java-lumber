package org.logstash.beats;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by ph on 2016-05-30.
 */
public class AckMessageEncoder extends MessageToByteEncoder<AckMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, AckMessage ack, ByteBuf out) throws Exception {
        if(ack.ackNeeded()) {
            ByteBuf buffer = ctx.alloc().buffer(6);
            buffer.writeByte(ack.getProtocol());
            buffer.writeByte('A');
            buffer.writeInt(ack.getSequence());
            ctx.writeAndFlush(buffer);
        }
    }
}
