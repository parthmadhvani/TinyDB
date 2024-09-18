package utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HashUtilsTest {

	@Test
    void testHashMD5() {
        String password = "password123";
        String expectedHash = "482c811da5d5b4bc6d497ffa98491e38"; // Precomputed MD5 hash for "password123"
        
        String actualHash = HashUtils.hashMD5(password);
        
        assertEquals(expectedHash, actualHash, "The hashed password should match the expected MD5 hash.");
    }

    @Test
    void testHashMD5WithEmptyString() {
        String password = "";
        String expectedHash = "d41d8cd98f00b204e9800998ecf8427e"; // Precomputed MD5 hash for an empty string
        
        String actualHash = HashUtils.hashMD5(password);
        
        assertEquals(expectedHash, actualHash, "The hashed empty string should match the expected MD5 hash.");
    }

    @Test
    void testHashMD5WithNull() {
        assertThrows(NullPointerException.class, () -> {
            HashUtils.hashMD5(null);
        }, "Hashing null should throw NullPointerException.");
    }


}
