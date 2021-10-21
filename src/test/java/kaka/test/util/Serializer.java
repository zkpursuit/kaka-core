package kaka.test.util;

/**
 * 序列化器接口
 *
 * @param <T>
 */
public interface Serializer<T> {

    /**
     * 将对象序列化为字节数组
     *
     * @param obj 需要序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(T obj);

    /**
     * 将字节数组反序列化为对象
     *
     * @param bytes 字节数组
     * @return 反序列化后的对象
     */
    T deserialize(byte[] bytes);

}
