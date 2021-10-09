package kaka.test.unit;

import com.kaka.notice.Command;
import com.kaka.notice.IResult;
import com.kaka.notice.Message;
import com.kaka.notice.SyncResult;
import com.kaka.notice.annotation.Handler;

/**
 * 模拟切面，执行前
 */
@Handler(cmd = "40000", type = String.class, priority = 1)
public class SimulateAopBeforeCommand extends Command {
    @Override
    public void execute(Message msg) {
        IResult<Long> execStartTime = new SyncResult<>(); //中间变量亦可使用 ThreadLocal 存储
        execStartTime.set(System.currentTimeMillis());
        msg.setResult("execStartTime", execStartTime);
    }
}
