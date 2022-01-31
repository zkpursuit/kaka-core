package kaka.test;

import com.kaka.Startup;
import com.kaka.fsm.State;
import com.kaka.fsm.StateMachine;
import com.kaka.notice.Facade;
import com.kaka.notice.FacadeFactory;
import kaka.test.fsm.Order;
import kaka.test.fsm.state.OrderState;
import kaka.test.fsm.OrderStateMachine;

public class Fsm_Test extends Startup {

    public static void main(String[] args) {
        Facade facade = FacadeFactory.getFacade();
        Fsm_Test test = new Fsm_Test();
        test.scan("kaka.test.fsm");

        Order order = new Order();
        order.setId(1L);
        StateMachine<Order> stateMachine = new OrderStateMachine(order, OrderState.wait_pay);
    }

}
