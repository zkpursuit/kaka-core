package com.kaka.notice.detector;

import com.kaka.notice.Facade;
import com.kaka.notice.FacadeFactory;
import com.kaka.notice.Mediator;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.MultiHandler;

/**
 * 注册事件观察者
 *
 * @author zkpursuit
 */
public class MediatorDetector implements IDetector {

    @Override
    public String name() {
        return "mediator";
    }

    /**
     * 识别事件观察者相关的类并注册到{@link Facade}
     *
     * @param cls 待识别的类，{@link Mediator}子类
     * @return 是否被识别注册
     */
    @Override
    public boolean discern(Class<?> cls) {
        if (!Mediator.class.isAssignableFrom(cls)) {
            return false;
        }
        MultiHandler sc = cls.getAnnotation(MultiHandler.class);
        if (sc == null) {
            return false;
        }
        Facade facade = sc.context().isEmpty() ? FacadeFactory.getFacade() : FacadeFactory.getFacade(sc.context());
        Mediator observer = facade.registerMediator((Class<? extends Mediator>) cls);
        if (facade.hasCommand("print_log")) {
            facade.sendMessage(new Message("print_log", new Object[]{MediatorDetector.class, new Object[]{observer.name, cls}}));
        }
        return true;
    }

}
