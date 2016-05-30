package org.logstash.beats;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.apache.log4j.Logger;


public class Server {
    private int port;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workGroup;

    private IMessageListener messageListener = new MessageListener();

    static Logger logger = Logger.getLogger(Server.class.getName());
    private SslContext sslContext;

    public Server(int port) {
        this.port = port;
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup();
    }

    public void enableSSL(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    public Server listen() throws InterruptedException {
        try {
            logger.info("Starting server listing port: " + this.port);

            ServerBootstrap server = new ServerBootstrap();
            server.group(this.bossGroup, this.workGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler())
                    .childHandler(new BeatsInitializer(this));

            Channel channel = server.bind(this.port).sync().channel();

            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

        return this;
    }

    public void setMessageListener(IMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public boolean isSslEnable() {
        if(this.sslContext != null) {
            return true;
        } else {
            return false;
        }
    }

    private class BeatsInitializer extends ChannelInitializer<SocketChannel> {
        private final int DEFAULT_IDLESTATEHANDLER_THREAD = 4;
        private final EventExecutorGroup idleExecutorGroup = new DefaultEventExecutorGroup(DEFAULT_IDLESTATEHANDLER_THREAD);
        private final BeatsHandler beatsHandler;
        private final LoggingHandler loggingHandler = new LoggingHandler();
        private final AckMessageEncoder ackEncoder = new AckMessageEncoder();

        private final Server server;

        public BeatsInitializer(Server server) {
            this.server = server;
            this.beatsHandler = new BeatsHandler(server.messageListener);
        }

        public void initChannel(SocketChannel socket) {
            ChannelPipeline pipeline = socket.pipeline();

            if(server.isSslEnable()) {
                SslHandler sslHandler = server.sslContext.newHandler(socket.alloc());
                sslHandler.engine().setEnabledProtocols(new String[] { "TLSv1.2" });
                pipeline.addLast(sslHandler);
            }

            pipeline.addLast("logger", this.loggingHandler);
            // We have set a specific executor for the idle check, because the `beatsHandler` can be
            // blocked on the queue, this the idleStateHandler manage the `KeepAlive` signal.
            pipeline.addLast(idleExecutorGroup, "keep-alive-handler", new IdleStateHandler(60*15, 5, 0));
            pipeline.addLast("beats-parser", new BeatsParser());
            pipeline.addLast("beats-handler", this.beatsHandler);
            pipeline.addLast("ack-encoder", ackEncoder);
        }
    }
}
