import org.junit.Test;
import org.logstash.beats.Message;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;


public class MessageTest {
    @Test
    public void TestGetData() {
        Map<Object, Object> map = new HashMap();

        Message message = new Message(1, map);
        assertEquals(map, message.getData());
    }

    @Test
    public void TestGetSequence() {
        Map<Object, Object> map = new HashMap();

        Message message = new Message(1, map);
        assertEquals(1, message.getSequence());
    }

    @Test
    public void TestComparison() {
        Map<Object, Object> map = new HashMap();
        Message messageOlder = new Message(1, map);
        Message messageNewer = new Message(2, map);
        assertThat(messageNewer, greaterThan(messageOlder))
    }
}
