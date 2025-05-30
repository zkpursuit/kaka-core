package com.kaka.util;

import java.net.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 *
 * @author zkpursuit
 */
public final class Tool {

    private static final String os = System.getProperty("os.name").toLowerCase();

    public static boolean isLinux() {
        return os.contains("linux");
    }

    public static boolean isWindows() {
        return os.contains("windows");
    }

    /**
     * 根据时间和本机mac地址生成唯一标识字符串
     *
     * @return 唯一字符串
     */
    public static String uuid() {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replaceAll("-", "");
        return uuid;
    }

    private static final byte[][] invalidMacs = {
            {0x00, 0x05, 0x69},             // VMWare
            {0x00, 0x1C, 0x14},             // VMWare
            {0x00, 0x0C, 0x29},             // VMWare
            {0x00, 0x50, 0x56},             // VMWare
            {0x08, 0x00, 0x27},             // Virtualbox
            {0x0A, 0x00, 0x27},             // Virtualbox
            {0x00, 0x03, (byte) 0xFF},       // Virtual-PC
            {0x00, 0x15, 0x5D}              // Hyper-V
    };

    private static boolean isVMMac(byte[] mac) {
        if (null == mac) {
            return false;
        }
        for (byte[] invalid : invalidMacs) {
            if (invalid[0] == mac[0] && invalid[1] == mac[1] && invalid[2] == mac[2]) {
                return true;
            }
        }
        return false;
    }

    public enum Filter implements Predicate<NetworkInterface> {
        /**
         * 过滤器: 所有网卡
         */
        ALL,
        /**
         * 过滤器: 在线设备,see also {@link NetworkInterface#isUp()}
         */
        UP,
        /**
         * 过滤器: 虚拟接口,see also {@link NetworkInterface#isVirtual()}
         */
        VIRTUAL,
        /**
         * 过滤器:LOOPBACK, see also {@link NetworkInterface#isLoopback()}
         */
        LOOPBACK,
        /**
         * 过滤器:物理网卡
         */
        PHYICAL_ONLY;

