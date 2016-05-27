package org.logstash.beats;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;

/**
 * Created by ph on 2016-05-27.
 */
public class AckingStrategyV1  extends AckingStrategy {
    private static Logger logger = Logger.getLogger(Server.class.getName());

    public void ack(Message message, ChannelHandlerContext ctx) {

        if(ackNeeded(message)) {
            ByteBuf buffer = ctx.alloc().buffer(6);
            buffer.writeByte('1');
            buffer.writeByte('A');
            buffer.writeInt(message.getSequence());
            ctx.writeAndFlush(buffer);
            logger.debug("Ack on sequence: " + message.getSequence());
        }
    }

    private boolean ackNeeded(Message message) {
        logger.debug("Ackneeded? Window Size: " + message.getPayload().getWindowSize() + " sequence: " + message.getSequence());

        if(message.getPayload().getWindowSize() == message.getSequence()) {
            return true;
        } else {
            return false;
        }
    }
}
