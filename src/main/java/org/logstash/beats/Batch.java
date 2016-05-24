package org.logstash.beats;

import java.util.ArrayList;
import java.util.List;

public class Batch {
    private List<Message> messages = new ArrayList<>();

    public List<Message> getMessages() {
        return this.messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public long size() {
        return this.messages.size();
    }

    public boolean isEmpty() {
        if(0 == this.messages.size()) {
            return true;
        } else {
            return false;
        }
    }
}