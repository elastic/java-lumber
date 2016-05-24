package org.logstash.beats;

/**
 * Created by ph on 2016-05-20.
 */
public abstract class AckingStrategy {
        private static AckingStrategy ackingStraegyV2 = new AckingStrategyV2();

        public static AckingStragy get(byte protocol, ChannelContext) {

        }
}
