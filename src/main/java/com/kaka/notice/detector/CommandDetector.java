package com.kaka.notice.detector;

import com.kaka.notice.Command;
import com.kaka.notice.Facade;
import com.kaka.notice.FacadeFactory;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;
import com.kaka.util.StringUtils;

/**
 * 基于{@link Command}的注册器
 *
 * @author zkpursuit
 */
public class CommandDetector implements IDetector {

    @Override
    public String name() {
        return "command";
    }

    /**
     * 识别业务处理器相关的类并注册到{@link Facade}
     *
     * @param cls 待识别的类，{@link Command}子类
     * @return 注册后的
     */
    @Override
    public boolean discern(Class<?> cls) {
        if (!Command.class.isAssignableFrom(cls)) {
            return false;
        }
        Handler[] handlers = cls.getAnnotationsByType(Handler.class);
        if (handlers.length == 0) {
            return false;
        }
        Class<? extends Command> cmdClass = (Class<? extends Command>) cls;
        for (Handler h : handlers) {
            Object cmd = h.cmd();
            Class<?> typeCls = h.type();
            int priority = h.priority();
            Facade facade = h.context().isEmpty() ? FacadeFactory.getFacade() : FacadeFactory.getFacade(h.context());
            if (typeCls != String.class) {
                String cmdStr = String.valueOf(cmd);
                if (StringUtils.isNumeric(cmdStr)) {
                    if (typeCls == short.class || typeCls == Short.class) {
                        facade.registerCommand(Short.parseShort(cmdStr), cmdClass, h.pooledSize(), priority);
                    } else if (typeCls == int.class || typeCls == Integer.class) {
                        facade.registerCommand(Integer.parseInt(cmdStr), cmdClass, h.pooledSize(), priority);
                    } else if (typeCls == long.class || typeCls == Long.class) {
                        facade.registerCommand(Long.parseLong(cmdStr), cmdClass, h.pooledSize(), priority);
                    }
                    if (facade.hasCommand("print_log")) {
                        facade.sendMessage(new Message("print_log", new Object[]{CommandDetector.class, new Object[]{typeCls.getTypeName(), cmd, cmdClass}}));
                    }
                } else {
                    boolean isEnum = false;
                    if (typeCls.isEnum()) {
                        Object[] enumConstants = typeCls.getEnumConstants();
                        for (Object ec : enumConstants) {
                            String ecs = String.valueOf(ec);
                            if (cmd.equals(ecs)) {
                                cmd = ec;
                                isEnum = true;
                                break;
                            }
                        }
                    }
                    facade.registerCommand(cmd, cmdClass, h.pooledSize(), priority);
                    if (facade.hasCommand("print_log")) {
                        Object[] eventArgs;
                        if (isEnum) {
                            eventArgs = new Object[]{CommandDetector.class, new Object[]{typeCls.getTypeName(), cmd, cmdClass}};
                        } else {
                            eventArgs = new Object[]{CommandDetector.class, new Object[]{String.class.getTypeName(), cmd, cmdClass}};
                        }
                        facade.sendMessage(new Message("print_log", eventArgs));
                    }
                }
            } else {
                facade.registerCommand(cmd, cmdClass, h.pooledSize(), priority);
                if (facade.hasCommand("print_log")) {
                    facade.sendMessage(new Message("print_log", new Object[]{CommandDetector.class, new Object[]{typeCls.getTypeName(), cmd, cmdClass}}));
                }
            }
        }
        return true;
    }
}
