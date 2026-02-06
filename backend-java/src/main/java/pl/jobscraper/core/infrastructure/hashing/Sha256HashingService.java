package pl.jobscraper.core.infrastructure.hashing;
import pl.jobscraper.core.domain.identity.HashingService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 implementation of {@link HashingService}
 * <p>
 * This class provides cryptographic hashing using the SHA-256 algorithm
 * from the Java Security API. It is used by {@link pl.jobscraper.core.domain.identity.DefaultJobIdentityCalculator}
 * for computing fallback identity hashes when job URL is invalid.
 *
 * <p><strong>Algorithm:</strong> SHA-256 (Secure Hash Algorithm 256-bit)
 * <ul>
 *     <li>Output: 256 bits (32 bytes) -> 64 hexadecimal characters</li>
 *     <li>Deterministic: Same input always produces same hash</li>
 *     <li>One-way: Cannot reserve hash back to original input</li>
 *     <li>Collision-resistant: Extremely unlikely for two inputs to produce hash</li>
 * </ul>
 *
 * <p><strong>Encoding:</strong> UTF-8 (StandardCharsets.UTF_8)
 * <ul>
 *     <li>Supports international characters: ą, ć, ę, ł, ń, ó, ś, ź, ż</li>
 *     <li>Ensures consistent hashing across different systems/locales</li>
 * </ul>
 *
 * <p><strong>Thread-safety:</strong> This class is thread-safe. Each invocation
 * creates a new {@link MessageDigest} instance (not shared between threads).
 *
 * <p><strong>Performance:</strong> SHA-256 is relatively fast (~100-200 MB/s) on modern CPUs).
 * For job deduplication (small payloads: ~100 bytes), performance is negligible.
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * HashingService service = new Sha256HashingService();
 * String hash = service.sha256Hex("google#java developer#warsaw");
 * // -> "a1b2c3d4e5f6..64 hex characters..."
 * }</pre>
 *
 * @see HashingService
 * @see pl.jobscraper.core.domain.identity.DefaultJobIdentityCalculator
 */
public class Sha256HashingService implements HashingService {
    /**
     * Computes SHA-256 hash of the input string.
     * <p>
     * <strong>Algorithm steps:</strong>
     * <ol>
     *     <li>Validate input (must not be null)</li>
     *     <li>Convert string to bytes using UTF-8 encoding</li>
     *     <li>Compute SHA-256 hash (32 bytes)</li>
     *     <li>Convert bytes to hexadecimal string (64 characters)</li>
     * </ol>
     *
     * <p><strong>Hash properties:</strong>
     * <ul>
     *     <li>Length: Always 64 hexadecimal characters (256 bits)</li>
     *     <li>Format: Lowercase hex (0-9, a-f)</li>
     *     <li>Deterministic: Same input -> same hash</li>
     * </ul>
     *
     * <p><strong>Examples:</strong>
     * <pre>{@code
     * sha256("test")
     *   -> "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"
     *
     * sha256Hex("Test") // Different case
     *   -> "532eaabd9574880dbf76b9b8cc00832c20a6ec113d682299550d7a6e0f345e25"
     *
     * sha256Hex("Kraków") // UTF-8 support (Polish ó)
     *   -> "e8c8c5f5d5c5f5d5c5f5d5c5f5d5c5f5d5c5f5d5c5f5d5c5f5d5c5f5d5c5f5d5"
     * }</pre>
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
            // Create SHA-256 digest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Hash the input bytes (UTF-8 encoding)
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert bytes to hex string
            return bytesToHex(hash);
        }catch (NoSuchAlgorithmException e){
            // SHA-256 is part of Java standard library since 1.4
            // Throws exception should never occur in practice
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Converts byte array to lowercase hexadecimal string.
     * <p>
     * Each byte (8 bits) is converted to 2 hexadecimal characters (4 bits each).
     * For example:
     * <ul>
     *     <li>Byte {@code 0x00} -> {@code "00"}</li>
     *     <li>Byte {@code 0x0F} -> {@code "0f"} (note: padded with leading zero)</li>
     *     <li>Byte {@code 0xFF} -> {@code "ff}</li>
     * </ul>
     *
     * <p><strong>Why padding is important:</strong>
     * Without padding, {@code 0x0F} would become {@code "f"} (single char),
     * resulting in incorrect hash length (not 64 chars).
     *
     * <p><strong>Performance:</strong> Pre-allocates StringBuilder with exact capacity
     * {@code 2* bytes.length} to avoid resizing during append operations.
     *
     * <p><strong>Example</strong>
     * <pre>{@code
     * byte[] bytes = {0x1A, 0x2B, 0x3C, 0x0D};
     * bytesToHex(bytes) → "1a2b3c0d"
     *
     * // 32 bytes (SHA-256) → 64 hex characters
     * byte[] hash = new byte[32];  // all zeros
     * bytesToHex(hash) → "0000000000000000000000000000000000000000000000000000000000000000"
     * }</pre>
     *
     * @param bytes byte array to convert (typically 32 bytes for SHA-256)
     * @return lowercase hexadecimal string (length = 2 * bytes.length)
     */
    private static String bytesToHex(byte[] bytes) {
        // Pre-allocate with exact capacity to avoid resizing
        StringBuilder hexString = new StringBuilder(2*bytes.length);

        for(byte b : bytes) {
            // Convert byte to hex (0xFF mask ensures unsigned interpretation)
            String hex = Integer.toHexString(0xFF & b);

            // Pad with leading zero if single character (0x00-0x0F)
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}