package com.kaka.notice.detector;

import com.kaka.notice.Facade;
import com.kaka.notice.FacadeFactory;
import com.kaka.notice.Mediator;
import com.kaka.notice.Proxy;
import com.kaka.notice.annotation.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * 基于{@link Proxy}的注册器
 *
 * @author zkpursuit
 */
public class ProxyDetector extends PriorityDetector {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ProxyDetector.class.getTypeName());

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
            Facade facade;
            String context = model.context();
            if (context.equals("")) {
                facade = FacadeFactory.getFacade();
            } else {
                facade = FacadeFactory.getFacade(model.context());
            }
            Proxy proxy;
            String modelName = model.value();
            if (!"".equals(modelName)) {
                proxy = facade.registerProxy((Class<? extends Proxy>) cls, modelName);
                logger.log(Level.INFO, "注册业务数据模型：Proxy（{0}）==>>>  {1}", new Object[]{proxy.name, cls});
            } else {
                proxy = facade.registerProxy((Class<? extends Proxy>) cls);
                logger.log(Level.INFO, "注册业务数据模型：Proxy（{0}）==>>>  {1}", new Object[]{proxy.name, cls});
            }
        });
        list.clear();
    }
}
