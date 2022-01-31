package kaka.test.fsm;

import com.kaka.fsm.DefaultStateMachine;
import com.kaka.fsm.State;

public class OrderStateMachine extends DefaultStateMachine<Order> {
    public OrderStateMachine(Order entity, State<Order> initialState) {
        super(entity, initialState);
    }
}
