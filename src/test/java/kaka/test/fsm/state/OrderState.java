package kaka.test.fsm.state;

import com.kaka.fsm.State;
import com.kaka.fsm.StateMachine;
import com.kaka.notice.Message;
import kaka.test.fsm.Order;

public enum OrderState implements State<Order> {
    wait_pay {
        @Override
        public boolean onMessage(StateMachine<Order> stateMachine, Message message) {
            Object eventType = message.getWhat();
            if (eventType instanceof OrderEvent) {
                OrderEvent event = (OrderEvent) eventType;
                if (event == OrderEvent.pay) {
                    if (Boolean.TRUE.equals(message.getBody())) {
                        stateMachine.changeState(OrderState.pay_success);
                        return true;
                    }
                }
            }
            return false;
        }
    }, //订单初始化等待支付
    cancelled {
        @Override
        public boolean onMessage(StateMachine<Order> stateMachine, Message message) {
            return false;
        }
    }, //订单取消
    closed {
        @Override
        public boolean onMessage(StateMachine<Order> stateMachine, Message message) {
            return false;
        }
    },//订单关闭
    pay_success {
        @Override
        public boolean onMessage(StateMachine<Order> stateMachine, Message message) {
            return false;
        }
    },//支付成功
    refunding {
        @Override
        public boolean onMessage(StateMachine<Order> stateMachine, Message message) {
            return false;
        }
    },//退款中
    refunded {
        @Override
        public boolean onMessage(StateMachine<Order> stateMachine, Message message) {
            return false;
        }
    };//已退款

    @Override
    public void enter(StateMachine<Order> stateMachine) {

    }

    @Override
    public void update(StateMachine<Order> stateMachine) {

    }

    @Override
    public void exit(StateMachine<Order> stateMachine) {

    }

    @Override
    abstract public boolean onMessage(StateMachine<Order> stateMachine, Message message);
}
