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

    private final List<Element> list = new ArrayList<>();

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
        list.add(new Element(model, cls));
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
        list.forEach((element) -> {
            Model model = element.getAnnotation();
            Class<?> cls = element.getClasz();
            Facade facade = model.context().equals("") ? FacadeFactory.getFacade() : FacadeFactory.getFacade(model.context());
            Proxy proxy;
            String modelName = model.value();
            if (!"".equals(modelName)) {
                proxy = facade.registerProxy((Class<? extends Proxy>) cls, modelName);
            } else {
                proxy = facade.registerProxy((Class<? extends Proxy>) cls);
            }
            if (facade.hasCommand("print_log")) {
                facade.sendMessage(new Message("print_log", new Object[]{ProxyDetector.class, new Object[]{proxy.name, cls}}));
            }
        });
        list.clear();
    }
}
