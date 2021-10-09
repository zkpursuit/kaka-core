package kaka.test.unit;

import com.kaka.notice.Command;
import com.kaka.notice.IResult;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

/**
 * 模拟切面，执行后
 */
@Handler(cmd = "40000", type = String.class, priority = 3)
public class SimulateAopAfterCommand extends Command {
    @Override
    public void execute(Message msg) {
        IResult<Long> execStartTime = msg.getResult("execStartTime");
        long offset = System.currentTimeMillis() - execStartTime.get();
        System.out.println("Aop业务执行耗时：" + offset);
    }
}
