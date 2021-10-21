package kaka.test.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;

/**
 * 序列化器
 *
 * @param <T>
 */
public class KryoSerializer<T> implements Serializer<T> {

    private static final Pool<Kryo> pool = new Pool<Kryo>(true, false, 100) {
        protected Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setReferences(true); //默认值就是 true，添加此行的目的是为了提醒维护者，不要改变这个配置
            //不强制要求注册类（注册行为无法保证多个 JVM 内同一个类的注册编号相同；而且业务系统中大量的 Class 也难以一一注册）
            kryo.setRegistrationRequired(false); //默认值就是 false，添加此行的目的是为了提醒维护者，不要改变这个配置

            //新增字段必须包含注解@Since(版本号)，向后兼容，即可以增加字段；不支持向前兼容，即不能删除、重命名、更改字段类型
            //kryo.setDefaultSerializer(VersionFieldSerializer.class);

            //仅序列化注解Tag(序号)所标注的字段，向后兼容，即可以增加字段；
            //可以重命名字段，但不能直接删除，可以添加Deprecated注解标注忽略字段，且Tag注解也不能删除，其中的序号也不能更改
            //kryo.setDefaultSerializer(TaggedFieldSerializer.class);
            //设置忽略未知字段，提供TaggedFieldSerializer序列化器的向前兼容
            //kryo.getTaggedFieldSerializerConfig().setSkipUnknownTags(true);

            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

            //兼容模式的序列化器，包括序列化字段名，故而序列化后的字节数量将会增大。
            //当setReferences为true时，删除字段有可能引发错误。
            kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
            //设置字段缓存策略为扩展，处理子类中某字段与父类的某字段具有相同的字段名
            //kryo.getFieldSerializerConfig().setCachedFieldNameStrategy(FieldSerializer.CachedFieldNameStrategy.EXTENDED);
            return kryo;
        }
    };

    Kryo obtainKryo() {
        return pool.obtain();
    }

    void releaseKryo(Kryo kryo) {
        pool.free(kryo);
    }

    @Override
    public byte[] serialize(T obj) {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);

        Kryo kryo = obtainKryo();

        kryo.writeClassAndObject(output, obj);
        output.flush();
        releaseKryo(kryo);

        byte[] bytes = byteArrayOutputStream.toByteArray();
        output.close();

        return bytes;
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);

        Kryo kryo = obtainKryo();
        Object obj = kryo.readClassAndObject(input);
        releaseKryo(kryo);
        input.close();

        return (T) obj;
    }

}
