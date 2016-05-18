package org.logstash.beats;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

@ChannelHandler.Sharable
public class BeatsHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = Logger.getLogger(Server.class.getName());
    private final MessageListener messageListener;

    public BeatsHandler(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.debug("Received a new payload");

        Message message = (Message) msg;
        this.messageListener.onNewMessage(message);

        if(requireAck(message)) {
            logger.debug("Sending ACK: " + message.getSequence() + " window size: " + message.getPayload().getWindowSize() );
            ByteBuf buffer  = ctx.alloc().buffer(6);
            buffer.writeByte('2');
            buffer.writeByte('A');
            buffer.writeInt(message.getSequence());

            ctx.writeAndFlush(buffer); //TODO should we explicitely flush?
        } else {
            logger.debug("No ACK: " + message.getSequence() + " window size: " + message.getPayload().getWindowSize() );
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private boolean requireAck(Message message){
        if(message.getPayload().getWindowSize() == message.getSequence()) {
            return true;
        }
        return false;
    }
}