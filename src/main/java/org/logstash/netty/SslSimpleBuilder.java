package org.logstash.netty;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;
import org.apache.tomcat.jni.SSLContext;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by ph on 2016-05-27.
 */
public class SslSimpleBuilder {
    public static Logger logger = Logger.getLogger(SslSimpleBuilder.class.getName());

    private File sslKeyFile;
    private File sslCertificateFile;

    // ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    /*

    Mordern Ciphers List from
    https://wiki.mozilla.org/Security/Server_Side_TLS

    ECDHE-ECDSA-AES256-GCM-SHA384
    ECDHE-RSA-AES256-GCM-SHA384
    ECDHE-ECDSA-CHACHA20-POLY1305 X
    ECDHE-RSA-CHACHA20-POLY1305 X
    ECDHE-ECDSA-AES128-GCM-SHA256
    ECDHE-RSA-AES128-GCM-SHA256
    ECDHE-ECDSA-AES256-SHA384
    ECDHE-RSA-AES256-SHA384
    ECDHE-ECDSA-AES128-SHA256
    */

    private String[] ciphers = new String[] {
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA38",
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"
    };
    private String[] protocols = new String[] { "TLSv1.2" };
    private String certificateAuthorities;
    private String verifyMode;
    private String passPhrase;

    public SslSimpleBuilder(String sslCertificateFilePath, String sslKeyFilePath) {
        sslCertificateFile = createFile(sslCertificateFilePath);
        sslKeyFile = createFile(sslKeyFilePath);
    }

    public SslSimpleBuilder setProtocols(String[] protocols) {
        protocols = protocols;
        return this;
    }

    public SslSimpleBuilder setCiphersSuite(String [] ciphersSuite) {
        ciphers = ciphersSuite;
        return this;
    }

    public SslSimpleBuilder setCertificateAuthorities(String cert) {
        certificateAuthorities = cert;
        return this;
    }

    public SslSimpleBuilder setVerifyMode(String mode) {
        verifyMode = mode;
        return this;
    }

    public File getSslKeyFile() {
        return sslKeyFile;
    }

    public File getSslCertificateFile() {
        return sslCertificateFile;
    }

    public SslHandler build(ByteBufAllocator bufferAllocator) throws SSLException {
        SslContextBuilder builder = SslContextBuilder.forServer(sslCertificateFile, sslKeyFile, passPhrase);
        logger.debug("Ciphers: " + String.join(",", ciphers));

        builder.ciphers(Arrays.asList(ciphers));

        if(requireClientAuth()) {
            logger.debug("Certificate Authorities: " + certificateAuthorities);
            builder.trustManager(createFile(certificateAuthorities));
        }

        SslContext context = builder.build();
        SslHandler sslHandler = context.newHandler(bufferAllocator);

        logger.debug("TLS: " +  String.join(",", protocols));
        SSLEngine engine = sslHandler.engine();
        engine.setEnabledProtocols(protocols);

        if(requireClientAuth()) {
            engine.setUseClientMode(false);
            engine.setNeedClientAuth(true);
        }

        return sslHandler;
    }

    private boolean requireClientAuth() {
        if(certificateAuthorities != null) {
            return true;
        }

        return false;
    }

    private File createFile(String filepath)
    {
        return new File(filepath);
    }

    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }
}