<h1 align="center" style="text-align:center;">
  kaka-core
</h1>
<p align="center" style="text-align:center;">
	<strong>基于事件驱动的领域模型框架</strong>
</p>

<p align="center" style="text-align:center;">
    <a target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0.txt">
		<img src="https://img.shields.io/:license-Apache2-blue.svg" alt="Apache 2" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK8+-green.svg" alt="JDK8+" />
	</a>
    <br />
    <a target="_blank" href='https://gitee.com/zkpursuit/kaka-core/stargazers'>
        <img src='https://gitee.com/zkpursuit/kaka-core/badge/star.svg' alt='gitee star'/>
    </a>
    <a target="_blank" href='https://github.com/zkpursuit/kaka-core/stargazers'>
        <img src="https://img.shields.io/github/stars/zkpursuit/kaka-core.svg?logo=github" alt="github star"/>
    </a>
</p>

<p align="center" style="text-align:center;">
	<img src="https://img.shields.io/badge/QQ交流群-801241310-orange" alt="help"/>
    <img src="https://img.shields.io/badge/答疑交流（微信）-zkpursuit-blue" alt="help"/>
</p>

<hr />

#### 介绍

模块为全局事件驱动框架，无任何第三方依赖；支持同步或者异步获取事件处理结果；支持对接第三方消息队列；可解耦业务，简化程序复杂性，提高代码可读性，降低开发维护成本。

#### 软件架构

基于观察者和命令模式

#### 安装使用

```xml

<dependency>
    <groupId>io.github.zkpursuit</groupId>
    <artifactId>kaka-core</artifactId>
    <version>6.0.1</version>
</dependency>
```

#### 使用说明

1. 通过Startup.scan方法扫描指定包下的Command、Proxy、Mediator子类并将其注册到Facade中，Command、Proxy、Mediator亦可直接使用Facade对应的方法手动注册；由Facade处理事件流向。
2. Command、Mediator一般作为业务处理器处理业务，Proxy为数据模型（比如作为数据库service层），Command、Mediator中可通过getProxy方法获得Proxy数据模型。
3. Command只能监听注册到Facade中的事件，可多个事件注册同一个Command（也可理解为一个Command可监听多个事件），而Mediator则是监听多个自身感兴趣的事件，具体对哪些事件感兴趣则由listMessageInterests方法的返回值决定（总结：一个Command可以对应多个事件；一个事件可以对应多个Mediator，一个Mediator可以对应多个事件；一个事件可以同时对应多个Command和多个Mediator；Command为动态创建，但可池化，Mediator为全局唯一）；Command、Mediator是功能非常相似的事件监听器和事件派发器。
4. Command、Proxy、Mediator中都能通过sendMessage方法向外派发事件，也可在此框架之外直接使用Facade实例调用sendMessage派发事件。
5. 此框架的事件数据类型尽可能的使用int和String。
6. Facade实例在调用initThreadPool方法配置了线程池的情况下，Facade、Command、Proxy、Mediator的sendMessage都将直接支持异步派发事件，默认为同步。
7. 统一同步或者异步获得事件处理结果，异步获取事件结果以wait、notifyAll实现。应该尽可能的少使用此方式，而改用派发事件方式。
8. 新增支持异步回调获取执行结果，优化第7点。
9. 新增支持单个事件对应多个Command（与第3点早期版本单个事件仅支持一个Command做了增强），并可依此模拟切面编程。
10. Handler注解支持枚举类型，亦可参考Handler自定义注解并实现IDetector的子类解析注解（需要调用startup.addDetector），例如：
    ```text 
    @Handler(cmd="A", type=MyEnum.class)
    其中"A"为MyEnum中的枚举项
    ```
11. 支持远端分布式事件处理并可获得事件处理结果（此功能由5.6版本重构所得）。
12. 支持对接远程消息队列，几乎支持市面上的所有消息队列。
13. 对接消息队列为分布式远程事件处理的具体实现方案之一，可参考以下范例代码 Remote_Test 类。
14. 使用第三方消息队列消费事件并处理时，返回处理结果可如在本地执行后通过AsynResult或者异步回调获取执行结果。
15. 对接第三方消息队列时，稳定性完全由第三方消息队列决定。

