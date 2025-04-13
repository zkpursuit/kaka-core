package com.kaka.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串辅助工具集
 *
 * @author zkpursuit
 */
public class StringUtils {

    private static final char[] nameChars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

    /**
     * 随机生成字符串
     *
     * @param random     随机数发生器
     * @param chars      字符数组，为null时使用默认字符数组
     * @param count      字符串字符个数
     * @param charRepeat 是否允许字符串中有重复字符，true为可以有重复字符
     * @return 随机的字符串
     */
    public static String randomString(Random random, char[] chars, int count, boolean charRepeat) {
        StringBuilder sb = new StringBuilder();
        chars = chars == null ? nameChars : chars;
        int len = chars.length - 1;
        int index;
        char c;
        int ci = 0;
        while (ci < count) {
            if (!charRepeat) {
                do {
                    c = chars[random.nextInt(len)];
                    int length = sb.length();
                    index = -1;
                    for (int i = 0; i < length; i++) {
                        char _c = sb.charAt(i);
                        if (_c == c) {
                            index = i;
                            break;
                        }
                    }
                } while (index >= 0);
            } else {
                c = chars[MathUtils.getRandom().nextInt(len)];
            }
            sb.append(c);
            ci++;
        }
        return sb.toString();
    }

    /**
     * 随机生成字符串
     *
     * @param chars      字符数组，为null时使用默认字符数组
     * @param count      字符串字符个数
     * @param charRepeat 是否允许字符串中有重复字符，true为可以有重复字符
     * @return 随机的字符串
     */
    public static String randomString(char[] chars, int count, boolean charRepeat) {
        return randomString(MathUtils.getRandom(), chars, count, charRepeat);
    }

    /**
     * 随机生成字符串
     *
     * @param random     随机数发生器
     * @param count      字符串字符个数
     * @param charRepeat 是否允许字符串中有重复字符，true为可以有重复字符
     * @return 随机的字符串
     */
    public static String randomString(Random random, int count, boolean charRepeat) {
        return randomString(random, nameChars, count, charRepeat);
    }

    /**
     * 随机生成字符串
     *
     * @param count      字符串字符个数
     * @param charRepeat 是否允许字符串中有重复字符，true为可以有重复字符
     * @return 包含（A-Z）或（a-z）或（0-9）的字符串
     */
    public static String randomString(int count, boolean charRepeat) {
        return randomString(nameChars, count, charRepeat);
    }

    /**
     * 字符串替换
     *
     * @param s    源字符串
     * @param sub  被替换的字符
     * @param with 替换的字符
     * @return 替换后的新字符串
     */
    public static String replaceSub(String s, String sub, String with) {
        int c = 0;
        int i = s.indexOf(sub, c);
        if (i == -1) {
            return s;
        }
        StringBuilder buf = new StringBuilder(s.length() + with.length());
        do {
            buf.append(s, c, i);
            buf.append(with);
            c = i + sub.length();
        } while ((i = s.indexOf(sub, c)) != -1);
        if (c < s.length()) {
            buf.append(s.substring(c));
        }
        return buf.toString();
    }

