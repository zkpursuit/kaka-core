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
        Handler[] controllers = cls.getAnnotationsByType(Handler.class);
        if (controllers.length == 0) {
            return false;
        }
        for (Handler regist : controllers) {
            Object cmd = regist.cmd();
            Class<?> cmdCls = regist.type();
            int priority = regist.priority();
            if (Command.class.isAssignableFrom(cls)) {
                Facade facade = regist.context().equals("") ? FacadeFactory.getFacade() : FacadeFactory.getFacade(regist.context());
                if (cmdCls != String.class) {
                    String cmdStr = String.valueOf(cmd);
                    if (StringUtils.isNumeric(cmdStr)) {
                        if (cmdCls == short.class || cmdCls == Short.class) {
                            facade.registerCommand(Short.parseShort(cmdStr), (Class<Command>) cls, regist.pooledSize(), priority);
                        } else if (cmdCls == int.class || cmdCls == Integer.class) {
                            facade.registerCommand(Integer.parseInt(cmdStr), (Class<Command>) cls, regist.pooledSize(), priority);
                        } else if (cmdCls == long.class || cmdCls == Long.class) {
                            facade.registerCommand(Long.parseLong(cmdStr), (Class<Command>) cls, regist.pooledSize(), priority);
                        }
                        if (facade.hasCommand("print_log")) {
                            facade.sendMessage(new Message("print_log", new Object[]{CommandDetector.class, new Object[]{cmdCls.getTypeName(), cmd, cls}}));
                        }
                    } else {
                        boolean isEnum = false;
                        if (cmdCls.isEnum()) {
                            Object[] enumConstants = cmdCls.getEnumConstants();
                            for (Object ec : enumConstants) {
                                String ecs = String.valueOf(ec);
                                if (cmd.equals(ecs)) {
                                    cmd = ec;
                                    isEnum = true;
                                    break;
                                }
                            }
                        }
                        facade.registerCommand(cmd, (Class<Command>) cls, regist.pooledSize(), priority);
                        if (facade.hasCommand("print_log")) {
                            Object[] eventArgs;
                            if (isEnum) {
                                eventArgs = new Object[]{CommandDetector.class, new Object[]{cmdCls.getTypeName(), cmd, cls}};
                            } else {
                                eventArgs = new Object[]{CommandDetector.class, new Object[]{String.class.getTypeName(), cmd, cls}};
                            }
                            facade.sendMessage(new Message("print_log", eventArgs));
                        }
                    }
                } else {
                    facade.registerCommand(cmd, (Class<Command>) cls, regist.pooledSize(), priority);
                    if (facade.hasCommand("print_log")) {
                        facade.sendMessage(new Message("print_log", new Object[]{CommandDetector.class, new Object[]{cmdCls.getTypeName(), cmd, cls}}));
                    }
                }
            }
        }
        return true;
    }
}
