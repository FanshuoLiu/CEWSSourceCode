import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class sha {
    public static int generateSecureDecimalKey(int keyLength) {
        // 创建SecureRandom实例来获取安全的随机数
        SecureRandom secureRandom = new SecureRandom();
        int maxValue = (int) Math.pow(10, keyLength) - 1;
        return secureRandom.nextInt(maxValue);
    }

    public static void main(String[] args) {
        int keyLength = 6; // 假设生成8位十进制的密钥
        int decimalKey = generateSecureDecimalKey(keyLength);
        System.out.println("生成的安全十进制密钥: " + decimalKey);
    }
}
