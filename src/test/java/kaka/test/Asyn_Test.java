package kaka.test;

import com.kaka.Startup;
import com.kaka.notice.*;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 异步使用范例
 *
 * @author zkpursuit
 */
public class Asyn_Test extends Startup {

    public static void main(String[] args) {
        Facade facade = FacadeFactory.getFacade();
        Asyn_Test test = new Asyn_Test();
        test.scan("kaka.test.unit");
        facade.initThreadPool(Executors.newFixedThreadPool(2));
        //同步发送事件通知
        facade.sendMessage(new Message("1000", "让MyCommand接收执行"));
        //简单的异步发送事件通知
        facade.sendMessage(new Message("2000", "让MyMediator和MyCommand接收执行"), true);

        /*
            1、以下为测试发送事件通知后获得事件处理器的处理结果。
            2、一般情况我们不一定需要此功能，为了尽可能的减少对象创建，故而
        在需要使用此功能时手动创建AsynResult或者SyncResult对象。
            3、我们应该尽可能的使用事件模式代替，比如事件处理器处理完成后再次
        调用sendMessage向外派发事件，分散到其它事件处理器中处理，而不是等待处
        理结果。
            4、异步future模式获取事件处理结果其本质是利用wait、notify（notifyAll）
        实现，而使用事件模式则无需调用wait让线程中断等待。
         */
        //获取异步处理结果
        Message asynMsg = new Message("10000", "让ResultCommand接收执行");
        //由于事件通知为广播模式，故而必须为执行结果进行命名标识唯一性
        IResult<String> result0 = asynMsg.setResult("ResultMsg", new AsynResult<>(12000));
        facade.sendMessage(asynMsg, true); //异步发送事件通知
        System.out.println(result0.get());

        //获取同步执行结果
        Message syncMsg = new Message("20000", "让ResultCommand接收执行");
        //由于事件通知为广播模式，故而必须为执行结果进行命名标识唯一性
        IResult<String> result1 = syncMsg.setResult("ResultMsg", new SyncResult<>());
        facade.sendMessage(syncMsg, false);  //同步发送事件通知
        System.out.println(result1.get());

        //另一种异步处理方式,同步派发事件，事件处理器中使用FutureTask及线程异步获取执行结果
        Message syncMsg1 = new Message("30000", "让FutureCommand接收执行");
        IResult<String> result2 = syncMsg1.setResult("ResultMsg", new SyncResult<>());
        facade.sendMessage(syncMsg1, false); //同步发送事件通知
        System.out.println(result2.get());

        //哈哈，异步中的异步，其实没必要
        Message syncMsg2 = new Message("30000", "让FutureCommand接收执行");
        IResult<String> result3 = syncMsg2.setResult("ResultMsg", new AsynResult<>());
        facade.sendMessage(syncMsg2, true); //异步发送事件通知
        System.out.println(result3.get());

        //基于事件模拟切面编程，仅支持Command
        facade.sendMessage(new Message("40000"), true);

        //异步回调获取事件执行结果
        facade.sendMessage(new Message("50000", "", (IResult<Object> result) -> {
            String clasz = ((CallbackResult<Object>) result).eventHanderClass;
            StringBuilder sb = new StringBuilder("异步回调：\t" + clasz + "\t");
            Object resultObj = result.get();
            if (resultObj instanceof Object[]) {
                Object[] ps = (Object[]) resultObj;
                sb.append(Arrays.toString(ps));
            } else {
                sb.append(resultObj);
            }
            System.out.println(sb);
        }), true);

        facade.initScheduleThreadPool(Executors.newScheduledThreadPool(2));
        long c = System.currentTimeMillis();
        Scheduler scheduler = Scheduler.create("com/test/units")
                .startTime(c + 3000) //3秒后开始执行
                .endTime(c + 7000) //调度执行结束时间点
                .interval(2000, TimeUnit.MILLISECONDS) //执行间隔
                .repeat(5); //执行次数
        //此处的执行次数为5次，但因执行到某次时超出设置的结束时间，故而实际次数将少于5次
        facade.sendMessage(new Message("1000", "让MyCommand接收执行"), scheduler);

        //以下通过ActiveMQ消息队列消费处理事件，并获得事件处理结果
        facade.initRemoteMessageQueue(new ActiveMQ("event_exec_before", "event_exec_after")); //此行全局一次设定
        Message message = new Message("20000", "让MyCommand接收执行");
        IResult<String> result4 = message.setResult("ResultMsg", new AsynResult<>(5000));
        facade.sendMessageByQueue(message);
        System.out.println("消息队列消费处理事件结果：" + result4.get());

        facade.sendMessageByQueue(new Message("40000", "", (IResult<Object> result) -> {
            String clasz = ((CallbackResult<Object>) result).eventHanderClass;
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
