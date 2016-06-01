import org.junit.Test;
import org.logstash.beats.Batch;
import org.logstash.beats.Message;

import java.util.HashMap;
import static org.junit.Assert.*;

public class BatchTest {

    @Test
    public void testIsEmpty() {
        Batch batch = new Batch();
        assertTrue(batch.isEmpty());
        batch.addMessage(new Message(1, new HashMap()));
        assertFalse(batch.isEmpty());
    }

    @Test
    public void testSize() {
        Batch batch = new Batch();
        assertEquals(0, batch.size());
        batch.addMessage(new Message(1, new HashMap()));
        assertEquals(1, batch.size());
    }
}