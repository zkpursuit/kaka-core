package com.kaka.notice;

import com.kaka.util.NanoId;
import com.kaka.util.ReflectUtils;
import com.kaka.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * 整个框架的中枢
 *
 * @author zkpursuit
 */
public class Facade implements INotifier {
    String __name;
    private final Map<String, Proxy> proxyMap = new ConcurrentHashMap<>();
    private final Map<String, Mediator> mediatorMap = new ConcurrentHashMap<>();
    private final Map<Object, List<Mediator>> cmdMediatorMap = new ConcurrentHashMap<>();
    private final Map<Object, CommandPoolSortedSet> cmdPoolMap = new ConcurrentHashMap<>();
    private final Map<Object, Set<IListener>> listenerMap = new ConcurrentHashMap<>();
    private Executor threadPool;
    private ScheduledExecutorService scheduleThreadPool;
    private final Map<String, ScheduledFuture<?>> scheduleFutureMap = new ConcurrentHashMap<>();
    private RemoteMessagePostman remoteMessagePostman;

    /**
     * 创建一个内核
     */
    protected Facade() {
    }

    /**
     * 获取内核唯一标识名
     *
     * @return 内核唯一标识名
     */
    public String getName() {
        return this.__name;
    }

    /**
     * 初始化线程池，用于sendMessage中异步处理消息
     * <br>
     * 全局设置一次
     *
     * @param threadPool 线程池
     */
    public void initThreadPool(Executor threadPool) {
        if (this.threadPool == null) {
            this.threadPool = threadPool;
        }
    }

    /**
     * 异步定时调度线程池
     * <br>
     * 全局设置一次
     *
     * @param scheduleThreadPool 定时调度线程池
     */
    public void initScheduleThreadPool(ScheduledExecutorService scheduleThreadPool) {
        if (this.scheduleThreadPool == null) {
            this.scheduleThreadPool = scheduleThreadPool;
        }
    }

    /**
     * 初始化远程消息队列服务
     * <br>
     * 全局设置一次
     *
     * @param remoteMessagePostman 远程消息处理器
     */
    public void initRemoteMessagePostman(RemoteMessagePostman remoteMessagePostman) {
        if (this.remoteMessagePostman == null) {
            this.remoteMessagePostman = remoteMessagePostman;
            this.remoteMessagePostman.setFacade(this);
        }
    }

    /**
     * 获取异步执行线程池
     *
     * @return 异步执行线程池
     */
    public Executor getThreadPool() {
        return this.threadPool;
    }

    /**
     * 获取定时调度线程池
     *
     * @return 定时调度线程池
     */
    public ScheduledExecutorService getScheduleThreadPool() {
        return this.scheduleThreadPool;
    }

    /**
     * 创建对象
     *
     * @param clasz 对象Class
     * @return 实例
     */
    <T> T createObject(Class<T> clasz) {
        try {
            return ReflectUtils.newInstance(clasz);
        } catch (Exception ex) {
            throw new RuntimeException("必须声明一个无参构造方法", ex);
        }
    }

    /**
     * 注册数据代理
     *
     * @param name  数据代理唯一标识名
     * @param proxy 数据处理集中代理器，当并发高的情况下需手动处理数据同步访问问题
     */
    final void registerProxy(String name, Proxy proxy) {
        if (proxy == null) {
            return;
        }
        if (!StringUtils.isNotEmpty(name)) {
            throw new RuntimeException("注册的Proxy.name不能为空");
        }
        if (hasProxy(name)) {
            removeProxy(name);
        }
        proxyMap.put(name, proxy);
    }

    /**
     * 注册数据代理，此方式会以别名的方式多次注册同一对象，即注册单例
     *
     * @param <T>        限定类型
     * @param proxyClass 数据处理集中代理器
     * @param names      数据代理的唯一名称，可以为null，为null则将类限定名作为唯一名称
     * @return 数据代理模型对象
     */
    final public <T extends Proxy> T registerProxy(Class<T> proxyClass, String... names) {
        final Proxy proxy = this.createObject(proxyClass);
        String typeName = proxyClass.getTypeName();
        Set<String> aliasSet = names.length > 0 ? new HashSet<>(names.length + 1) : new HashSet<>(1);
        if (proxy.name != null) {
            registerProxy(proxy.name, proxy);
            aliasSet.add(typeName);
        } else {
            proxy.name = typeName;
            registerProxy(typeName, proxy);
        }
        for (String name : names) {
            if (name != null && !name.equals(proxy.name) && !name.equals(typeName)) {
                aliasSet.add(name);
            }
        }
        for (String name : aliasSet) {
            registerProxy(name, proxy);
            proxy.addAlias(name);
        }
        proxy.setFacade(this);
        proxy.onRegister();
        return (T) proxy;
    }

