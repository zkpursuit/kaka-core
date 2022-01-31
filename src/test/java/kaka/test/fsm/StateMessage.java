package kaka.test.fsm;

import com.kaka.fsm.State;
import com.kaka.notice.Message;

public class StateMessage extends Message {
    private final State<?> state;

    public StateMessage(Object what, State<?> state, Object body) {
        super(what, body);
        this.state = state;
    }

    public State<?> getState() {
        return this.state;
    }
}