        @Override
        public boolean test(NetworkInterface input) {
            if (null == input) {
                return false;
            }
            try {
                byte[] hardwareAddress;
                switch (this) {
                    case UP:
                        return input.isUp();
                    case VIRTUAL:
                        return input.isVirtual();
                    case LOOPBACK:
                        return input.isLoopback();
                    case PHYICAL_ONLY:
                        hardwareAddress = input.getHardwareAddress();
                        return null != hardwareAddress && hardwareAddress.length > 0 && !input.isVirtual() && !isVMMac(hardwareAddress);
                    case ALL:
                    default:
                        return true;
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Set<NetworkInterface> getNetworkInterfaces(Filter... filters) {
        if (filters.length == 0) {
            filters = new Filter[]{Filter.ALL};
        }
        try {
            Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
            Set<NetworkInterface> sets = new HashSet<>();
            NetworkInterface ni;
            while (enums.hasMoreElements() && (ni = enums.nextElement()) != null) {
                for (Filter filter : filters) {
                    if (filter.test(ni)) {
                        sets.add(ni);
                    }
                }
            }
            return sets;
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMacAddress() throws SocketException {
        Set<NetworkInterface> nis = getNetworkInterfaces();
        for (NetworkInterface ni : nis) {
            for (InterfaceAddress address : ni.getInterfaceAddresses()) {
                InetAddress inetAddress = address.getAddress();
                if (inetAddress instanceof Inet4Address inet4Address) {
                    String ip = inet4Address.getHostAddress();
                    if ("127.0.0.1".equals(ip)) continue;
                    return ip;
                }
            }
        }
        for (NetworkInterface ni : nis) {
            byte[] bytes = ni.getHardwareAddress();
            if (bytes != null) {
                return new String(StringUtils.encodeHex(bytes));
            }
        }
        return null;
    }

    public static final String LOCAL_MAC_ADDRESS;

    static {
        String LOCAL_MAC_ADDRESS1;
        try {
            LOCAL_MAC_ADDRESS1 = getMacAddress();
        } catch (SocketException e) {
            LOCAL_MAC_ADDRESS1 = null;
        }
        LOCAL_MAC_ADDRESS = LOCAL_MAC_ADDRESS1;
    }

    /**
     * 获取address网络地址主机的mac地址
     *
     * @param address 网络地址
     * @return mac地址
     */
    public static String getMacAddress(InetAddress address) {
        String mac;
        StringBuilder sb = new StringBuilder();
        try {
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            if (ni == null) {
                return null;
            }
            byte[] macs = ni.getHardwareAddress();
            if (macs == null) {
                return null;
            }
            for (byte b : macs) {
                mac = Integer.toHexString(b & 0xFF);
                if (mac.length() == 1) {
                    mac = '0' + mac;
                }
                sb.append(mac);
            }
        } catch (SocketException e) {
        }
        mac = sb.toString();
        mac = mac.substring(0, mac.length() - 1);
        return mac;
    }

    /**
     * 获取本机IP地址列表
     *
     * @return 本机IP地址列表
     */
    public static List<InetAddress> getLocalInetAddressList() {
        List<InetAddress> list = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                //System.out.println(netInterface.getName());
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        //System.out.println("本机的IP = " + ip.getHostAddress());
                        list.add(ip);
                    }
                }
            }
        } catch (SocketException ignored) {
        }
        return list;
    }

    /**
     * 设置value二进制表示的index位，当state为true是置为1，false置为0
     *
     * @param value 十进制数字
     * @param index bit位索引，从右至左，以0为起始
     * @param state 当state为true是置为1，false置为0
     * @return 设置后的十进制数字
     */
    public static int setStateAtBitIndex(int value, int index, boolean state) {
        return state ? (value | (1 << index)) : (value & (~(1 << index)));
    }

    /**
     * 获取value二进制表示的index位状态，true表示index位为1，false表示index位为0
     *
     * @param value 十进制数字
     * @param index bit位索引，从右至左，以0为起始
     * @return true表示index位为1，false表示index位为0
     */
    public static boolean getStateAtBitIndex(int value, int index) {
        return (value & (1 << index)) != 0;
    }

    /**
     * 设置value二进制表示的index位，当state为true是置为1，false置为0
     *
     * @param value 十进制数字
     * @param index bit位索引，从右至左，以0为起始
     * @param state 当state为true是置为1，false置为0
     * @return 设置后的十进制数字
     */
    public static long setStateAtBitIndex(long value, int index, boolean state) {
        return state ? (value | (1L << index)) : (value & (~(1L << index)));
    }

    /**
     * 获取value二进制表示的index位状态，true表示index位为1，false表示index位为0
     *
     * @param value 十进制数字
     * @param index bit位索引，从右至左，以0为起始
     * @return true表示index位为1，false表示index位为0
     */
    public static boolean getStateAtBitIndex(long value, int index) {
        return (value & (1L << index)) != 0;
    }

    /**
     * 将两个32位整数合并为为一个64位整数
     *
     * @param value1 32位整数
     * @param value2 32位整数
     * @return 64位整数
     */
    public static long merge(int value1, int value2) {
        return (((long) value1) << 32) + value2;
    }

    /**
     * 从一个64位整数分离出两个32位整数
     *
     * @param value 64位整数
     * @return 分离后的数字
     */
    public static int[] split(long value) {
        return new int[]{(int) (value >> 32), (int) ((value << 32) >> 32)};
    }

    /**
     * 从一个64位整数分离出一个32位整数
     *
     * @param value 64位整数
     * @param first 是否为第一个整数
     * @return 32位整数
     */
    public static int split(long value, boolean first) {
        return first ? (int) (value >> 32) : (int) ((value << 32) >> 32);
    }

    /**
     * 将两个16位整数合并为为一个32位整数
     *
     * @param value1 16位整数
     * @param value2 16位整数
     * @return 32位整数
     */
    public static int merge(short value1, short value2) {
        return (((int) value1) << 16) + value2;
    }

    /**
     * 从一个32位整数分离出两个16位整数
     *
     * @param value 32位整数
     * @return 分离后的数字
     */
    public static short[] split(int value) {
        return new short[]{(short) (value >> 16), (short) ((value << 16) >> 16)};
    }

    /**
     * 从参数数字中析出16位整数
     *
     * @param value 32位整数
     * @param first 是否为第一个整数
     * @return 16位整数
     */
    public static short split(int value, boolean first) {
        return first ? (short) (value >> 16) : (short) ((value << 16) >> 16);
    }

    /**
     * 判断身份证上的年龄是否达到ageYear年龄
     *
     * @param idCard  身份证号码
     * @param ageYear 周岁
     * @return 满足为true
     */
    public static boolean fullYearOfLife(String idCard, int ageYear) {
        if (idCard == null || idCard.isEmpty()) {
            return false;
        }
        String birthdayStr = idCard.substring(6, 14);
        int year = Integer.parseInt(birthdayStr.substring(0, 4));
        int month = Integer.parseInt(birthdayStr.substring(4, 6));
        int day = Integer.parseInt(birthdayStr.substring(6));
        LocalDate birthdayDate = LocalDate.of(year, month, day);
        LocalDate currentDate = LocalDate.now();
        int intervalYear = (int) ChronoUnit.YEARS.between(birthdayDate, currentDate);
        return intervalYear >= ageYear;
    }

    //IP正则
    private final static String ipRegex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)($|(?!\\.$)\\.)){4}$";
    //private static String ipRegex = "((25[0-5]|2[0-4]//d|1//d{2}|[1-9]//d|//d)//.){3}(25[0-5]|2[0-4]//d|1//d{2}|[1-9]//d|//d)";