    /**
     * 注册数据代理，此方式会以别名的方式多次注册同一对象，即注册单例
     *
     * @param <T>        限定类型
     * @param proxyClass 数据处理集中代理器
     * @return 数据代理模型对象
     */
    final public <T extends Proxy> T registerProxy(Class<T> proxyClass) {
        return registerProxy(proxyClass, (String) null);
    }

    /**
     * 注册数据代理
     *
     * @param <T>   限定类型
     * @param proxy 数据处理集中代理器，当并发高的情况下需手动处理数据同步访问问题
     * @return 数据处理
     */
    final public <T extends Proxy> T registerProxy(T proxy) {
        registerProxy(proxy.name, proxy);
        proxy.setFacade(this);
        proxy.onRegister();
        return proxy;
    }

    /**
     * 是否已经注册了名为proxyName的数据代理处理器
     *
     * @param proxyName 数据处理代理器名称，此代理器的唯一标识
     * @return true 存在此代理器
     */
    final public boolean hasProxy(String proxyName) {
        return proxyMap.containsKey(proxyName);
    }

    /**
     * 是否存在相应的数据模型
     *
     * @param proxyClass 模型代理类
     * @return true 存在
     */
    final public boolean hasProxy(Class<? extends Proxy> proxyClass) {
        String _name = proxyClass.getTypeName();
        return hasProxy(_name);
    }

    /**
     * 获取数据代理
     *
     * @param <T>       限定类型
     * @param proxyName 数据处理代理器名称，此代理器的唯一标识
     * @return 数据处理代理器
     */
    final public <T extends Proxy> T retrieveProxy(String proxyName) {
        return (T) proxyMap.get(proxyName);
    }

    /**
     * 获取数据代理
     *
     * @param <T>        限定类型
     * @param proxyClass 数据处理代理器类
     * @return 数据处理代理器
     */
    final public <T extends Proxy> T retrieveProxy(Class<T> proxyClass) {
        return retrieveProxy(proxyClass.getTypeName());
    }

    /**
     * 移除数据代理
     *
     * @param <T>       限定类型
     * @param proxyName 数据代理器唯一标识
     * @return 数据代理
     */
    final public <T extends Proxy> T removeProxy(String proxyName) {
        Proxy proxy = proxyMap.remove(proxyName);
        if (proxy != null) {
            proxy.onRemove();
        }
        return (T) proxy;
    }

    /**
     * 移除数据代理
     *
     * @param <T>   限定类型
     * @param proxy 数据代理器
     * @return 数据代理
     */
    final public <T extends Proxy> T removeProxy(T proxy) {
        if (proxy == null) {
            return null;
        }
        //因为是注册的相同对象，所以此处注册的别名对应的观察者也一并清除
        String[] alis = proxy.getAliases();
        if (alis != null && alis.length > 0) {
            for (String alisName : alis) {
                proxyMap.remove(alisName);
            }
        }
        return removeProxy(proxy.name);
    }

    /**
     * 移除数据代理
     *
     * @param <T>        限定类型
     * @param proxyClass 数据代理类型
     * @return 数据代理
     */
    final public <T extends Proxy> T removeProxy(Class<T> proxyClass) {
        Proxy proxy = retrieveProxy(proxyClass.getTypeName());
        removeProxy(proxy);
        return (T) proxy;
    }

    /**
     * 注册事件观察者
     *
     * @param name     事件观察者名称
     * @param mediator 事件观察者
     */
    final void registerMediator(String name, Mediator mediator) {
        if (mediator == null) {
            return;
        }
        if (!StringUtils.isNotEmpty(name)) {
            throw new RuntimeException("注册的Observer.name不能为空");
        }
        if (hasMediator(name)) {
            removeMediator(name);
        }
        mediatorMap.put(name, mediator);
    }

