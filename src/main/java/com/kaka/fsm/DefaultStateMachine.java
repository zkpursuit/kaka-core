package com.kaka.fsm;

/**
 * 默认状态机实现
 *
 * @param <E> 状态机拥有者类型
 */
public class DefaultStateMachine<E> implements StateMachine<E> {

    /**
     * 状态机所属实体
     */
    protected E entity;

    /**
     * 状态机所属实体的当前状态
     */
    protected State<E> currentState;

    /**
     * 状态机所属实体的上一个状态
     */
    protected State<E> previousState;

    /**
     * 创建一个默认状态机实例
     */
    public DefaultStateMachine() {
        this(null);
    }

    /**
     * 创建一个默认状态机实例
     *
     * @param entity 状态机拥有者，即状态机所绑定的实体
     */
    public DefaultStateMachine(E entity) {
        this(entity, null);
    }

    /**
     * 创建一个默认状态机实例
     *
     * @param entity       状态机拥有者，即状态机所绑定的实体
     * @param initialState 初始化状态
     */
    public DefaultStateMachine(E entity, State<E> initialState) {
        this.entity = entity;
        this.setInitialState(initialState);
    }

    /**
     * 获得状态机所属实体
     */
    public E getEntity() {
        return entity;
    }

    @Override
    public void setInitialState(State<E> state) {
        this.previousState = null;
        this.currentState = state;
    }

    /**
     * 获取状态机的当前状态
     */
    @Override
    public State<E> getCurrentState() {
        return currentState;
    }

    /**
     * 获取状态机的上一状态
     */
    @Override
    public State<E> getPreviousState() {
        return previousState;
    }

    /**
     * 更新状态机
     * <p>
     * 必须调用当前状态的update方法
     * </p>
     */
    @Override
    public void update() {
        if (currentState != null) currentState.update(this);
    }

    /**
     * 转换到指定状态
     *
     * @param newState 新的目标状态
     */
    @Override
    public void changeState(State<E> newState) {
        previousState = currentState;
        if (currentState != null) currentState.exit(this);
        currentState = newState;
        if (currentState != null) currentState.enter(this);
    }

}