基于此模型构建的斗地主开放源代码 https://gitee.com/zkpursuit/fight-against-landlords

### 运行时最低要求jdk17+，以下范例均在 jdk-17.0.3.1 测试运行
### 从6.0.0版本开始引入虚拟线程，故最低运行时要求jdk21+

```java
import com.kaka.Startup;
import com.kaka.notice.*;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 异步使用范例
 *
 * @author zkpursuit
 */
public class Test extends Startup {

    public static void main(String[] args) {
        Facade facade = FacadeFactory.getFacade();
        Test test = new Test();
        test.scan("com.test.units"); //扫描类包注册事件
        facade.initThreadPool(Executors.newFixedThreadPool(2)); //全局仅设置一次
        //同步发送事件通知
        facade.sendMessage(new Message("1000", "让MyCommand接收执行"));
        //简单的异步发送事件通知
        facade.sendMessage(new Message("2000", "让MyMediator和MyCommand接收执行"), true);

        /*
            1、以下为测试发送事件通知后获得事件处理器的处理结果。
            2、一般情况我们不一定需要此功能，为了尽可能的减少对象创建，故而
        在需要使用此功能时手动创建AsynResult或者SyncResult对象。
            3、我们应该尽可能的使用事件模式代替，比如事件处理器处理完成后再次
        调用sendMessage向外派发事件，分散到其它事件处理器中处理，而不是等待处
        理结果。
            4、异步future模式获取事件处理结果其本质是利用wait、notify（notifyAll）
        实现，而使用事件模式则无需调用wait让线程中断等待。
         */
        //获取异步处理结果
        Message asynMsg = new Message("10000", "让ResultCommand接收执行");
        //由于事件通知为广播模式，故而必须为执行结果进行命名标识唯一性
        IResult<String> result0 = asynMsg.setResult("ResultMsg", new AsynResult<>(12000));
        facade.sendMessage(asynMsg, true); //异步发送事件通知
        System.out.println(result0.get());

        //获取同步执行结果
        Message syncMsg = new Message("20000", "让ResultCommand接收执行");
        //由于事件通知为广播模式，故而必须为执行结果进行命名标识唯一性
        IResult<String> result1 = syncMsg.setResult("ResultMsg", new SyncResult<>());
        facade.sendMessage(syncMsg, false);  //同步发送事件通知
        System.out.println(result1.get());

        //另一种异步处理方式,同步派发事件，事件处理器中使用FutureTask及线程异步获取执行结果
        Message syncMsg1 = new Message("30000", "让FutureCommand接收执行");
        IResult<String> result2 = syncMsg1.setResult("ResultMsg", new SyncResult<>());
        facade.sendMessage(syncMsg1, false); //同步发送事件通知
        System.out.println(result2.get());

        //哈哈，异步中的异步，其实没必要
        Message syncMsg2 = new Message("30000", "让FutureCommand接收执行");
        IResult<String> result3 = syncMsg2.setResult("ResultMsg", new AsynResult<>());
        facade.sendMessage(syncMsg2, true); //异步发送事件通知
        System.out.println(result3.get());

        //基于事件模拟切面编程，仅支持Command
        facade.sendMessage(new Message("40000"), true);

        //异步回调获取事件执行结果
        facade.sendMessage(new Message("50000", "", (IResult<Object> result) -> {
            String clasz = ((CallbackResult<Object>) result).eventHanderClass;
            StringBuilder sb = new StringBuilder("异步回调：\t" + clasz + "\t");
            Object resultObj = result.get();
            if (resultObj instanceof Object[]) {
                Object[] ps = (Object[]) resultObj;
                sb.append(Arrays.toString(ps));
            } else {
                sb.append(resultObj);
            }
            System.out.println(sb);
        }), true);

        facade.initScheduleThreadPool(Executors.newScheduledThreadPool(2));
        long c = System.currentTimeMillis();
        Scheduler scheduler = Scheduler.create("com/test/units")
                .startTime(c + 3000) //3秒后开始执行
                .endTime(c + 7000) //调度执行结束时间点
                .interval(2000, TimeUnit.MILLISECONDS) //执行间隔
                .repeat(5); //执行次数
        //此处的执行次数为5次，但因执行到某次时超出设置的结束时间，故而实际次数将少于5次
        facade.sendMessage(new Message("1000", "让MyCommand接收执行"), scheduler);
    }
}
```