    /**
     * 注册事件观察者感兴趣的事件
     *
     * @param mediator 事件观察者
     */
    final void registerMediatorMessageInterests(Mediator mediator) {
        Object[] evtIds = mediator.listMessageInterests();
        if (evtIds == null || evtIds.length == 0) {
            return;
        }
        for (Object eid : evtIds) {
            List<Mediator> list = cmdMediatorMap.computeIfAbsent(eid, k -> Collections.synchronizedList(new ArrayList<>()));
            list.add(mediator);
        }
        mediator.setFacade(this);
        mediator.onRegister();
    }

    /**
     * 注册事件观察者，此方式会以别名的方式多次注册同一对象，即注册单例
     * <p>
     * 事件观察者能集中监听通知消息，可理解为可处理多个Command的对象
     *
     * @param <T>           限定类型
     * @param mediatorClass 事件观察者
     * @param names         事件观察者的唯一名称，可以为null，为null则将类限定名作为唯一名称
     * @return 事件观察者对象
     */
    <T extends Mediator> T registerMediator(Class<T> mediatorClass, String... names) {
        final Mediator mediator = this.createObject(mediatorClass);
        String typeName = mediatorClass.getTypeName();
        Set<String> aliasSet = names.length > 0 ? new HashSet<>(names.length + 1) : new HashSet<>(1);
        if (mediator.name != null) {
            registerMediator(mediator.name, mediator);
            aliasSet.add(typeName);
        } else {
            mediator.name = typeName;
            registerMediator(typeName, mediator);
        }
        for (String name : names) {
            if (name != null && !name.equals(mediator.name) && !name.equals(typeName)) {
                aliasSet.add(name);
            }
        }
        for (String name : aliasSet) {
            registerMediator(name, mediator);
            mediator.addAlias(name);
        }
        registerMediatorMessageInterests(mediator);
        return (T) mediator;
    }

    /**
     * 注册事件观察者
     *
     * @param <T>           限定类型
     * @param mediatorClass 事件观察者，能集中监听通知消息，可理解为可处理多个Command的对象
     * @return 事件观察者
     */
    final public <T extends Mediator> T registerMediator(Class<T> mediatorClass) {
        return registerMediator(mediatorClass, (String) null);
    }

    /**
     * 注册事件观察者
     *
     * @param <T>      限定类型
     * @param mediator 视图代理，能集中监听通知消息，可理解为可处理多个Command的对象
     * @return 事件观察者
     */
    final <T extends Mediator> T registerMediator(T mediator) {
        registerMediator(mediator.name, mediator);
        registerMediatorMessageInterests(mediator);
        return mediator;
    }

    /**
     * 是否已经注册了名为observerName的事件观察者
     *
     * @param mediatorName 视图代理处理唯一标识
     * @return true 存在
     */
    final public boolean hasMediator(String mediatorName) {
        return mediatorMap.containsKey(mediatorName);
    }

    /**
     * 是否存在相应的事件观察者
     *
     * @param mediatorClass 事件观察者类
     * @return true 存在
     */
    final public boolean hasMediator(Class<? extends Mediator> mediatorClass) {
        return mediatorMap.containsKey(mediatorClass.getTypeName());
    }

    /**
     * 移除事件观察者
     *
     * @param mediatorName 事件观察者处理唯一标识
     */
    final <T extends Mediator> T removeMediator(String mediatorName) {
        Mediator mediator = mediatorMap.remove(mediatorName);
        if (mediator != null) {
            Object[] nids = mediator.listMessageInterests();
            if (nids == null) {
                return (T) mediator;
            }
            for (Object nid : nids) {
                List<Mediator> list = cmdMediatorMap.get(nid);
                if (list == null || list.isEmpty()) continue;
                list.remove(mediator);
            }
            mediator.setFacade(null);
            mediator.onRemove();
        }
        return (T) mediator;
    }

