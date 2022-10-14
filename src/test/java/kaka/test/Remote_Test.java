package kaka.test;

import com.kaka.Startup;
import com.kaka.notice.*;

import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * @author zkpursuit
 */
public class Remote_Test extends Startup {

    public static void main(String[] args) throws Exception {
        Facade facade = FacadeFactory.getFacade();
        Remote_Test test = new Remote_Test();
        test.scan("kaka.test.unit");
        facade.initThreadPool(Executors.newFixedThreadPool(2));

        //以下通过ActiveMQ消息队列消费处理事件，并获得事件处理结果
        facade.initRemoteMessagePostman(new ActiveMQ("event_exec_before", "event_exec_after")); //此行全局一次设定
        //facade.initRemoteMessagePostman(new RocketMQ("event_exec_before", "event_exec_after"));

        Message message = new Message("20000", "让ResultCommand接收执行");
        IResult<String> result4 = message.setResult("ResultMsg", new AsynLatchResult<>()); //AsynLatchResult可用AsynResult替代
        facade.sendRemoteMessage(message);
//        try {
//            System.out.println("消息队列消费处理事件结果：" + ((AsynLatchResult) result4).get(5, TimeUnit.SECONDS));
//        } catch (TimeoutException ex) {
//            System.out.println("获取结果超时");
//        }
        System.out.println("消息队列消费处理事件结果：" + result4.get()); //一直等待结果
        facade.sendRemoteMessage(new Message("40000", "", (IResult<Object> result) -> {
            String clasz = ((CallbackResult<Object>) result).eventHandlerClass;
            StringBuilder sb = new StringBuilder("消息队列消费处理事件结果异步回调：\t" + clasz + "\t");
            Object resultObj = result.get();
            if (resultObj instanceof Object[]) {
                Object[] ps = (Object[]) resultObj;
                sb.append(Arrays.toString(ps));
            } else {
                sb.append(resultObj);
            }
            System.out.println(sb);
        }));
    }

}
