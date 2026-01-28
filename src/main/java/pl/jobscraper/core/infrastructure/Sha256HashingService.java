package pl.jobscraper.core.infrastructure;
import pl.jobscraper.core.domain.identity.HashingService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256HashingService implements HashingService {
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