    /**
     * 移除事件观察者
     *
     * @param <T>      限定类型
     * @param mediator 事件观察者
     * @return 事件观察者
     */
    final <T extends Mediator> T removeMediator(T mediator) {
        if (mediator == null) {
            return null;
        }
        //因为是注册的相同的事件，所以此处注册的别名对应的观察者也一并清除
        String[] alis = mediator.getAliases();
        if (alis != null && alis.length > 0) {
            for (String alisName : alis) {
                mediatorMap.remove(alisName);
            }
        }
        return removeMediator(mediator.name);
    }

    /**
     * 移除事件观察者
     *
     * @param <T>           限定类型
     * @param mediatorClass 事件观察者类型
     * @return 事件观察者
     */
    final public <T extends Mediator> T removeMediator(Class<T> mediatorClass) {
        Mediator mediator = retrieveMediator(mediatorClass.getTypeName());
        removeMediator(mediator);
        return (T) mediator;
    }

    /**
     * 获取事件观察者
     *
     * @param <T>          限定类型
     * @param mediatorName 事件观察者处理唯一标识
     * @return 事件观察者�
     */
    final <T extends Mediator> T retrieveMediator(String mediatorName) {
        return (T) mediatorMap.get(mediatorName);
    }

    /**
     * 获取数据代理
     *
     * @param <T>           限定类型
     * @param mediatorClass 事件观察者类型
     * @return 事件观察者
     */
    final public <T extends Mediator> T retrieveMediator(Class<T> mediatorClass) {
        return retrieveMediator(mediatorClass.getTypeName());
    }

    /**
     * 获取通知对象，可为数据代理处理器或者为视图代理器
     *
     * @param name 通知对象的唯一标识
     * @return 通知对象
     * @see Proxy
     * @see Mediator
     */
    final Notifier retrieve(String name) {
        if (proxyMap.containsKey(name)) {
            return proxyMap.get(name);
        }
        return mediatorMap.get(name);
    }

    /**
     * 是否存在命令执行器
     *
     * @param cmd 命令执行器唯一标识
     * @return true 存在
     * @see Command
     */
    final public boolean hasCommand(Object cmd) {
        return cmdPoolMap.containsKey(cmd);
    }

    /**
     * 注册命令执行器
     *
     * @param cmd        命令执行器唯一标识
     * @param clasz      命令执行器类对象
     * @param pooledSize 池化大小，-1表示不池化
     * @param priority   执行优先级，数字越小越先执行，可依此模拟切面编程
     */
    final public void registerCommand(Object cmd, Class<? extends Command> clasz, int pooledSize, int priority) {
        CommandPoolSortedSet sortedSet = cmdPoolMap.computeIfAbsent(cmd, k -> new CommandPoolSortedSet());
        sortedSet.add(new CommandPool(clasz, pooledSize, priority));
    }

    /**
     * 注册命令执行器
     *
     * @param cmd        命令执行器唯一标识
     * @param clasz      命令执行器类对象
     * @param pooledSize 池化大小，-1表示不池化
     */
    final public void registerCommand(Object cmd, Class<? extends Command> clasz, int pooledSize) {
        registerCommand(cmd, clasz, pooledSize, 0);
    }

    /**
     * 注册命令执行器，默认不池化
     *
     * @param cmd   命令执行器唯一标识
     * @param clasz 命令执行器类对象
     */
    final public void registerCommand(Object cmd, Class<? extends Command> clasz) {
        registerCommand(cmd, clasz, -1);
    }

    /**
     * 移除命令执行器
     *
     * @param cmd 命令执行器唯一标识
     */
    final public void removeCommand(Object cmd) {
        cmdPoolMap.remove(cmd);
    }

    /**
     * 移除命令执行器
     *
     * @param cmd   命令执行器唯一标识
     * @param clasz 命令执行器类对象
     */
    final public void removeCommand(Object cmd, Class<? extends Command> clasz) {
        if (cmdPoolMap.containsKey(cmd)) {
            CommandPoolSortedSet sortedSet = cmdPoolMap.get(cmd);
            sortedSet.remove(new CommandPool(clasz));
        }
    }

    /**
     * 添加事件监听器
     *
     * @param cmd      事件名
     * @param listener 事件监听器
     */
    final public void addListener(Object cmd, IListener listener) {
        Set<IListener> listeners = listenerMap.computeIfAbsent(cmd, k -> Collections.synchronizedSet(new LinkedHashSet<>()));
        listeners.add(listener);
    }