```java
import com.kaka.Startup;
import com.kaka.notice.*;

import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * 本类中使用的activeMQ或RecketMQ均为最新版本
 *
 * @author zkpursuit
 */
public class Remote_Test extends Startup {

    public static void main(String[] args) throws Exception {
        Facade facade = FacadeFactory.getFacade();
        Remote_Test test = new Remote_Test();
        test.scan("kaka.test.unit");
        facade.initThreadPool(Executors.newFixedThreadPool(2));

        //以下通过ActiveMQ消息队列消费处理事件，并获得事件处理结果
        facade.initRemoteMessagePostman(new ActiveMQ("event_exec_before", "event_exec_after")); //此行全局一次设定
        //facade.initRemoteMessagePostman(new RocketMQ("event_exec_before", "event_exec_after"));

        Message message = new Message("20000", "让ResultCommand接收执行");
        IResult<String> result4 = message.setResult("ResultMsg", new AsynLatchResult<>()); //AsynLatchResult可用AsynResult替代
        facade.sendRemoteMessage(message);
//        try {
//            System.out.println("消息队列消费处理事件结果：" + ((AsynLatchResult) result4).get(5, TimeUnit.SECONDS));
//        } catch (TimeoutException ex) {
//            System.out.println("获取结果超时");
//        }
        System.out.println("消息队列消费处理事件结果：" + result4.get()); //一直等待结果
        facade.sendRemoteMessage(new Message("40000", "", (IResult<Object> result) -> {
            String clasz = ((CallbackResult<Object>) result).eventHandlerClass;
            StringBuilder sb = new StringBuilder("消息队列消费处理事件结果异步回调：\t" + clasz + "\t");
            Object resultObj = result.get();
            if (resultObj instanceof Object[]) {
                Object[] ps = (Object[]) resultObj;
                sb.append(Arrays.toString(ps));
            } else {
                sb.append(resultObj);
            }
            System.out.println(sb);
        }));
    }

}
```

```java
package com.test.units;

import com.kaka.notice.Command;
import com.kaka.notice.IResult;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zkpursuit
 */
@Handler(cmd = "30000", type = String.class)
public class FutureCommand extends Command {

    @Override
    public void execute(Message msg) {
        FutureTask<String> ft = new FutureTask<>(() -> {
            Thread.sleep(3000); //模拟耗时操作
            return ">>>>>>>>异步执行结果";
        });
        new Thread(ft).start();
        try {
            IResult result = msg.getResult("ResultMsg");
            if (result != null) {
                result.set(ft.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(FutureCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
```

```java
package com.test.units;

import com.kaka.notice.Command;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

/**
 *
 * @author zkpursuit
 */
@Handler(cmd = "1000", type = String.class)
@Handler(cmd = "2000", type = String.class)
public class MyCommand extends Command {

    @Override
    public void execute(Message msg) {
        System.out.println(MyCommand.class.getTypeName() + " -> execute " + msg.getWhat() + " 绑定的数据：" + msg.getBody());
        //MyProxy proxy = this.getProxy(MyProxy.class);
        //proxy.func();
        //this.sendMessage(new Message("3000", "让MyMediator接收执行"));
    }

}
```

