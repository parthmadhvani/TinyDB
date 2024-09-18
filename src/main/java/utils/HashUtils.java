package utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utility class for hashing operations.
 * This class provides methods to hash data using different algorithms.
 * It cannot be instantiated.
 */
public class HashUtils {

    // Private constructor to prevent instantiation
    private HashUtils() {
        throw new AssertionError("HashUtils class cannot be instantiated.");
    }

    /**
     * Hashes the given password using the MD5 algorithm.
     *
     * @param password The password to be hashed.
     * @return The MD5 hash of the given password as a hexadecimal string.
     */
    public static String hashMD5(String password) {
        // Use Apache Commons Codec to hash the password using MD5
        return DigestUtils.md5Hex(password);
    }
}
