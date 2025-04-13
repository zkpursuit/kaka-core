package kaka.test;

import com.kaka.Startup;
import com.kaka.notice.AsynResult;
import com.kaka.notice.Facade;
import com.kaka.notice.FacadeFactory;
import com.kaka.notice.Message;
import com.kaka.util.MathUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 同步使用范例
 *
 * @author zkpursuit
 */
public class Sync_Test extends Startup {

    public static void main(String[] args) throws Exception {
        Facade facade = FacadeFactory.getFacade();
        Sync_Test test = new Sync_Test();
        test.scan("kaka.test.unit");
        Thread.sleep(1000);
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            facade.sendMessage(new Message("1000", "让MyCommand接收执行"), Message.ExecuteType.ASYN_VIRTUAL_THREAD);
        }
        System.out.println(System.currentTimeMillis() - t1);

        t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            facade.sendMessage(new Message("1000", "让MyCommand接收执行"), Message.ExecuteType.ASYN_VIRTUAL_THREAD);
        }
        System.out.println(System.currentTimeMillis() - t1);
//        facade.sendMessage(new Message("1000", "让MyCommand接收执行"));
//        facade.sendMessage(new Message("2000", "让MyMediator和MyCommand接收执行"));
//        AsynResult<String> result = new AsynResult<>();
//        result.get(5, TimeUnit.SECONDS);
//        new Thread(() -> {result.set("abc");}).start();
//
//        System.out.println(result.get());
//        for (int i = 0; i < 100; i++) {
//            System.out.println(MathUtils.random(1, 2));
//        }
//        facade.addListener("1000", (msg, _facade) -> {
//            System.out.println("我执行了" + "==>>>" + msg.getBody());
//        });
//        facade.sendMessage(new Message("1000", "给谁执行？"));
    }

}
