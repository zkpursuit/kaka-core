package com.kaka.notice;

/**
 * 控制命令类
 *
 * @author zkpursuit
 */
abstract public class Command extends Notifier implements ICommand {

    /**
     * 注册时的命令号
     */
    Object cmd;
    private Message msg;

    /**
     * 获取注册时的命令号
     *
     * @return 命令号
     */
    protected Object cmd() {
        return cmd;
    }

    /**
     * 重置对象，以备放入对象池中再次利用
     */
    @Override
    public void reset() {
        this.setFacade(null);
        this.cmd = null;
        this.msg = null;
    }

    /**
     * 设置执行回调参数
     *
     * @param params 回调参数
     */
    protected void returnCallbackResult(Object params) {
        Message msg = this.msg;
        if (msg == null) return;
        msg.setCallbackParams(this.getClass(), params);
    }

    /**
     * 执行事件通知
     *
     * @param msg 被执行的事件通知
     */
    void execute0(Message msg) {
        this.msg = msg;
        this.execute(msg);
        this.msg = null;
    }

    /**
     * 处理消息，此方法中不要用线程处理
     * <br>
     * 由于{@link Facade}中调度事件时，此方法被执行后即刻
     * 对本对象进行池化处理，将调用reset方法；如内部再使用线程，将引发不必要的错误。
     * <br>
     * 未池化的对象因不会调用reset方法，内部可使用线程。
     *
     * @param msg 通知消息
     * @see Message Message处理方法
     */
    abstract public void execute(Message msg);

}