    /**
     * 将src字符串中的regex字符串依次替换为args中的值
     *
     * @param src   源字符串
     * @param regex 需替换的正则表达式
     * @param args  被替换的字符串
     * @return 替换后的字符串
     */
    public static String replaceByRegex(String src, String regex, Object... args) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (!matcher.find()) {
                break;
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(arg)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 格式化字符串 <br>
     * 将src字符串中的{}字符串依次替换为args中的值
     *
     * @param src  源字符串
     * @param args 替换的新值列表
     * @return 新的字符串
     */
    public static String format(String src, Object... args) {
        return replaceByRegex(src, "\\{[ 0-9]*\\}", args);
    }

    /**
     * 将number数字转为为count位的字符串，不足count位，前面补0
     *
     * @param number 正整数
     * @param count  位数
     * @return count位字符串
     */
    public static String leftPadZero(long number, int count) {
        return String.format("%0" + count + "d", number);
    }

    /**
     * 将number数字转为为count位的字符串，不足count位，后面补0
     *
     * @param number 正整数
     * @param count  位数
     * @return count位字符串
     */
    public static String rightPadZero(long number, int count) {
        String v = String.valueOf(number);
        if (v.length() >= count) return v;
        int offset = count - v.length();
        char[] chars = new char[offset];
        Arrays.fill(chars, '0');
        return v + new String(chars);
    }

    private static void getNextVal(char[] p, int[] next) {
        int pLen = p.length;
        next[0] = -1;
        int k = -1;
        int j = 0;
        while (j < pLen - 1) {
            //p[k]表示前缀，p[j]表示后缀
            if (k == -1 || p[j] == p[k]) {
                ++j;
                ++k;
                //较之前next数组求法，改动在下面4行
                if (p[j] != p[k])
                    next[j] = k;   //之前只有这一行
                else
                    //因为不能出现p[j] = p[ next[j ]]，所以当出现时需要继续递归，k = next[k] = next[next[k]]
                    next[j] = next[k];
            } else {
                k = next[k];
            }
        }
    }

    /**
     * kmp字符串查找算法 <br>
     * 参考博文：<a href="https://www.cnblogs.com/ZuoAndFutureGirl/p/9028287.html">...</a> <br>
     * 经jdk8上测试简单英文字符串匹配搜索100000+次，性能不及String原生indexOf算法
     *
     * @param src  源字符串
     * @param pstr 搜索的字符串
     * @return 字符串索引
     */
    public static int indexOfByKmp(String src, String pstr) {
        int i = 0;
        int j = 0;
        int sLen = src.length();
        int pLen = pstr.length();
        char[] s = src.toCharArray();
        char[] p = pstr.toCharArray();
        int[] next = new int[pLen];
        getNextVal(p, next);
        while (i < sLen && j < pLen) {
            //①如果j = -1，或者当前字符匹配成功（即S[i] == P[j]），都令i++，j++
            if (j == -1 || s[i] == p[j]) {
                i++;
                j++;
            } else {
                //②如果j != -1，且当前字符匹配失败（即S[i] != P[j]），则令 i 不变，j = next[j]
                //next[j]即为j所对应的next值
                j = next[j];
            }
        }
        if (j == pLen) {
            return i - j;
        }
        return -1;
    }

    /**
     * 获取src中匹配regex正则规则的字符串集合
     *
     * @param src             源字符串
     * @param regex           正则表达式
     * @param regexMatchIndex 0表示匹配整个表达式，其它正数表示正则表达式中的括号中所标示的子表达式
     * @return 字符串集合
     */
    public static List<String> match(String src, String regex, int regexMatchIndex) {
        if (src == null || regex == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(src);
        List<String> result = null;
        while (matcher.find()) {
            if (result == null) {
                result = new ArrayList<>();
            }
            result.add(matcher.group(regexMatchIndex));
        }
        return result;
    }

    /**
     * 获取src中匹配regex正则规则的字符串集合
     *
     * @param src   源字符串
     * @param regex 正则表达式
     * @return 字符串集合
     */
    public static List<String> match(String src, String regex) {
        return match(src, regex, 0);
    }

    /**
     * 获取src中匹配{数字}正则规则的字符串集合
     *
     * @param src 源字符串
     * @return 新的字符串
     */
    public static List<String> match(String src) {
        return match(src, "\\{[0-9&]*\\}");
    }

    /**
     * 是否为数字的正则
     */
    private final static Pattern intPattern = Pattern.compile("^([-+])?[0-9]*$");

    /**
     * 判断字符串是否为纯整型数字
     *
     * @param src 待判断的字符串
     * @return true为纯整型数字
     */
    public static boolean isInteger(String src) {
        if (src == null || src.isEmpty()) {
            return false;
        }
        return intPattern.matcher(src).matches();
    }

    /**
     * 是否为数字的正则
     */
    private final static Pattern numberPattern = Pattern.compile("^([-+])?[0-9]*(\\.[0-9]*)?$");

    /**
     * 判断字符串是否为数字
     *
     * @param src 待判断的字符串
     * @return true为数字
     */
    public static boolean isNumeric(String src) {
        if (src == null || src.isEmpty()) {
            return false;
        }
        return numberPattern.matcher(src).matches();
    }

    /**
     * 判断字符串是否不为null或者""
     *
     * @param src 源字符串
     * @return true不为null或者""，false为null或者""
     */
    public static boolean isNotEmpty(String src) {
        if (src == null) {
            return false;
        }
        return !src.isEmpty();
    }

    public static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 将字符转换为16进制的数字
     *
     * @param ch    字符
     * @param index 字符数组中的索引位置
     * @return 16进制数字
     */
    private static int toDigit(final char ch, final int index) {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    private static final char[] hex_digits_numbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    /**
     * 任意字符串转数字
     *
     * @param src 字符串
     * @return 转换后的数字
     */
    public static long toNumber(final String src) {
        byte[] source = src.getBytes(StandardCharsets.UTF_8);
        String s = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(source);
            byte[] tmp = md.digest();
            char[] str = new char[16];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                //只取高位
                str[k++] = hex_digits_numbers[(byte0 >>> 4 & 0xf) % 10];
                //str[k++] = hexDigits[byte0 & 0xf];
            }
            s = new String(str);  // 换后的结果转换为字符串
        } catch (Exception ignored) {
        }
        if (s == null) {
            return 0;
        }
        return Long.parseLong(s);
    }

    /**
     * 编码字节数组为16进制字符
     *
     * @param data     字节数组
     * @param toDigits 基础编码数据
     * @return 编码后的字符数组
     */
    public static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int len = data.length;
        final char[] out = new char[len << 1];
        //两个字符构成十六进制值。
        for (int i = 0, j = 0; i < len; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * 编码字节数组为16进制字符
     *
     * @param data 字节数组
     * @return 编码后的字符数组
     */
    public static char[] encodeHex(final byte[] data) {
        return encodeHex(data, DIGITS_UPPER);
    }

    /**
     * 将16进制字符编码为字节
     *
     * @param data 字符数组
     * @return 字节数组
     */
    public static byte[] decodeHex(final char[] data) {
        final int len = data.length;
        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("字符数组长度必然为偶数。");
        }
        final byte[] out = new byte[len >> 1];
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }
        return out;
    }

