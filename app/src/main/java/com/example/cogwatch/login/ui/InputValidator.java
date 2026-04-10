package com.example.cogwatch.login.ui;

import java.util.regex.Pattern;

/**
 * 账号密码输入验证器
 * 使用正则表达式进行格式验证，防止暴力破解
 */
public class InputValidator {

    /**
     * 密码复杂度要求：
     * - 至少8位
     * - 包含大写字母
     * - 包含小写字母
     * - 包含数字
     * - 包含特殊字符（@$!%*?&）
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    /**
     * 账号格式要求：
     * - 支持手机号（11位数字，以1开头）
     * - 支持邮箱（字母数字@字母数字.字母）
     * - 支持字母数字组合（3-20位）
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^((1[3-9]\\d{9})|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|[a-zA-Z0-9]{3,20})$"
    );

    /**
     * 检测连续重复字符（限制连续5个以上重复）
     */
    private static final Pattern REPEATING_CHARS_PATTERN = Pattern.compile("(.)\\1{4,}");

    /**
     * 键盘常见序列
     */
    private static final String[] KEYBOARD_SEQUENCES = {
            "qwerty", "asdfgh", "zxcvbn", "123456", "098765", "qazwsx"
    };

    private InputValidator() {
        // 工具类，禁止实例化
    }

    /**
     * 验证密码复杂度
     * @param password 密码
     * @return true 表示密码符合要求
     */
    public static boolean isPasswordValid(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 验证账号格式
     * @param username 账号（支持手机号、邮箱、字母数字组合）
     * @return true 表示账号符合要求
     */
    public static boolean isUsernameValid(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 获取密码强度等级（0-4）
     * 0: 不符合基本要求
     * 1: 弱（仅满足长度要求）
     * 2: 中等（满足长度+1类字符）
     * 3: 良好（满足长度+2类字符）
     * 4: 强（满足所有字符类型要求）
     */
    public static int getPasswordStrengthLevel(String password) {
        if (password == null || password.length() < 8) {
            return 0;
        }

        int level = 0;
        if (password.matches(".*[a-z].*")) level++;
        if (password.matches(".*[A-Z].*")) level++;
        if (password.matches(".*\\d.*")) level++;
        if (password.matches(".*[@$!%*?&].*")) level++;

        return level;
    }

    /**
     * 获取密码强度描述
     */
    public static String getPasswordStrengthText(int level) {
        switch (level) {
            case 0: return "密码不符合要求";
            case 1: return "弱";
            case 2: return "中等";
            case 3: return "良好";
            case 4: return "强";
            default: return "未知";
        }
    }

    /**
     * 检测连续重复字符（防爆）
     */
    public static boolean hasExcessiveRepeatingChars(String input) {
        return input != null && REPEATING_CHARS_PATTERN.matcher(input).find();
    }

    /**
     * 检测键盘常见序列（防爆）
     */
    public static boolean hasKeyboardSequence(String input) {
        if (input == null) return false;
        String lower = input.toLowerCase();
        for (String seq : KEYBOARD_SEQUENCES) {
            if (lower.contains(seq)) {
                return true;
            }
        }
        return false;
    }
}
