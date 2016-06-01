import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import org.logstash.beats.AckMessageEncoder;
import org.logstash.beats.KeepAliveMessage;

/**
 * Created by ph on 2016-06-01.
 */
public class AckMessageEncoderTest {

/*    public testEncodingKeepAlive() {
        EmbeddedChannel channel = new EmbeddedChannel(new AckMessageEncoder());
        channel.writeOutbound(new KeepAliveMessage());
        channel.writeInbound(channel.readOutbound());
    }*/
}