import org.junit.Test;
import org.logstash.beats.Payload;
import static org.junit.Assert.assertEquals;


public class PayloadTest {
    @Test
    public void testWindowSize() {
        Payload payload = new Payload();
        payload.setWindowSize(20);
        assertEquals(20, payload.getWindowSize());
    }

    @Test
    public void testProcotol() {
        Payload payload = new Payload();
        payload.setProtocol(2);
        assertEquals(2, payload.getProtocol());
    }
}