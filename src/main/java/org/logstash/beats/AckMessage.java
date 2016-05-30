package org.logstash.beats;

/**
 * Created by ph on 2016-05-30.
 */
public class AckMessage {
    private int sequence;
    private boolean ackNeeded = false;
    private int protocol;

    public AckMessage(Message message) {
        this.protocol = message.getBatch().getProtocol();
        this.sequence = message.getSequence();

        // We have remove the partial ack from V2,
        // un the current implementation this might come back.
        if(message.getBatch().getWindowSize() == message.getSequence()) this.ackNeeded = true;
    }

    public AckMessage() {
    }

    public boolean ackNeeded() {
        return this.ackNeeded;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getSequence() {
        return sequence;
    }
}
