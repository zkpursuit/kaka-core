package kaka.test.util;

import java.io.*;

/**
 * jdk自带序列化器
 *
 * @param <T>
 */
public class JavaSerializer<T> implements Serializer<T> {

    public static final int BYTE_ARRAY_OUTPUT_STREAM_SIZE = 128;

    @Override
    public byte[] serialize(T obj) {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BYTE_ARRAY_OUTPUT_STREAM_SIZE);
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return byteStream.toByteArray();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteStream);
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new Error(e);
        }
    }

}
