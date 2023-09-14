package kaka.test;

import com.kaka.notice.Message;
import com.kaka.notice.RemoteMessage;
import com.kaka.notice.RemoteMessageCache;
import com.kaka.notice.RemoteMessagePostman;
import kaka.test.util.KryoSerializer;
import kaka.test.util.Serializer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author zkpursuit
 */
public class RocketMQ extends RemoteMessagePostman {

    private static class TestRemoteMessageCache implements RemoteMessageCache {
        private final Map<String, Message> localMap = new ConcurrentHashMap<>();

        @Override
        public void add(String id, com.kaka.notice.Message message) {
            localMap.put(id, message);
        }

        @Override
        public Message get(String id) {
            return localMap.get(id);
        }

        @Override
        public com.kaka.notice.Message remove(String id) {
            return localMap.remove(id);
        }
    }

    private final String broker_address = "127.0.0.1:9876";
    private final Serializer<RemoteMessage> eventSerializer = new KryoSerializer<>();
    private MQProducer producer;

    public RocketMQ(String beforeTopic, String afterTopic) {
        super(beforeTopic, afterTopic);
        this.init();
    }

    @Override
    protected RemoteMessageCache initRemoteMessageCache() {
        return new TestRemoteMessageCache();
    }

    @Override
    protected void sendRemoteMessage(RemoteMessage remoteMessage) {
        byte[] bytes = this.eventSerializer.serialize(remoteMessage);
        try {
            String topic = remoteMessage.getWhat().toString();
            org.apache.rocketmq.common.message.Message message = new org.apache.rocketmq.common.message.Message(topic, bytes);
            this.producer.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        DefaultMQProducer producer = new DefaultMQProducer("producer-BBC");
        producer.setNamesrvAddr(broker_address);
        this.producer = producer;
        try {
            this.producer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            try {
                consume(event_topic, (byte[] bytes) -> {
                    RemoteMessage remoteMessage = eventSerializer.deserialize(bytes);
                    facade.sendMessage(remoteMessage); //这里很重要，必须调用
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                consume(event_result_topic, (byte[] bytes) -> {
                    RemoteMessage remoteMessage = eventSerializer.deserialize(bytes);
                    facade.sendMessage(remoteMessage); //这里很重要，必须调用
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void consume(String topic, Consumer<byte[]> callback) {
        DefaultMQPushConsumer mqPushConsumer = new DefaultMQPushConsumer("remote-group-" + topic);
        mqPushConsumer.setNamesrvAddr(broker_address);
        try {
            mqPushConsumer.subscribe(topic, "*");
            mqPushConsumer.registerMessageListener(new MessageListenerOrderly() {
                @Override
                public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                    for (MessageExt msg : msgs) {
                        byte[] bytes = msg.getBody();
                        callback.accept(bytes);
                    }
                    return ConsumeOrderlyStatus.SUCCESS;
                }
            });
            mqPushConsumer.start();
        } catch (MQClientException e) {
            throw new RuntimeException(e);
        }
    }

}
