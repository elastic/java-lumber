package org.logstash.beats;

import io.netty.channel.ChannelHandlerContext;

public abstract class AckingStrategy {
        private static AckingStrategy ackingStrategyV2 = new AckingStrategyV2();

        public abstract void ack(Message message, ChannelHandlerContext ctx);

        public void keepAlive(ChannelHandlerContext ctx) {
        }

        public static AckingStrategy get(byte protocol) {
                return ackingStrategyV2;
        }


}
