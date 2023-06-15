package kaka.test;

import com.kaka.notice.Command;
import com.kaka.notice.Message;
import com.kaka.notice.detector.CommandDetector;
import com.kaka.notice.detector.MediatorDetector;
import com.kaka.notice.detector.ProxyDetector;

/**
 * 内部日志输出，需要手动注册，参见 {@link kaka.test.Asyn_Test}
 *
 * @author zkpursuit
 */
public class PrintLogHandler extends Command {
    @Override
    public void execute(Message msg) {
        Object[] args = (Object[]) msg.getBody();
        Class<?> cls = (Class<?>) args[0];
        if (cls.isAssignableFrom(CommandDetector.class)) {
            System.out.println("------------------1");
            return;
        }
        if (cls.isAssignableFrom(MediatorDetector.class)) {
            System.out.println("------------------2");
            return;
        }
        if (cls.isAssignableFrom(ProxyDetector.class)) {
            System.out.println("------------------3");
        }
    }
}
