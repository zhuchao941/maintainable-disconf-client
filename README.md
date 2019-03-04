# matainable-disconf-client包

## 背景

1. 当前很多配置都在disconf上，迁移到别的配置中心工作量较大
2. 原生的disconf-client加载配置的姿势不对：时机过于靠后(BeanFactoryPostProcessor)，使用过程中会有很多问题
3. 原生的disconf-client不支持多项目公共配置
4. 原生的disconf-client代码写得太乱，不便于维护
5. 细细去看源代码还会发现更多的问题.....
6. 社区disconf-client已经处于无人维护的状态

## 核心改造点

1. 以正确的姿势加载配置文件(扩展EnvironmentPostProcessor)
2. 原生zookeeper操作改造成使用Curator来操作zookeeper
3. 支持多项目都要用到的公共配置(appName=common/version及env与主项目保持一致)
4. 加入了公司的异地多活配置维度的支持（扩展）
5. 支持其他框架嵌入一些自己的核心配置
5. 保证和原逻辑一致的前提下，尽可能优化代码结构

第一点改造主要基于Spring-Boot整个启动流程，切入的时机就在Spring-Boot加载`application.properties`之后，立马去加载disconf上的相关配置

## 顺带手的改动

除了上面写的核心改造点之外，这次改动还顺带手做了一些优化：

1. 远程文件下载机制从原来的串行，优化成了并行（当然加载顺序是没有变的）
2. 废弃了一些disconf里我们不用的功能（比如配置项config item）
3. disconf自身的配置（通常配置在disconf.properties文件里或者启动参数里的）尽量无须特殊指定，能取的直接取，避免同一个东西多处定义，反而容易造成不一致，增加维护成本。比如原来的disconf.app可以直接取project.name，环境也可以通过spring.active.profiles转换得到
4. 在上传instance信息到zk的时候，节点名称从原来的host+uuid优化成了host+ip+project.name+uuid，便于查看一些公共的配置文件的引用情况
5. 默认加载主项目下的specific.properties文件
6. ZK session timeout从10s->60s

## 整体流程

重构的代码在设计上总共抽象出三种角色：Loader、Downloader、Watcher，分别是：

- Loader：加载配置到环境变量中
- Downloader：从Disconf远端下载配置文件
- Watcher：监听配置文件的变动

![](http://assets.processon.com/chart_image/5c4d2fd1e4b025fe7c8c1bd4.png)

## ZooKeeper相关核心问题点

由于Disconf的配置变更通知使用的是ZooKeeper的Watcher机制，所以对于这块知识还是恶补了一下，这里列一些核心的问题点：

### 是不是每次更新都能通知到对应订阅者

令人失望的是，答案并不是肯定的。下面分析几种场景：

- Watcher自身机制原因：Watcher是one-time-trigger，每次触发之后就失效，需要重新注册。在并发更新同一个节点(比如两次更新介于再次watcher注册成功时间)时，可能导致丢失更新

- 重新注册Watcher的请求执行失败，导致无法再接收到后续的变更通知

- Session过期：ZK服务端长时间（超过Session Timeout）接收不到客户端的心跳(心跳包或者正常的请求)，就认为会话过期了(Session Expired)。这个时候会把该Session拥有的临时节点以及Watcher都删掉。需要注意的是，客户端能感知到这个信息还需要等他能重新连接到ZK才行，否则一直处于懵逼状态（Disconnected）
     >     Session expiration is managed by the ZooKeeper cluster itself, not by the client. When the ZK client establishes a session with the cluster it provides a "timeout" value detailed above. This value is used by the cluster to determine when the client's session expires. Expirations happens when the cluster does not hear from the client within the specified session timeout period (i.e. no heartbeat). At session expiration the cluster will delete any/all ephemeral nodes owned by that session and immediately notify any/all connected clients of the change (anyone watching those znodes). At this point the client of the expired session is still disconnected from the cluster, it will not be notified of the session expiration until/unless it is able to re-establish a connection to the cluster. The client will stay in disconnected state until the TCP connection is re-established with the cluster, at which point the watcher of the expired session will receive the "session expired" notification.


### 如何更好的使用ZooKeeper

1. 使用Curator来而不是原生的ZooKeeper客户端来与服务端交互，Curator能弥补一些原生ZooKeeper的问题

    1. 更友好的API
    2. 每个操作（create, getData, etc.）都用了重试的策略来避免connection loss或session expiration的场景
    3. connection loss的场景能够自动重连

2. 基于Curator的重试机制，基本大部分场景都能保证操作成功，当然也不可能无限重试。但是由于配置的正确性非常重要，所以还需要增加定时任务做一致性比对作为兜底机制。

3. 还是基于配置的正确性非常重要，针对上面Session过期的场景，需要重新添加临时节点以及注册核心Watcher

    ```java
       client.getConnectionStateListenable().addListener((client, newState) -> {
       if ((newState == ConnectionState.CONNECTED) || (newState
               == ConnectionState.RECONNECTED)) {
           if (isConnected.compareAndSet(false, true)) {
               try {
                   DisconfPropertyLoader.getInstance().reload();
                   watch(DisconfConfig.getInstance().getDisconfAppPropList());
               } catch (Exception e) {
                   ThreadUtils.checkInterrupted(e);
                   log.error("Trying to reset after reconnection", e);
               }
           }
       } else {
            // connection loss or session expiration
           isConnected.set(false);
           log.error("disconf zk connection state is not correct:{}", newState.name());
       }
   });
    ```

## 扩展点

1. com.github.zhuchao941.disconf.client.resolver.DisconfAppPropResolver
2. com.github.zhuchao941.disconf.client.watch.IDisconfUpdatePipeline

## One more thing

还开发了一个基于Disconf回调实现重新设值@Value等属性/日志动态级别调整的工具包
