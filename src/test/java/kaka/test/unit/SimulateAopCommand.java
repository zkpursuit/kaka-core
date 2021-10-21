package kaka.test.unit;

import com.kaka.notice.Command;
import com.kaka.notice.IResult;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

/**
 * 模拟切面
 */
@Handler(cmd = "40000", type = String.class, priority = 2)
public class SimulateAopCommand extends Command {
    @Override
    public void execute(Message msg) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Aop业务执行");
        IResult result = msg.getResult("ResultMsg");
        if(result != null) {
            result.set("Aop业务执行结果");
        }
        returnCallbackResult("我爱我家");
    }
}
