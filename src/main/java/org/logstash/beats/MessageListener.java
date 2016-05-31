package org.logstash.beats;

import io.netty.channel.ChannelHandlerContext;

// This need to be implemented in Ruby
public class MessageListener implements IMessageListener {
    public void onNewMessage(Message message) {
    }

    @Override
    public void onNewConnection(ChannelHandlerContext ctx) {
    }

    @Override
    public void onConnectionClose(ChannelHandlerContext ctx) {
    }
}