    /**
     * 判断是否为IP地址
     *
     * @param addr ip地址表示的字符串
     * @return true为ip地址
     */
    public static boolean isIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15) {
            return false;
        }
        Pattern pat = Pattern.compile(ipRegex);
        Matcher mat = pat.matcher(addr);
        return mat.find();
    }

    /**
     * 判断ip是否在某网段内
     *
     * @param ip      需判断的IP
     * @param startIp 起始网段IP
     * @param endIp   结束网段IP
     * @return true在网段内，否在不在网段内
     */
    public static boolean ipIsInNetworkSegment(String ip, String startIp, String endIp) {
        if (ip == null) {
            throw new NullPointerException("IP不能为空！");
        }
        if (startIp == null) {
            throw new NullPointerException("IP段不能为空！");
        }
        if (endIp == null) {
            throw new NullPointerException("IP段不能为空！");
        }
        ip = ip.trim();
        if (!isIP(ip)) {
            return false;
        }
        startIp = startIp.trim();
        if (!isIP(startIp)) {
            return false;
        }
        endIp = endIp.trim();
        if (!isIP(endIp)) {
            return false;
        }
        String[] sips = startIp.split("\\.");
        String[] sipe = endIp.split("\\.");
        String[] sipt = ip.split("\\.");
        long ips = 0L, ipe = 0L, ipt = 0L;
        for (int i = 0; i < 4; ++i) {
            ips = ips << 8 | Integer.parseInt(sips[i]);
            ipe = ipe << 8 | Integer.parseInt(sipe[i]);
            ipt = ipt << 8 | Integer.parseInt(sipt[i]);
        }
        if (ips > ipe) {
            long t = ips;
            ips = ipe;
            ipe = t;
        }
        return ips <= ipt && ipt <= ipe;
    }

    /**
     * 判断ip是否在某网段内
     *
     * @param ip        需判断的IP
     * @param ipSegment IP网段，以“-”或“,”或“:”连接的两个IP地址
     * @return true在网段内，否在不在网段内
     */
    public static boolean ipIsInNetworkSegment(String ip, String ipSegment) {
        if (ipSegment == null) {
            throw new NullPointerException("IP段不能为空！");
        }
        if (ipSegment.isEmpty()) {
            throw new NullPointerException("IP段不能为空！");
        }
        String[] ips = ipSegment.split("[-,;]");
        return ipIsInNetworkSegment(ip, ips[0], ips[1]);
    }

    /**
     * IPv4转数字
     *
     * @param ip IPv4地址
     * @return IPv4地址的数字表示
     */
    public static long ipv4ToNumber(String ip) {
        boolean bool = isIP(ip);
        if (!bool) {
            throw new IllegalArgumentException(ip + " 无效的IP地址");
        }
        String[] parts = ip.split("\\.");
        long sip1 = Long.parseLong(parts[0]);
        long sip2 = Long.parseLong(parts[1]);
        long sip3 = Long.parseLong(parts[2]);
        long sip4 = Long.parseLong(parts[3]);
        long result = sip1 << 24;
        result += sip2 << 16;
        result += sip3 << 8;
        result += sip4;
        return result;
    }

    /**
     * 数字转IPv4地址
     *
     * @param ip IPv4地址的数字表示
     * @return IPv4地址
     */
    public static String numberToIpv4(long ip) {
        long ip1 = ip & 0xFF000000L;
        ip1 = ip1 >> 24;
        long ip2 = ip & 0x00FF0000;
        ip2 = ip2 >> 16;
        long ip3 = ip & 0x0000FF00;
        ip3 = ip3 >> 8;
        long ip4 = ip & 0x000000FF;
        return String.valueOf(ip1) + '.' + ip2 + '.' + ip3 + '.' + ip4;
    }

    /**
     * 判断字符串是否为url地址
     *
     * @param url 字符串
     * @return true表示字符串参数为url地址
     */
    public static boolean isURL(String url) {
        if (url == null) {
            return false;
        }
        url = url.trim();
        if (url.isEmpty()) {
            return false;
        }
        String strRegex = "^((https|http|ftp|rtsp|mms)?://)"
                + "?(([0-9a-z_!~*\"().&=+$%-]+: )?[0-9a-z_!~*\"().&=+$%-]+@)?"//ftp的user@
                + "(([0-9]{1,3}.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
                + "|" // 允许IP和DOMAIN（域名）
                + "([0-9a-z_!~*\"()-]+.)*" // 域名- www.
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]." // 二级域名
                + "[a-z]{2,6})" // first level domain- .com or .museum
                + "(:[0-9]{1,4})?" // 端口- :80
                + "((/?)|" // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*\"().;?:@&=+$,%#-]+)+/?)$";
        Pattern pattern = Pattern.compile(strRegex);
        Matcher m = pattern.matcher(url);
        return m.find();
    }

    private Tool() {
    }
}
