package com.kaka.util;

import java.nio.charset.StandardCharsets;

/**
 * 字符编码常量 <br>
 * 已废弃，可能在后续版本中移除，另可参见 {@link StandardCharsets}
 *
 * @author zkpursuit
 */
@Deprecated
public class Charsets {

    /**
     * UTF-8字符集
     */
    public static final java.nio.charset.Charset utf8 = StandardCharsets.UTF_8;

    /**
     * GBK字符集
     */
    public static final java.nio.charset.Charset gbk = java.nio.charset.Charset.forName("GBK");

    /**
     * GB2312字符集
     */
    public static final java.nio.charset.Charset gb2312 = java.nio.charset.Charset.forName("GB2312");
    
    /**
     * ISO-8859-1字符集
     */
    public static final java.nio.charset.Charset iso_8859_1 = StandardCharsets.ISO_8859_1;
    
    /**
     * 系统默认字符集
     */
    public static final java.nio.charset.Charset sys = java.nio.charset.Charset.defaultCharset();

    /**
     * 私有构造，不允许外部实例化
     */
    private Charsets() {
    }

}
