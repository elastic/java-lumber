package org.logstash.netty;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;
import org.bouncycastle.util.io.pem.PemReader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ph on 2016-05-27.
 */
public class SslSimpleBuilder {
    public static Logger logger = Logger.getLogger(SslSimpleBuilder.class.getName());

    private final File sslKeyFile;
    private final File sslCertificateFile;
    private final boolean insecure = false;


    // TODO default to TLSv1.2
    // TODO DEFAULT TO THE MODERN MOZILLA CIPHERS LIST.
    // TODO LSF MIGHT NEED TO CHANGES STUFF IN THEIR CONFIG.
    // TODO Mutual auth.

    // TODO disable local trust store by default.
    // This cipher selection comes from https://wiki.mozilla.org/Security/Server_Side_TLS
    // TODO cypher_suites
    // I think I would be in favor of using the defaults?
    // LOOK at the IANA column on the mozilla page.
    private final String[] DEFAULT_CIPHERS = new String[] {

    };
    private String[] protocols = new String[] { "TLSv1", "TLSv1.1", "TLSv1.2" };
    private String[] certificateAuthorities;
    private String verifyMode;

    // TODO TLS, lets try to make sure we use similar or the same name for the option if possible.
    // https://www.elastic.co/guide/en/beats/filebeat/current/configuration-output-tls.html#_max_version
    // https://www.elastic.co/guide/en/beats/filebeat/current/configuration-output-tls.html#_min_version

    // TODO insecure.
    // TODO, check if we can convert x509 to PKCS8 on the fly.
    // because netty sslcontext builder only support pkcs8 for the private key ..
    // openssl pkcs8 -topk8 -nocrypt -in pkcs1_key_file -out pkcs8_key.pem
    // http://netty.io/wiki/sslcontextbuilder-and-private-key.html
    public SslSimpleBuilder(String sslCertificateFilePath, String sslKeyFilePath) {
        this.sslCertificateFile = this.createFile(sslCertificateFilePath);
        this.sslKeyFile = this.createFile(sslKeyFilePath);
    }

    public SslSimpleBuilder protocols(String[] protocols) {
        this.protocols = protocols;
        return this;
    }

    public SslContext build() throws SSLException {
        SslContextBuilder sslBuilder = SslContextBuilder.forServer(this.sslCertificateFile, this.sslKeyFile, null);
        //sslBuilder.ciphers(Arrays.asList(DEFAULT_CIPHERS));
        SslContext context = sslBuilder.build();

        logger.debug(context.cipherSuites());
        SslHandler handler = context.build();


        return context;
    }


    private File createFile(String filepath) {
        return new File(filepath);
    }

    public SslSimpleBuilder setCertificateAuthorities(String[] certificateAuthorities) {
        this.certificateAuthorities = certificateAuthorities;
        return this;
    }

    public SslSimpleBuilder setVerifyMode(String verifyMode) {
        this.verifyMode = verifyMode;
        return this;
    }
}