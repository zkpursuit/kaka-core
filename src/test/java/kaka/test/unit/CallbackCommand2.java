package kaka.test.unit;

import com.kaka.notice.Command;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

@Handler(cmd = "40000", type = String.class, priority = 2)
public class CallbackCommand2 extends Command {
    @Override
    public void execute(Message msg) {
        this.returnCallbackResult("我爱我家2");
    }
}

