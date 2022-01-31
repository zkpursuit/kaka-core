package com.kaka.fsm;

import com.kaka.notice.Message;

import java.io.Serializable;

/**
 * @param <E> 状态机绑定的实体类型
 */
public interface State<E> extends Serializable {

    /**
     * 状态进入时执行
     *
     * @param stateMachine 状态机
     */
    void enter(StateMachine<E> stateMachine);

    /**
     * 状态的常规更新
     *
     * @param stateMachine 状态机
     */
    void update(StateMachine<E> stateMachine);

    /**
     * 状态退出时执行
     *
     * @param stateMachine 状态机
     */
    void exit(StateMachine<E> stateMachine);

    /**
     * 当实体处于本状态且接收到事件时，此方法将被执行判断是否进入下一个状态<br>
     * 状态变迁过渡也可在此实施。
     *
     * @param stateMachine 状态机
     * @param event        实体所接收的事件，亦表示状态改变的条件
     * @return true 事件被成功处理;
     */
    boolean onMessage(StateMachine<E> stateMachine, Message event);
}
