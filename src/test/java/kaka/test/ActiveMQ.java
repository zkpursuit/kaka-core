package kaka.test;

import com.kaka.notice.RemoteMessageQueue;
import kaka.test.util.KryoSerializer;
import kaka.test.util.Serializer;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 本类仅为测试用例，ActiveMQ消息队列的访问实现代码是否为最优不做考虑，在此仅为范例参考
 */
public class ActiveMQ extends RemoteMessageQueue {

    private static class TestLocalMessageCache implements LocalMessageCache {
        private final Map<String, com.kaka.notice.Message> localMap = new ConcurrentHashMap<>();

        @Override
        public void add(String id, com.kaka.notice.Message message) {
            localMap.put(id, message);
        }

        @Override
        public com.kaka.notice.Message remove(String id) {
            return localMap.remove(id);
        }
    }

    private static final String BROKER_URL = "tcp://127.0.0.1:61616";
    private final ActiveMQConnectionFactory activeMQConnectionFactory;
    private final Serializer<MessageWrap> eventSerializer = new KryoSerializer<>();

    public ActiveMQ(String beforeTopic, String afterTopic) {
        super(beforeTopic, afterTopic);
        this.activeMQConnectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, BROKER_URL);
        this.init();
    }

    @Override
    protected LocalMessageCache initLocalMessageCache() {
        return new TestLocalMessageCache();
    }

    @Override
    protected void publishEventMessage(MessageWrap msgWrap, String topic) {
        byte[] bytes = this.eventSerializer.serialize(msgWrap);
        try {
            Connection connection = activeMQConnectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createQueue(topic));
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(bytes);
            producer.send(bytesMessage);
            producer.close();
            connection.close();
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        new Thread(() -> {
            try {
                consume(beforeTopic, (byte[] bytes) -> {
                    MessageWrap remoteMsgWrap = eventSerializer.deserialize(bytes);
                    receivedBeforeEventMessage(remoteMsgWrap); //这里很重要，必须调用
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                consume(afterTopic, (byte[] bytes) -> {
                    MessageWrap remoteMsgWrap = eventSerializer.deserialize(bytes);
                    receivedAfterEventMessage(remoteMsgWrap); //这里很重要，必须调用
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void consume(String topic, Consumer<byte[]> callback) throws Exception {
        Connection connection = this.activeMQConnectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(topic);
        MessageConsumer consumer = session.createConsumer(destination);
        while (true) {
            Message message = consumer.receive();
            if (null == message) break;
            if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                try {
                    int byteSize = (int) bytesMessage.getBodyLength();
                    byte[] bytes = new byte[byteSize];
                    bytesMessage.readBytes(bytes);
                    callback.accept(bytes);
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
        consumer.close();
        session.close();
        connection.close();
    }
}
