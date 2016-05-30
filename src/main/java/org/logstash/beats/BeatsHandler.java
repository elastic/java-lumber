package org.logstash.beats;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

@ChannelHandler.Sharable
public class BeatsHandler extends ChannelInboundHandlerAdapter {
    private AckingStrategy acking = AckingStrategy.get(Protocol.VERSION_1);
    private static Logger logger = Logger.getLogger(Server.class.getName());
    private AtomicBoolean processing = new AtomicBoolean(false);
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

        this.processing.compareAndSet(false, true);

        Batch batch = (Batch) data;
        for(Message message : batch.getMessages()) {
            logger.debug("Sending a new message for the listener, sequence: " + message.getSequence());
            this.messageListener.onNewMessage(message);
            this.acking.ack(message, ctx);
        }

        this.processing.compareAndSet(true, false);
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
                this.clientTimeout();Ide
            }
        }
    }

    private void clientTimeout() {
        logger.debug("Client Timeout");
        this.ctx.close();
    }

    private void sendKeepAlive() {
        // If we are actually blocked on processing
        // we can send a keep alive.
        if(this.processing.get()) {
            logger.debug("Sending KeepAlive");
            this.acking.keepAlive(this.ctx);
        }
    }
}