# kaka-core

#### 介绍
模块为全局事件驱动框架，无任何第三方依赖；支持同步或者异步获取事件处理结果；可解耦业务，简化程序复杂性，提高代码可读性，降低开发维护成本。

#### 软件架构
基于观察者和命令模式，


#### 安装教程

1.  直接获得源码使用
2.  maven安装
	<dependency>
        <groupId>io.github.zkpursuit</groupId>
        <artifactId>kaka-core</artifactId>
        <version>1.0</version>
    </dependency>

#### 使用说明

1. 通过Startup.scan方法扫描指定包下的Command、Proxy、Mediator子类并将其注册到Facade中，Command、Proxy、Mediator亦可直接使用Facade对应的方法手动注册；由Facade处理事件流向。
2. Command、Mediator一般作为业务处理器处理业务，Proxy为数据模型（比如作为数据库service层），Command、Mediator中可通过getProxy方法获得Proxy数据模型。
3. Command只能监听注册到Facade中的事件，可多个事件注册同一个Command（也可理解为一个Command可监听多个事件），而Mediator则是监听多个自身感兴趣的事件，具体对哪些事件感兴趣则由listMessageInterests方法的返回值决定（总结：一个事件只能对应一个Command，一个Command可以对应多个事件；一个事件可以对应多个Mediator，一个Mediator可以对应多个事件；一个事件可以同时对应一个Command和多个Mediator；Command为动态创建，但可池化，Mediator为全局唯一）；Command、Mediator是功能非常相似的事件监听器和事件派发器。
4. Command、Proxy、Mediator中都能通过sendMessage方法向外派发事件，也可在此框架之外直接使用Facade实例调用sendMessage派发事件。
5. 此框架的事件数据类型尽可能的使用int和String。
6. Facade实例在调用initThreadPool方法配置了线程池的情况下，Facade、Command、Proxy、Mediator的sendMessage都将直接支持异步派发事件，默认为同步。
7. 统一同步或者异步获得事件处理结果，异步获取事件结果以wait、notifyAll实现。应该尽可能的少使用此方式，而改用派发事件方式。
8. 实现aop功能基础框架，需配合kaka-aopwear使用。
