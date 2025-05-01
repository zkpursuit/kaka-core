package com.kaka.notice.detector;

import com.kaka.notice.*;
import com.kaka.notice.annotation.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于{@link Proxy}的注册器
 *
 * @author zkpursuit
 */
public class ProxyDetector extends PriorityDetector {

    private final List<Element<? extends Proxy>> list = new ArrayList<>();

    @Override
    public String name() {
        return "model";
    }

    /**
     * 识别业务模型相关的类并注册到{@link Facade}
     *
     * @param cls 待识别的类，{@link Proxy}子类
     * @return 是否被识别注册
     */
    @Override
    public boolean discern(Class<?> cls) {
        if (!Proxy.class.isAssignableFrom(cls)) {
            return false;
        }
        if (Mediator.class.isAssignableFrom(cls)) {
            return false;
        }
        Model model = cls.getAnnotation(Model.class);
        if (model == null) {
            return false;
        }
        Class<? extends Proxy> proxyClass = (Class<? extends Proxy>) cls;
        list.add(new Element<>(model, proxyClass));
        return true;
    }

    @Override
    public void centralizeProcess() {
        if (list.isEmpty()) return;
        list.sort((e1, e2) -> {
            Model m1 = e1.getAnnotation();
            Model m2 = e2.getAnnotation();
            return Integer.compare(m2.priority(), m1.priority());
        });
        list.forEach(element -> {
            Model model = element.getAnnotation();
            Class<? extends Proxy> cls = element.getClasz();
            Facade facade = model.context().isEmpty() ? FacadeFactory.getFacade() : FacadeFactory.getFacade(model.context());
            String modelName = model.value();
            Proxy proxy = !modelName.isEmpty() ? facade.registerProxy(cls, modelName) : facade.registerProxy(cls);
            proxy.setPriority(model.priority());
            if (facade.hasCommand("print_log")) {
                facade.sendMessage(new Message("print_log", new Object[]{ProxyDetector.class, new Object[]{proxy.getName(), cls}}));
            }
        });
        list.clear();
    }
}
