package org.logstash.netty;

import org.apache.log4j.Logger;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Created by ph on 2016-06-01.
 */
public class PrivateKeyConverter {
    private String passphrase;
    private PrivateKey privateKey;
    private File orignalPrivateKey;
    private KeyPair keypair;
    private final static Logger logger = Logger.getLogger(PrivateKeyConverter.class.getName());


    public PrivateKeyConverter(String filepath, String pw) {
        orignalPrivateKey = new File(filepath);
        passphrase = pw;
        privateKey = loadPrivateKey();
    }

    public File getPkcs8() {
        if(isPkcs8()) {
            return orignalPrivateKey;
        } else {
            return orignalPrivateKey;
        }
    }

    private boolean isPkcs8() {
        logger.info("PKey format" + privateKey.getFormat());
        return false;
    }

    private PrivateKey loadPrivateKey() {
        try {
            PemReader pem = new PemReader(new FileReader(orignalPrivateKey));

            PemObject obj = pem.readPemObject();
            logger.debug("type: " + obj.getType());

        } catch (FileNotFoundException e) {
            logger.fatal("File doesn't exist, file: " + orignalPrivateKey.getPath());
        } catch (IOException e) {
            logger.fatal("Cannot load the private key.");
        }
        return null;
    }
}