```java
package com.test.units;

import com.kaka.notice.AsynResult;
import com.kaka.notice.Command;
import com.kaka.notice.IResult;
import com.kaka.notice.Message;
import com.kaka.notice.SyncResult;
import com.kaka.notice.annotation.Handler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zkpursuit
 */
@Handler(cmd = "10000", type = String.class)
@Handler(cmd = "20000", type = String.class)
public class ResultCommand extends Command {

    @Override
    public void execute(Message msg) {
        try {
            //模拟耗时操作
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ResultCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
        IResult result = msg.getResult("ResultMsg");
        if (result != null) {
            //必须设置处理结果
            if (result instanceof AsynResult) {
                result.set(">>>>>>>>异步执行结果");
            } else if (result instanceof SyncResult) {
                result.set(">>>>>>>>同步执行结果");
            }
        }
    }

}
```

```java
package com.test.units;

import com.kaka.notice.Mediator;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.MultiHandler;

/**
 *
 * @author zkpursuit
 */
@MultiHandler
public class MyMediator extends Mediator {

    /**
     * 处理感兴趣的事件
     *
     * @param msg 事件通知
     */
    @Override
    public void handleMessage(Message msg) {
        Object what = msg.getWhat();
        String cmd = String.valueOf(what);
        switch (cmd) {
            case "2000":
                System.out.println(MyMediator.class.getTypeName() + " -> handleMessage " + msg.getWhat() + " 绑定的数据：" + msg.getBody());
                break;
            case "3000":
                System.out.println(MyMediator.class.getTypeName() + " -> handleMessage " + msg.getWhat() + " 绑定的数据：" + msg.getBody());
                break;
        }
    }

    /**
     * 申明感兴趣的事件
     *
     * @return 感兴趣的事件
     */
    @Override
    public Object[] listMessageInterests() {
        return new Object[]{"2000", "3000"};
    }

}
```

```java
package com.test.units;

import com.kaka.notice.Proxy;
import com.kaka.notice.annotation.Model;

/**
 *
 * @author zkpursuit
 */
@Model
public class MyProxy extends Proxy {

    public void func() {
        System.out.println("调用了：" + MyProxy.class.getTypeName() + " -> func方法");
    }

}
```

```java
package com.test.unit;

import com.kaka.notice.Command;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

@Handler(cmd = "50000", type = String.class, priority = 1)
public class CallbackCommand1 extends Command {
    @Override
    public void execute(Message msg) {
        this.returnCallbackResult(new Object[]{100, "我爱我家"});
    }
}
```

```java
package com.test.unit;

import com.kaka.notice.Command;
import com.kaka.notice.IResult;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

/**
 * 模拟切面，执行后
 */
@Handler(cmd = "40000", type = String.class, priority = 3)
public class SimulateAopAfterCommand extends Command {
    @Override
    public void execute(Message msg) {
        IResult<Long> execStartTime = msg.getResult("execStartTime");
        long offset = System.currentTimeMillis() - execStartTime.get();
        System.out.println("Aop业务执行耗时：" + offset);
    }
}
```

```java
package com.test.unit;

import com.kaka.notice.Command;
import com.kaka.notice.IResult;
import com.kaka.notice.Message;
import com.kaka.notice.SyncResult;
import com.kaka.notice.annotation.Handler;

/**
 * 模拟切面，执行前
 */
@Handler(cmd = "40000", type = String.class, priority = 1)
public class SimulateAopBeforeCommand extends Command {
    @Override
    public void execute(Message msg) {
        IResult<Long> execStartTime = new SyncResult<>(); //中间变量亦可使用 ThreadLocal 存储
        execStartTime.set(System.currentTimeMillis());
        msg.setResult("execStartTime", execStartTime);
    }
}
```

```java
package com.test.unit;

import com.kaka.notice.Command;
import com.kaka.notice.Message;
import com.kaka.notice.annotation.Handler;

/**
 * 模拟切面
 */
@Handler(cmd = "40000", type = String.class, priority = 2)
public class SimulateAopCommand extends Command {
    @Override
    public void execute(Message msg) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Aop业务执行");
    }
}
```