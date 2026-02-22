package pl.jobscraper.core.infrastructure.hashing;
import pl.jobscraper.core.domain.identity.HashingService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Infrastructure adapter providing SHA-256 hashing capabilities.
 * <p>
 * Uses the Java Security API to generate deterministic, 64-character
 * hexadecimal representations of string payloads.
 *
 * @see HashingService
 * @see pl.jobscraper.core.domain.identity.DefaultJobIdentityCalculator
 */
public class Sha256HashingService implements HashingService {
    /**
     * Computes an SHA-256 hash in hexadecimal format.
     *
     * @param input the string to hash (must not be null)
     * @return 64-character lowercase hexadecimal SHA-256 hash
     * @throws IllegalArgumentException if input is null
     * @throws RuntimeException if SHA-256 algorithm is not available (extremely rare)
     */
    @Override
    public String sha256Hex(String input) {
        if(input == null) {
            throw new IllegalArgumentException("input is null");
        }
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(hash);
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Converts byte array to lowercase hexadecimal string.
     * <p>
     * Each byte (8 bits) is converted to 2 hexadecimal characters (4 bits each).
     *
     * @param bytes byte array to convert (typically 32 bytes for SHA-256)
     * @return lowercase hexadecimal string (length = 2 * bytes.length)
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2*bytes.length);

        for(byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);

            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}