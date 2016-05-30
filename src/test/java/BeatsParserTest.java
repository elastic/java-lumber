import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Before;
import org.junit.Test;
import org.logstash.beats.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class BeatsParserTest {
    private Batch batch;
    private Message message1;
    private Message message2;

    @Before
    public void setup() {
        this.batch = new Batch();
        this.batch.setProtocol(Protocol.VERSION_2);

        Map map1 = new HashMap<String, String>();
        map1.put("line", "My super event log");
        map1.put("coming-from", "beats");

        this.message1 = new Message(1, map1);
        this.batch.addMessage(this.message1);

        Map map2 = new HashMap<String, String>();
        map2.put("line", "Another world");
        map2.put("game", "Little big Adventure");

        this.message2 = new Message(2, map2);
        this.batch.addMessage(this.message2);
    }

    @Test
    public void testEncodingDecodingJson() {
        EmbeddedChannel channel = new EmbeddedChannel(new BatchEncoder(), new BeatsParser());
        channel.writeOutbound(this.batch);
        channel.writeInbound(channel.readOutbound());

        Batch decodedBatch = (Batch) channel.readInbound();
        assertNotNull(decodedBatch);

        assertEquals(this.batch.getMessages().get(0).getSequence(), decodedBatch.getMessages().get(0).getSequence());
        assertEquals(this.batch.getMessages().get(1).getSequence(), decodedBatch.getMessages().get(1).getSequence());
        assertEquals(this.batch.getMessages().get(0).getData().get("line"), decodedBatch.getMessages().get(0).getData().get("line"));
        assertEquals(this.batch.getMessages().get(1).getData().get("line"), decodedBatch.getMessages().get(1).getData().get("line"));
        assertEquals(this.batch.size(), decodedBatch.size());
    }

    @Test
    public void testCompressedEncodingDecodingJson() {
        EmbeddedChannel channel = new EmbeddedChannel(new CompressedBatchEncoder(), new BeatsParser());
        channel.writeOutbound(this.batch);
        channel.writeInbound(channel.readOutbound());

        Batch decodedBatch = (Batch) channel.readInbound();
        assertNotNull(decodedBatch);

        assertEquals(this.batch.getMessages().get(0).getSequence(), decodedBatch.getMessages().get(0).getSequence());
        assertEquals(this.batch.getMessages().get(1).getSequence(), decodedBatch.getMessages().get(1).getSequence());
        assertEquals(this.batch.getMessages().get(0).getData().get("line"), decodedBatch.getMessages().get(0).getData().get("line"));
        assertEquals(this.batch.getMessages().get(1).getData().get("line"), decodedBatch.getMessages().get(1).getData().get("line"));
        assertEquals(this.batch.size(), decodedBatch.size());
    }
}
