package kaka.test.fsm;

import com.kaka.notice.Command;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

@Handler(cmd = "order_change")
public class OrderStateChangedHandler extends Command {
    @Override
    public void execute(Message msg) {
        if (!(msg instanceof StateMessage)) return;
        OrderStateMachine orderStateMachine = null;
        boolean result = orderStateMachine.handleMessage(msg);
        //if(result) orderStateMachine.changeState();
    }
}
