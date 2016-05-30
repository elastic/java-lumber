package org.logstash.beats;

import java.util.Map;

public class Message implements Comparable<Message> {
    private int sequence;
    private String identityStream;
    private Map data;
    private Batch batch;

    public Message(int sequence, Map map) {
        setSequence(sequence);
        setData(map);
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    @Override
    public int compareTo(Message o) {
        return Integer.compare(this.getSequence(), o.getSequence());
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }
}