package com.kaka.numerical;

import com.kaka.notice.Facade;
import com.kaka.notice.FacadeFactory;
import com.kaka.notice.Proxy;
import com.kaka.notice.detector.PriorityDetector;
import com.kaka.numerical.annotation.Numeric;
import com.kaka.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于{@link NumericConfig}的注册器
 *
 * @author zkpursuit
 */
public class NumericDetector extends PriorityDetector {

    private final List<Element> list = new ArrayList<>();

    @Override
    public String name() {
        return "numeric";
    }


    /**
     * 解析数值配置文件，如果有数值配置文件需要被解析，那么必须覆盖此方法<br>
     * 目前支持解析的配置文件，包括Excel内容直接复制的纯文本数据和XML格式的数据<br>
     *
     * @param cls 待注册的类，{@link NumericConfig}子类
     * @return 是否被识别注册
     */
    @Override
    public boolean discern(Class<?> cls) {
        if (!NumericConfig.class.isAssignableFrom(cls)) {
            return false;
        }
        Numeric numeric = cls.getAnnotation(Numeric.class);
        if (numeric == null) {
            return false;
        }
        if (!StringUtils.isNotEmpty(numeric.src())) {
            return false;
        }
        list.add(new Element(numeric, cls));
        return true;
    }

    @Override
    public void centralizeProcess() {
        if (list.isEmpty()) return;
        list.sort((e1, e2) -> {
            Numeric numeric1 = e1.getAnnotation();
            Numeric numeric2 = e2.getAnnotation();
            return Integer.compare(numeric2.priority(), numeric1.priority());
        });
        list.forEach((element) -> {
            Class<?> cls = element.getClasz();
            Numeric numeric = element.getAnnotation();
            Facade facade = numeric.context().equals("") ? FacadeFactory.getFacade() : FacadeFactory.getFacade(numeric.context());
            facade.registerProxy((Class<? extends Proxy>) cls, numeric.src());
        });
        list.clear();
    }
}
