package org.logstash.beats;

/**
 * Created by ph on 2016-05-30.
 */
public class KeepAliveMessage extends AckMessage {
    public KeepAliveMessage() {}

    public boolean ackNeeded() {
        return true;
    }

    public int getSequence() {
        return 0;
    }

    public int getProtocol() {
        return Protocol.VERSION_2;
    }
}
