import org.junit.Before;
import org.junit.Test;
import org.logstash.beats.KeepAliveMessage;
import org.logstash.beats.Protocol;

import static org.junit.Assert.*;

/**
 * Created by ph on 2016-06-01.
 */
public class KeepAliveMessageTest {
    private KeepAliveMessage keepalive;

    @Before
    public void setup() {
        this.keepalive = new KeepAliveMessage();
    }

    @Test
    public void ackNeedTest() {
        assertTrue(this.keepalive.ackNeeded());
    }

    @Test
    public void getSequenceTest() {
        assertEquals(0, this.keepalive.getSequence());
    }

    @Test
    public void getProtocolTest() {
        assertEquals(Protocol.VERSION_2, this.keepalive.getProtocol());
    }
}