    /**
     * 移除事件监听器
     *
     * @param cmd      事件名
     * @param listener 事件监听器
     */
    final public void removeListener(Object cmd, IListener listener) {
        Set<IListener> listeners = listenerMap.get(cmd);
        if (listeners == null) return;
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            listenerMap.remove(cmd);
        }
    }

    /**
     * 移除事件名对应的所有监听器
     *
     * @param cmd 事件名
     */
    final public void removeListener(Object cmd) {
        listenerMap.remove(cmd);
    }

    /**
     * 执行所有相同命令号下的{@link com.kaka.notice.Command}对象
     *
     * @param poolSet 相同命令号的{@link com.kaka.notice.Command}对象池集合
     * @param msg     事件消息
     */
    private void execCommands(final CommandPoolSortedSet poolSet, final Message msg) {
        for (CommandPool pool : poolSet) {
            final Command cmd = pool.obtain();
            if (cmd == null) continue;
            cmd.setFacade(this);
            cmd.cmd = msg.getWhat();
            cmd.execute0(msg);
            msg.callback(cmd.getClass().getTypeName());
            pool.idle(cmd);
        }
    }

    private void executeMediator(final Mediator mediator, final Message msg) {
        mediator.handleMessage0(msg);
        msg.callback(mediator.getClass().getTypeName());
    }

    private void executeListener(final IListener listener, final Message msg) {
        listener.onMessage(msg, this);
        msg.callback(listener.getClass().getTypeName());
    }

    /**
     * 消息事件处理
     *
     * @param msg 待处理的消息
     */
    private void sendMessage0(final Message msg) {
        if (msg == null || msg.getWhat() == null) {
            return;
        }
        final CommandPoolSortedSet poolSet = cmdPoolMap.get(msg.getWhat());
        if (poolSet != null) {
            execCommands(poolSet, msg);
        }
        final List<Mediator> mediatorList = cmdMediatorMap.get(msg.getWhat());
        if (mediatorList != null) {
            for (Mediator mediator : mediatorList) {
                executeMediator(mediator, msg);
            }
        }
        final Set<IListener> listeners = listenerMap.get(msg.getWhat());
        if (listeners != null) {
            for (IListener listener : listeners) {
                executeListener(listener, msg);
            }
        }
        if (msg.poolable) msg.reset();
    }

    /**
     * 消息事件处理
     *
     * @param msg         待处理的消息
     * @param executeType 消息执行类型
     */
    public void sendMessage(final Message msg, final Message.ExecuteType executeType) {
        if (msg == null || msg.getWhat() == null) {
            return;
        }
        if (executeType == Message.ExecuteType.ASYN_REMOTE_QUEUE) {
            if (this.remoteMessagePostman == null) {
                throw new RuntimeException(String.format("执行sendRemoteMessage前请先调用 %s.initRemoteMessagePostman方法初始化", this.getClass()));
            }
            Map<Object, IResult> msgResultMap = msg.resultMap;
            Message mqMsg = new Message(msg.getWhat(), msg.getBody());
            String id = NanoId.randomNanoId();
            if (msgResultMap != null) {
                msgResultMap.forEach((Object key, IResult result) -> {
                    if (result instanceof AsynResult) {
                        mqMsg.setResult((String) key, new RemoteAsynResult<>());
                    }
                });
            }
            this.remoteMessagePostman.remoteMessageCache.add(id, msg);
            RemoteMessage remoteMessage = new RemoteMessage(this.remoteMessagePostman.event_topic, id, mqMsg);
            this.remoteMessagePostman.sendRemoteMessage(remoteMessage);
            return;
        }
        if (executeType == Message.ExecuteType.SYNC) {
            this.sendMessage0(msg);
        } else if (executeType == Message.ExecuteType.ASYN_THREAD) {
            if (threadPool == null) {
                throw new RuntimeException(String.format("执行异步sendMessage前请先调用 %s.initThreadPool方法初始化线程池", this.getClass()));
            }
            threadPool.execute(() -> this.sendMessage0(msg));
        } else if (executeType == Message.ExecuteType.ASYN_VIRTUAL_THREAD) {
            Thread.ofVirtual().start(() -> this.sendMessage0(msg));
        }
    }

    /**
     * 消息事件处理
     *
     * @param msg  待处理的消息
     * @param asyn true为异步，设为true时须调用initThreadPool方法初始化线程池
     */
    @Override
    public void sendMessage(final Message msg, final boolean asyn) {
        sendMessage(msg, asyn ? Message.ExecuteType.ASYN_THREAD : Message.ExecuteType.SYNC);
    }

    /**
     * 消息事件处理
     *
     * @param msg 通知消息
     */
    @Override
    final public void sendMessage(Message msg) {
        sendMessage(msg, Message.ExecuteType.SYNC);
    }

    /**
     * 发送到远程消息队列，并由消息队列消费端处理事件消息。
     * <br>
     * {@link SyncResult} 同步获取结果将不受支持。
     * <br>
     * 支持{@link AsynResult}或者异步回调获取远程事件执行结果。
     * <br>
     * 保证事件的顺利执行完全由消息队列的运行情况而决定。
     *
     * @param msg 待发送的消息
     */
    @Override
    public void sendRemoteMessage(Message msg) {
        this.sendMessage(msg, Message.ExecuteType.ASYN_REMOTE_QUEUE);
    }

    /**
     * 终止任务调度
     *
     * @param name 任务名
     */
    void cancelSchedule(String name) {
        ScheduledFuture<?> future = scheduleFutureMap.remove(name);
        if (future == null) return;
        future.cancel(true);
    }

    /**
     * 定时调度执行事件通知
     *
     * @param msg       事件
     * @param scheduler 定时调度器
     */
    @Override
    final public void sendMessage(final Message msg, Scheduler scheduler) {
        if (scheduleThreadPool == null) {
            throw new RuntimeException(String.format("执行sendMessage定时调度前请先调用 %s.initScheduleThreadPool方法初始化线程池", this.getClass()));
        }
        if (scheduler.facade != null && scheduler.msg != null) {
            throw new RuntimeException(String.format("每次调用sendMessage进行事件调度时必须保证%s参数为新的且独立的对象", Scheduler.class.getTypeName()));
        }
        msg.poolable = false;
        Object cmd = msg.what;
        scheduler.name += String.format("_$%s$_%s", cmd.getClass().getTypeName(), cmd);
        scheduler.facade = this;
        scheduler.msg = msg;
        long currMillSecs = System.currentTimeMillis();
        if (scheduler.startTime <= 0) {
            scheduler.startTime = currMillSecs;
        }
        scheduler.prevExecTime.set(scheduler.startTime);
        long initDelay = scheduler.startTime >= currMillSecs ? scheduler.startTime - currMillSecs : 0;
        long delay = scheduler.interval <= 0 ? 1 : scheduler.interval;
        cancelSchedule(scheduler.name);
        ScheduledFuture<?> future = scheduleThreadPool.scheduleWithFixedDelay(scheduler, initDelay, delay, TimeUnit.MILLISECONDS);
        scheduleFutureMap.put(scheduler.name, future);
    }

    /**
     * 取消调度
     *
     * @param cmd   事件名
     * @param group 调度器组名
     */
    @Override
    final public void cancelSchedule(Object cmd, String group) {
        String name = group;
        name += String.format("_$%s$_%s", cmd.getClass().getTypeName(), cmd);
        cancelSchedule(name);
    }

    /**
     * 释放内存
     */
    public void dispose() {
        for (Object key : cmdPoolMap.keySet()) {
            CommandPoolSortedSet cmdPoolSet = cmdPoolMap.get(key);
            if (cmdPoolSet != null) {
                for (CommandPool pool : cmdPoolSet) {
                    pool.clear();
                }
                cmdPoolSet.clear();
            }
        }
        this.cmdPoolMap.clear();
        for (Object key : cmdMediatorMap.keySet()) {
            List<Mediator> list = cmdMediatorMap.get(key);
            if (list != null) {
                list.clear();
            }
        }
        this.cmdMediatorMap.clear();
        this.mediatorMap.clear();
        this.proxyMap.clear();
        this.listenerMap.clear();
        this.threadPool = null;
        for (String key : scheduleFutureMap.keySet()) {
            this.cancelSchedule(key);
        }
        this.scheduleFutureMap.clear();
        this.scheduleThreadPool = null;
    }

}
