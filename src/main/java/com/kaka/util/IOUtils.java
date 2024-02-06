package com.kaka.util;

import java.io.*;
import java.util.function.Consumer;

/**
 * IO流相关工具集
 *
 * @author zkpursuit
 */
public class IOUtils {

    public static final int bufferSize = 4 * 1024;
    public static final int EOF = -1;

    /**
     * 从输入流中读取字节放入目标输出流
     *
     * @param in     输入流
     * @param output 目标输出流
     * @return 字节数组
     * @throws IOException 数据流访问异常
     */
    public static long readBytes(final InputStream in, final OutputStream output) throws IOException {
        if (in == null) {
            return -1;
        }
        byte[] buffer = new byte[bufferSize];
        long count = 0;
        int n;
        while (EOF != (n = in.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * 从输入流中读取字节数据
     *
     * @param in 输入流
     * @return 字节数组
     * @throws IOException 数据流访问异常
     */
    public static byte[] readBytes(final InputStream in) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            readBytes(in, output);
            return output.toByteArray();
        }
    }

    /**
     * 将InputStream输入流转换为字符串
     *
     * @param is      输入流
     * @param charset 字符编码
     * @return 字符串
     * @throws IOException 数据流访问异常
     */
    public static String toString(InputStream is, String charset) throws IOException {
        byte[] bytes = readBytes(is);
        return new String(bytes, 0, bytes.length, charset);
    }

    /**
     * 将InputStream输入流转换为字符串
     *
     * @param is      输入字节流
     * @param charset 字符编码
     * @return 将输入字节流转换为字符串
     * @throws IOException 数据流访问异常
     */
    public static String toString(InputStream is, java.nio.charset.Charset charset) throws IOException {
        byte[] bytes = readBytes(is);
        return new String(bytes, 0, bytes.length, charset);
    }

    /**
     * 转换输入字节流为字符流
     *
     * @param is      输入字节流
     * @param charset 字符编码
     * @return 输入字符流
     * @throws UnsupportedEncodingException 编码错误
     */
    public static BufferedReader toBufferedReader(InputStream is, final String charset) throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(is, charset));
    }

    /**
     * 转换输入字节流为字符流
     *
     * @param is      输入字节流
     * @param charset 字符编码
     * @return 输入字符流
     * @throws UnsupportedEncodingException 编码错误
     */
    public static BufferedReader toBufferedReader(InputStream is, final java.nio.charset.Charset charset) throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(is, charset));
    }

    /**
     * 读取数据中的行
     *
     * @param reader 输入字符流
     * @param action 字符串行数据访问器
     * @throws IOException 数据流访问异常
     */
    public static void readLines(final BufferedReader reader, Consumer<String> action) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            action.accept(line);
            line = reader.readLine();
        }
    }

}
