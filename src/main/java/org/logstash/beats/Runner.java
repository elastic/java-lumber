package org.logstash.beats;

import io.netty.handler.ssl.SslContext;
import io.netty.util.ResourceLeakDetector;
import org.apache.log4j.Logger;
import org.logstash.netty.PrivateKeyConverter;
import org.logstash.netty.SslSimpleBuilder;


public class Runner {
    private static final int DEFAULT_PORT = 5044;
    public static Logger logger = Logger.getLogger(Runner.class.getName());


    static public void main(String[] args) throws Exception {
        logger.info("Starting Beats Bulk");

        // Check for leaks.
        // ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        Server server = new Server(DEFAULT_PORT);

        if(args.length > 0 && args[0].equals("ssl")) {
            logger.debug("Using SSL");

            String sslCertificate = "/Users/ph/es/certificates/certificate.crt";
            String sslKey = "/Users/ph/es/certificates/certificate.pkcs8.key";

            logger.debug("SSLCertificate: " + sslCertificate);
            logger.debug("SSLKey: " + sslKey);

            SslSimpleBuilder sslBuilder = new SslSimpleBuilder(sslCertificate, sslKey)
                    .setProtocols(new String[] { "TLSv1.2" })
                    .setCertificateAuthorities(sslCertificate);
            server.enableSSL(sslBuilder);
        }

        server.listen();
    }
}