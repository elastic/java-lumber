import org.junit.Before;
import org.junit.Test;
import org.logstash.beats.AckMessage;
import org.logstash.beats.Batch;
import org.logstash.beats.Message;
import org.logstash.beats.Protocol;

import java.util.HashMap;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by ph on 2016-06-01.
 */
public class AckMessageTest {
    private Batch batch;
    private int sequence;
    private AckMessage ack;
    private final int windowSize = 201;

    @Before
    public void setup() {
        this.sequence = 200;
        this.batch = new Batch();
        this.batch.setProtocol(Protocol.VERSION_2);
        this.batch.setWindowSize(this.windowSize);
        this.batch.addMessage(new Message(this.sequence, new HashMap<Object,Object>()));
        this.ack = new AckMessage(this.batch.getMessages().get(0));
    }

    @Test
    public void testGetProtocol() {
        assertEquals(Protocol.VERSION_2, this.ack.getProtocol());
    }

    @Test
    public void testGetSequenceTest() {
        assertEquals(this.sequence, this.ack.getSequence());
    }

    @Test
    public void TestWhenSequenceIsNotEndOfWindowSize() {
        assertFalse(ack.ackNeeded());
    }

    @Test
    public void TestWhenSequenceIsEndOfWindowSize() {
        this.batch.addMessage(new Message(this.windowSize, new HashMap<Object, Object>()));
        this.ack = new AckMessage(this.batch.getMessages().get(1));
        assertTrue(ack.ackNeeded());
    }
}
