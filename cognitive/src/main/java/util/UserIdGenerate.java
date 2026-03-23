package util;

import java.security.SecureRandom;
import java.util.Random;

public class UserIdGenerate {

    // 字符库：包含 大写字母 + 小写字母 + 数字
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    // 使用 SecureRandom 以获得更好的随机性（推荐用于 Android）
    private static final Random RANDOM = new SecureRandom();

    /**
     * 生成指定长度的随机 alphanumeric 字符串
     *
     * @param length 字符串长度 (n)
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        if (length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}