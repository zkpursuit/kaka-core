package com.kaka.notice;

import com.kaka.util.StringUtils;

import java.util.Map;

/**
 * 远端事件处理器 <br>
 * 目前一个事件仅支持获取一次处理结果，即广播状态，事件处理结果以最先获得为准，后面获得的处理结果直接丢弃 <br>
 * 分布式环境需谨慎使用事件远端广播，因广播事件时所有拥有事件逻辑处理器{@link Command}的远程服务都将处理事件逻辑 <br>
 *
 * @author zkpursuit
 */
abstract public class RemoteMessagePostman {

    protected Facade facade;
    protected final RemoteMessageCache remoteMessageCache;
    protected final String event_topic; //收到远端事件数据后处理事件
    protected final String event_result_topic; //收到远端事件处理后的结果给本地缓存的事件赋值处理结果

    /**
     * 处理远端事件
     */
    private static class RemoteMessageMediator extends Mediator {

        private final RemoteMessagePostman postman;

        RemoteMessageMediator(RemoteMessagePostman postman) {
            super("remote_message_mediator_" + StringUtils.randomString(16, true));
            this.postman = postman;
        }

        @Override
        public void handleMessage(Message msg) {
            RemoteMessage remoteMessage = (RemoteMessage) msg;
            String cmd = msg.getWhat().toString();
            if (this.postman.event_topic.equals(cmd)) {
                //接收远端事件并执行事件处理逻辑
                String id = remoteMessage.id;
                Message localEventMessage = this.postman.remoteMessageCache.remove(id);
                if (localEventMessage != null) {
                    //远端事件接收方如果与事件发送方同源则直接处理
                    this.sendMessage(localEventMessage);
                    return;
                }
                Message remoteEventMessage = (Message) remoteMessage.getBody();
                this.sendMessage(remoteEventMessage);
                if (remoteEventMessage.resultMap != null && !remoteEventMessage.resultMap.isEmpty()) {
                    remoteMessage.what = this.postman.event_result_topic;
                    this.postman.sendRemoteMessage(remoteMessage);
                }
                return;
            }
            if (this.postman.event_result_topic.equals(cmd)) {
                //事件消息发送方接收到来自远端的处理结果
                String id = remoteMessage.id;
                Message remoteEventMessage = (Message) remoteMessage.getBody();
                Message localEventMessage = this.postman.remoteMessageCache.remove(id);
                if (localEventMessage == null) {
                    this.getFacade().sendMessage(new Message("remote_processed_message", remoteEventMessage));
                    return;
                }
                Map<Object, IResult> eventResultMap = remoteEventMessage.resultMap;
                if (eventResultMap == null) return;
                eventResultMap.forEach((Object key, IResult result) -> {
                    String keyStr = String.valueOf(key);
                    IResult localResult = localEventMessage.getResult(keyStr);
                    if (localResult instanceof AsynResult) {
                        localResult.set(result.get());
                    } else if (result instanceof CallbackResult) {
                        localEventMessage.setResult(keyStr, result);
                        localEventMessage.callback(keyStr);
                    }
                });
            }
        }

        @Override
        public Object[] listMessageInterests() {
            return new Object[]{this.postman.event_topic, this.postman.event_result_topic};
        }
    }

    /**
     * 构造方法
     *
     * @param event_topic        收到远端事件数据后处理事件
     * @param event_result_topic 收到远端事件处理后的结果给本地缓存的事件赋值处理结果
     */
    public RemoteMessagePostman(String event_topic, String event_result_topic) {
        this.remoteMessageCache = this.initRemoteMessageCache();
        this.event_topic = event_topic;
        this.event_result_topic = event_result_topic;
    }

    /**
     * 包内赋值事件总线
     *
     * @param facade 事件总线，事件分发器
     */
    void setFacade(Facade facade) {
        this.facade = facade;
        this.facade.registerMediator(new RemoteMessageMediator(this));
    }

    /**
     * 初始化事件消息处理器缓存
     *
     * @return 事件消息处理器缓存
     */
    abstract protected RemoteMessageCache initRemoteMessageCache();

    /**
     * 发送远端消息
     *
     * @param remoteMessage 事件消息
     */
    protected abstract void sendRemoteMessage(RemoteMessage remoteMessage);

}
