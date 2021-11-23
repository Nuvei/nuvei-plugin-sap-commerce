package com.nuvei.services.util;

import com.nuvei.services.enums.NuveiHashAlgorithm;
import com.safecharge.util.Constants;
import com.safecharge.util.Constants.HashAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class to handle operations with the Hash Algorithms
 */
public final class NuveiHashAlgorithmUtil {
    private static final Logger LOG = LogManager.getLogger(NuveiHashAlgorithmUtil.class);

    private NuveiHashAlgorithmUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Static convenience method that returns a {@link HashAlgorithm} for a given {@link NuveiHashAlgorithm}
     *
     * @param nuveiHashAlgorithm The {@link NuveiHashAlgorithm} to be converted
     * @return The corresponding {@link HashAlgorithm} for the given {@link NuveiHashAlgorithm}
     */
    public static HashAlgorithm getNuveiHashAlgorithmName(final NuveiHashAlgorithm nuveiHashAlgorithm) {
        if (NuveiHashAlgorithm.SHA256.equals(nuveiHashAlgorithm)) {
            return HashAlgorithm.SHA256;
        }
        if (NuveiHashAlgorithm.MD5.equals(nuveiHashAlgorithm)) {
            return HashAlgorithm.MD5;
        }
        return null;
    }

    /**
     * @param text
     * @param charset
     * @param algorithm
     * @return String with the hash
     */
    public static String getHash(String text, String charset, Constants.HashAlgorithm algorithm) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm.getAlgorithm());

        } catch (NoSuchAlgorithmException nsae) {
            LOG.error("Implementation of {} not found.", algorithm, nsae);
            return null;
        }

        CharsetEncoder encoder = Charset.forName(charset)
                .newEncoder();

        ByteBuffer encoded;
        try {
            encoded = encoder.encode(CharBuffer.wrap(text));
        } catch (CharacterCodingException e) {
            LOG.error("Cannot encode text into bytes using charset {} : {}",charset, e.getMessage());
            return null;
        }

        byte[] inbytes;

        inbytes = new byte[encoded.remaining()];

        encoded.get(inbytes, 0, inbytes.length);

        byte[] bytes = md.digest(inbytes);

        // Output the bytes of the hash as a String (text/plain)
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            int low = bytes[i] & 0x0f;
            int high = (bytes[i] & 0xf0) >> 4;
            sb.append(Constants.HEXADECIMAL[high]);
            sb.append(Constants.HEXADECIMAL[low]);
        }

        return sb.toString();
    }
}
