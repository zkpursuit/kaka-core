package kaka.test.unit;

import com.kaka.notice.Command;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

@Handler(cmd = "40000", type = String.class, priority = 1)
public class CallbackCommand1 extends Command {
    @Override
    public void execute(Message msg) {
        System.out.println("CallbackCommand1被执行！");
        this.returnCallbackResult(new Object[]{100, "我爱我家"});
    }
}