    /**
     * 将16进制字符编码为字节
     *
     * @param data 字符串
     * @return 字节数组
     */
    public static byte[] decodeHex(final String data) {
        return decodeHex(data.toCharArray());
    }

    /**
     * 判断字符是否为中文字符
     *
     * @param c 字符
     * @return 为中文字符返回true，否则为false
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    /**
     * 判断字符串中是否包含中文字符
     *
     * @param src 待判断的源字符串
     * @return 包含中文字符返回true， 否则返回false
     */
    public static boolean hasChinese(String src) {
        char[] ch = src.toCharArray();
        for (char c : ch) {
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 组合列表元素为字符串
     *
     * @param list      数据列表
     * @param open      开始字符串
     * @param separator 分隔符
     * @param close     关闭字符串
     * @return 组合后的字符串
     */
    public static String group(Collection<?> list, String open, String separator, String close) {
        StringBuilder sb = new StringBuilder(open);
        int size = list.size();
        list.forEach((v) -> sb.append(v).append(separator));
        if (size > 0) {
            int len = sb.length();
            sb.delete(len - separator.length(), len);
        }
        sb.append(close);
        return sb.toString();
    }

    /**
     * 组合数组元素为字符串
     *
     * @param array     待组合的数组
     * @param open      开始字符串
     * @param separator 分隔符
     * @param close     关闭字符串
     * @return 组合后的字符串
     */
    public static String group(Object array, String open, String separator, String close) {
        if (!array.getClass().isArray()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(open);
        int size = ArrayUtils.getLength(array);
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(ArrayUtils.get(array, i));
        }
        sb.append(close);
        return sb.toString();
    }

    /**
     * 求解两字符串的最长公共子串 <br>
     * 原理：二维矩阵中，将str1的每个字符置于行首，str2的每个字符置于列首，
     * 再于矩阵中查询出连续行列值。
     *
     * @param str1 字符串
     * @param str2 字符串
     * @return 最长公共子串
     */
    public static String getMaxSubstring(String str1, String str2) {
        int row = 0;
        int col = str2.length();
        int end_index = 0;
        int max_len = 0;
        while (row < str1.length()) {
            int i = row;
            int j = col;
            int pre = 0;
            while (i < str1.length() && j < str2.length()) {
                if (str1.charAt(i) == str2.charAt(j)) {
                    pre += 1;
                    if (pre > max_len) {
                        max_len = pre;
                        end_index = i;
                    }
                } else {
                    pre = 0;
                }
                i++;
                j++;
            }
            if (col > 0) {
                col--;
            } else {
                row++;
            }
        }
        //最长子串长度即为max_len
        if (max_len > 0) {
            int start = end_index - max_len + 1;
            int end = end_index + 1;
            return str1.substring(start, end);
        }
        return null;
    }

//    public static void main(String[] args) {
//        System.out.println(getMaxSubstring("dsafjdsafdf3qewrdf", "rrqrewqfjdsafdf3qewjdlfaj"));
//        System.out.println(getMaxSubstring("ab", "b"));
//    }

}
