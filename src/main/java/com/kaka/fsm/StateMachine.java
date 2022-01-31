package com.kaka.fsm;

import com.kaka.notice.Message;

import java.io.Serializable;

/**
 * 状态机管理其实体的状态转换，且实体可以委托状态机处理其消息。<br>
 * 状态机主要由现态、条件、动作、次态四要素组成。<br>
 * 现态：是指当前所处的状态。 <br>
 * 条件：又称为“事件”。当一个条件被满足，将会触发一个动作，或者执行一次状态的迁移。 <br>
 * 动作：条件满足后执行的动作。动作执行完毕后，可以迁移到新的状态，也可以仍旧保持原状态。动作不是必需的，当条件满足后，也可以不执行任何动作，直接迁移到新状态。 <br>
 * 次态：条件满足后要迁往的新状态。“次态”是相对于“现态”而言的，“次态”一旦被激活，就转变成新的“现态”了。 <br>
 *
 * @param <E> 状态机绑定的实体
 */
public interface StateMachine<E> extends Serializable {

    /**
     * 状态机绑定的实体
     *
     * @return 状态机绑定的实体
     */
    E getEntity();

    /**
     * 为此状态机设置初始状态
     *
     * @param state 初始状态
     */
    void setInitialState(State<E> state);

    /**
     * 转换到指定状态
     *
     * @param newState 新的目标状态
     */
    void changeState(State<E> newState);

    /**
     * 获取状态机的当前状态
     */
    State<E> getCurrentState();

    /**
     * 获取状态机的上一状态
     */
    State<E> getPreviousState();

    /**
     * 判断状态机是否处于指定状态
     *
     * @param state 与当前状态比较的目标状态
     * @return true 表示处于指定状态
     */
    default boolean isInState(State<E> state) {
        State<E> currentState = this.getCurrentState();
        if (currentState == null) return false;
        return currentState.equals(state);
    }

    /**
     * 更新状态机
     * <p>
     * 必须调用当前状态的update方法
     * </p>
     */
    default void update() {
        State<E> currentState = this.getCurrentState();
        E entity = this.getEntity();
        if (currentState != null) currentState.update(this);
    }

    /**
     * 回溯到上一个状态
     *
     * @return true表示正确回溯到上一状态；如果没有上一状态，则直接返回false。
     */
    default boolean revertToPreviousState() {
        State<E> previousState = this.getPreviousState();
        if (previousState == null) {
            return false;
        }
        changeState(previousState);
        return true;
    }

    /**
     * 处理接收到的事件消息，即状态改变的条件
     * <p>
     * 基于此接口的类必须能正确的路由到对应的事件
     * </p>
     *
     * @param event 接收到的事件消息
     * @return true表示事件被成功处理
     */
    default boolean handleMessage(Message event) {
        State<E> currentState = this.getCurrentState();
        return currentState != null && currentState.onMessage(this, event);
    }
}
