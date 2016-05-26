import org.junit.Test;
import org.logstash.beats.Message;
import org.logstash.beats.Payload;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;


public class MessageTest {
    @Test
    public void TestGetData() {
        Map<Object, Object> map = new HashMap();

        Message message = new Message(new Payload(), 1, map);
        assertEquals(map, message.getData());
    }

    @Test
    public void TestPayload() {
        Map<Object, Object> map = new HashMap();
        Payload payload = new Payload();

        Message message = new Message(payload, 1, map);
        assertEquals(payload, message.getPayload());
    }

    @Test
    public void TestGetSequence() {
        Map<Object, Object> map = new HashMap();
        Payload payload = new Payload();

        Message message = new Message(payload, 1, map);
        assertEquals(1, message.getSequence());
    }

    @Test
    public void TestComparison() {
        Map<Object, Object> map = new HashMap();
        Payload payload = new Payload();
        Message messageOlder = new Message(payload, 1, map);
        Message messageNewer = new Message(payload, 2, map);
    }
}
