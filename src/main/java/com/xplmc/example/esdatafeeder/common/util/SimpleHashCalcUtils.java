package com.xplmc.example.esdatafeeder.common.util;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * simple hash calculation impl
 *
 * @author luke
 */
public class SimpleHashCalcUtils {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHashCalcUtils.class);

    private static final String SHA1 = "SHA1";

    /**
     * calculate SHA1 checksum
     *
     * @param input file path
     * @return file SHA1 checksum, null when something bad happened
     */
    public static String sha1sum(File input) {
        byte[] checksum = checksum(input, SHA1);
        return checksum == null ? null : Hex.encodeHexString(checksum);
    }


    /**
     * calculate input's checksum using designated hash algorithm
     *
     * @param input     input file
     * @param algorithm hash algorithm
     * @return checksum bytes, maybe <b>null</b> when something bad happened
     */
    private static byte[] checksum(File input, String algorithm) {
        try (InputStream in = new FileInputStream(input)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return digest.digest();
        } catch (Exception e) {
            logger.error("hash calculation error", e);
        }
        return null;
    }

}
