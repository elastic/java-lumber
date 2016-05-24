package org.logstash.beats;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

@ChannelHandler.Sharable
public class BeatsHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = Logger.getLogger(Server.class.getName());
    private final IMessageListener messageListener;
    private ChannelHandlerContext ctx;

    public BeatsHandler(IMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object data) {


        logger.debug("Received a new payload");
        Batch batch = (Batch) data;
        AckingStrategy acking = AckingStrategy.get(Protocol.VERSION_2);



        for(Message message : batch.getMessages()) {
            this.messageListener.onNewMessage(message);

            if(requireAck(message)) {
                this.ack(message);
            } else {
                logger.debug("No ACK: " + message.getSequence() + " window size: " + message.getPayload().getWindowSize() );
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if(event instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) event;

            if(e.state() == IdleState.WRITER_IDLE) {
                this.sendKeepAlive();
            } else if(e.state() == IdleState.READER_IDLE) {
                this.clientTimeout();
            }
        }
    }

    private void clientTimeout() {
        logger.debug("Client Timeout");
        this.ctx.close();
    }

    private void sendKeepAlive() {
        logger.debug("Sending KeepAlive");

        ByteBuf buffer  = ctx.alloc().buffer(6);
        buffer.writeByte('2');
        buffer.writeByte('A');
        buffer.writeInt(0);

        this.ctx.writeAndFlush(buffer);
    }

    private void ack(Message message) {
        logger.debug("Sending ACK: " + message.getSequence() + " window size: " + message.getPayload().getWindowSize() );

        ByteBuf buffer  = ctx.alloc().buffer(6);
        buffer.writeByte('2');
        buffer.writeByte('A');
        buffer.writeInt(message.getSequence());

        this.ctx.writeAndFlush(buffer);

    }

    private boolean requireAck(Message message){
        if(message.getPayload().getWindowSize() == message.getSequence()) {
            return true;
        }
        return false;
    }
}