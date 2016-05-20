package org.logstash.beats;

/**
 * Created by ph on 2016-05-18.
 */
public interface IMessageListener {
    public void onNewMessage(Message message);
}
