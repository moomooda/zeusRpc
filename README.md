# zeusRpc

本项目是一个轻量级RPC框架，也是对SPI的一次实践，通过SPI可自定义扩展通信模块、服务管理中心、负载均衡算法。

对于这三个部分，本项目分别提供的默认实现是Netty、ZooKeeper、random算法；并在demo模块里面做了自定义扩展组件的演示，扩展的实现分别是Tomcat、Redis、RoundRobin算法。

**默认的实现功能更丰富**，支持异步调用、通信连接复用、动态感知服务上下线等功能。

先声明：代码参考了  https://github.com/luxiaoxun/NettyRpc

<!-- TOC -->

- [zeusRpc](#zeusrpc)
  - [如何使用](#如何使用)
  - [Rpc 设计](#rpc-设计)
    - [数据传输](#数据传输)
    - [代理实现透明调用](#代理实现透明调用)
    - [Future实现异步调用](#future实现异步调用)
    - [SPI原理和组件接口设计](#spi原理和组件接口设计)
    - [SPI default](#spi-default)
      - [Netty通信](#netty通信)
      - [ZooKeeper服务管理](#zookeeper服务管理)
      - [一致性Hash负载均衡](#一致性hash负载均衡)
    - [SPI demo extension](#spi-demo-extension)
      - [Tomcat通信](#tomcat通信)
      - [Redis服务管理](#redis服务管理)
      - [RoundRobin负载均衡](#roundrobin负载均衡)
  - [功能扩展](#功能扩展)

<!-- /TOC -->

## 如何使用

1. 拷贝项目到本地后，maven编译

   ```
   mvn -U idea:idea -DskipTests
   ```

2. 参考module zeusRpc-demo进行使用

   1.  定义一个接口

      ```Java
      public interface WelcomeService {
          String welcome(String name);
          String welcome(Person person);
      }
      ```

   2. Rpc server 服务提供者

      1. 提供WelcomeServiceI的两个实现HelloImpl和HiImpl并且打上各自的@**RpcService**注解，用于**服务远程暴露**，以HiImpl为例

          ```Java
          // 远程暴露
          @RpcService(value = WelcomeService.class, version = "2.0")
          public class HiImpl implements WelcomeService {
              @Override
              public String welcome(String name) {
                  return "Hi " + name;
              }
              @Override
              public String welcome(Person person) {
                  return "Hi " + person.getFirstName() + " " + person.getLastName();
              }
          }
          ```

      2. 定义一个Spring Config类，用于将远程暴露的HelloImpl对象和HiImpl对象注册到Spring容器

          ```Java
          @ComponentScan(value = "group.zeus.demo.server", includeFilters = {@ComponentScan.Filter(type =  FilterType.ANNOTATION, classes = {RpcService.class})})
          public class ServerApplicationConfiguration {
          }
          ```

      3.  配置application.properties 默认的服务管理中心是ZooKeeper因此配置2181端口

          ```Java
          // 服务暴露地址
          zeusRpc.serviceAddress = 127.0.0.1:18877
          // 服务注册地址
          zeusRpc.registryAddress = 222.28.84.14:2181
          ```
          
      4. 启动Rpc server用于**服务注册**和**服务暴露**

          ```Java
          public class RpcServerBootStrap {
              public static void main(String[] args) {
                AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(RpcServerConfiguration.class, ServerApplicationConfiguration.class);
                // 确保销毁资源
                applicationContext.registerShutdownHook();
              }
          }
          ```
      
   3. Rpc client 服务消费者

         1. 定义一个类WelcomeImpl，打上@**Component**注解，并且利用@**RpcAutowired**注解引用Api接口的实现，根据指定的注解字段**version** = 2.0，WelcomeImpl对象初始化完成后，可以认为已经持有了远程服务端HiImpl的实例，并可以调用这个实例的方法。
         
            ```Java
            @Component
            public class WelcomeImpl {
                // 服务引用
                @RpcAutowired(version = "2.0")
                WelcomeService welcomeService;
            
                public void welcome(String name){
                    System.out.println(welcomeService.welcome(name));
                }
                public void welcome(Person person){
                    System.out.println(welcomeService.welcome(person));
                }
            }
            ```
         
         2. 定义一个Spring Config类，用于注册WelcomeImpl对象到Spring容器
         
            ```Java
            @Configuration
            @ComponentScan("group.zeus.demo.client")
            public class ClientApplicationConfiguration {
            }
            ```
         
        3. 配置application.properties
        
           ```Java
           // 服务发现地址
           zeusRpc.discoverAddress = 222.28.84.14:2181
           ```
           
        4. 启动Rpc client用于**服务发现**和**服务消费**
           
           ```Java
           public class RpcClientBootStrap {
               public static void main(String[] args) {
                   AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(RpcClientConfiguration.class, ClientApplicationConfiguration.class);
                   // 确保销毁资源
                   applicationContext.registerShutdownHook();
                   WelcomeImpl welcome = applicationContext.getBean(WelcomeImpl.class);
                   welcome.welcome(new Person("dazhan", "mao"));
                   welcome.welcome("maodazhan");
           ```
        
   4. 自定义扩展组件
      1. demo里面提供了三个扩展实现的样例，分别是tomcat、roundRobin、redis
      [![DvfT1K.md.png](https://s3.ax1x.com/2020/12/07/DvfT1K.md.png)](https://imgchr.com/i/DvfT1K)
         
      2. 如果要切换使用扩展的组件来实现Rpc，需要在application.properties增加如下配置，**其他都不需要变**

         ```Java
         // 如果要替换掉通信组件，服务端和消费端都要替换
         zeusRpc.IConsumer = tomcat
         zeusRpc.IProvider = tomcat
         // 如果要替换掉服务管理组件，需要对端口进行修改2181->6379
         zeusRpc.INameService = redis
         zeusRpc.registryAddress = 222.28.84.14:6379
         zeusRpc.discoverAddress = 222.28.84.14:6379
         // 如果要替换负载均衡算法    
         zeusRpc.ILoadBalance = roundrobin
         ```
      3. 如果用户不满足于默认实现和demo里面的扩展实现，可以仿照扩展实现根据**SPI接口**自定义组件，并且要在**classpath:META-INF/SPI**下对自定义的组件进行注册，如下
          [![Dv5CwQ.md.png](https://s3.ax1x.com/2020/12/07/Dv5CwQ.md.png)](https://imgchr.com/i/Dv5CwQ)

      4. 最后只需要和b一样对application.properties做下修改，即可完成对组件的替换
## Rpc 设计

### 数据传输

- 通信协议 **RpcProtocol**

  RpcProtocol主要封装了两部分信息**服务地址**和**服务内容**，RpcProtocol必须是可序列化的，本项目采用**fastJson**序列化和反序列化RpcProtocol，原因时**Json具备很好的可读性**，在服务管理中心上可以查看RpcProtocol。

  RpcProtocol在Rpc server服务注册的时候将被序列化到服务管理中心上，在Rpc client服务发现的时候将被反序列化获取到client本地用于负载均衡，并对select的服务地址发起真正的Rpc通信。
  
  - 服务地址 ip:host​
  - 服务内容 **接口名**和**版本号** 版本号将用于区分同一个接口下的不同实现，字符串"接口名#版本号"将作为一个Key，判别是否是同一个服务，通过获取到指定Key的所有RpcProtocol，才开始负载均衡算法的计算。
  
- Rpc请求和响应

  ​	Rpc请求和响应分别用RpcRequest和RpcResponse进行封装，它们必须是可序列化的，本项目采用**ProtoStuff**序列化和反序列化RpcRequest和RpcResponse，原因是易用，性能强劲，二进制协议非常安全。其他如ProtoBuff、Thrift等二进制协议也不错。

  - **RpcRequest**
    - requestId 请求序列号 UUID生成，用于支持Future的异步【后续再介绍】
    - className 接口名
    - methodName 方法名
    - parameterTypes 参数类型
    - parameters 参数值
    - version 版本号
  -  **RpcResponse**
    - requestId 请求序列号 
    - error 报错信息
    - result 响应结果

### 代理实现透明调用

当Rpc client在本地调用一个接口的方法时，其实本地并没有该接口对应的实现类。一般来说，没有实现类就没有方法体，没有方法体就不知道如何执行。

Rpc通过**代理拦截接口方法的调用**，去和可以执行这个方法的远程服务器进行Rpc通信，从而获取到执行结果，对于调用方来说，这个远程通信的过程是透明的。

本项目使用JDK动态代理实现透明调用

```Java
for (Field field: fields){
    RpcAutowired rpcAutowired = field.getAnnotation(RpcAutowired.class);
    if (rpcAutowired!=null){
        String version = rpcAutowired.version();
        field.setAccessible(true);
        field.set(bean, createProxy(field.getType(), version));
   	}
}
```

Spring在初始化RpcClient的时候，会为容器内所有试图通过使用@**RpcAutowired**注解实现属性注入的Java Bean注入一个代理对象，

这个代理对象内部封装了一个InvocationHandler对象，当对应接口的方法被调用的时候，InvocationHandler对象会对方法进行拦截,执行invoke方法。

```Java
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	// 对Object类方法equals、toString、hashCode进行过滤
    ...
    // 构造请求体
    ...
    String serviceKey = ServiceUtils.buildServiceKey(this.interfaceClass.getName(), version);
    RpcProtocol rpcProtocol = chooseRpcProtocol(serviceKey);
    Object result =  iConsumer.connect(rpcProtocol, rpcRequest);
    ...
}
```

最后invoke方法把返回结果作为被拦截方法的执行结果返回给调用方。

### Future实现异步调用

异步调用区别于**同步调用**。

前面提到Rpc通过代理实现透明调用，底层封装了远程通信的过程，如果这个远程通信的过程是直接由方法的调用方去完成全部的过程的话，那么就是同步调用，因为调用方必须等到远程通信结束拿到响应结果才可以去干别的事情。

而异步调用是调用方把远程通信的任务委托给让**第三方**（比如**Netty的EventLoop线程**），第三方扔给调用方一个Future，之后第三方去全权负责远程通信的过程，调用方可以去干别的事情，等到调用方需要用到方法的响应结果的时候，再用Future去获取响应结果就行啦。

需要说明的是**这里的异步调用并不是完全异步的**。因为网络IO事件就绪之后，还是由调用方自己去获取结果。

具体说来，在本项目中通过定义一个RpcFuture的类封装了RpcResponse，并且调用方通过RpcFuture#get方法获取对应的RpcResponse，这个get方法是同步阻塞的。

```Java
// 调用方执行get()
public Object get() throws InterruptedException, ExecutionException {
    // 这个1没有特别的含义
    sync.acquire(1);
    if (this.response!=null){
    	return this.response.getResult();
    }
    return null;
}
...
// 第三方执行done()
public void done(RpcResponse response){
    this.response = response;
    // 这个1没有特别的含义
    sync.release(1);
}
```

因为RpcFuture#get调用时，如果第三方还没有把RpcResponse放入RpcFuture，那么调用方将**阻塞**，直到对应的RpcFuture#done方法被执行，此时RpcResponse已经放入RpcFuture，调用方将**被唤醒**，获取到RpcResponse。

而这个阻塞唤醒的同步过程直接通过RetrantLock和Synchronized等常用JUC提供的同步手段完成并不适合，本项目采用JUC提供的**AQS**同步框架来定义一个**同步锁Sync**。

它继承了**AbstractQueuedSynchronizer**，重写了tryAcquire方法和tryRelease方法。

RpcFuture#get方法中执行sync.acquire(1)时内部会通过tryAcquire(1)判断当前的**state**是否为1，如果是1，tryAcquire(1)将返回true；否则tryAcquire(1)返回false，调用方线程将进入**CLH队列**。

RpcFuture#done方法中执行sync.release(1)时内部会通过tryRelease(1)通过判断当前的**state**是否为0，如果ture，则通过CAS(expect=0, update=1)修改**state**的状态(正常情况下肯定都是true，因为state初始化就是0)于是**state**的状态被改变了，RpcFuture#done方法执行完毕。

而从时序上来说，state的最新修改状态将立刻对调用方线程可见，从而tryAcquire(1)成功，RpcFuture#get成功获取RpcResponse，因为state是**volatile**变量。

对于第三方来说，它可以为同一个网络连接维护一个Map，key是RpcRequest的requestId，value是对应的RpcFuture。具体来说，

在返回一个RpcFuture给调用方的同时，第三方会把这个<requestId,RpcFuture>保存到Map中；当响应到达后，第三方执行RpcFuture#done方法，并把<requestId,RpcFuture>从Map中移除，**这也是RpcRequest和RpcResponse为何都得持有requestId的原因**。

当然为了支持这种异步调用，第三方必须支持**NIO**，因为如果同时有多个调用方线程的RpcFuture还没就绪，第三方必须同时可以检查多个RpcFuture是否就绪。

前面提到由代理来实现透明调用，代理同时支持NIO的异步调用和BIO的同步调用，在本框架中**Netty**实现了异步调用，**Tomcat**实现了

同步调用。

```Java
if (result instanceof RpcFuture)
    // 异步调用
    return ((RpcFuture) result).get();
else if (result instanceof RpcResponse)
    // 同步调用
    return ((RpcResponse)result).getResult();
else
    throw new RuntimeException("Wrong response object received,Check again...");
```

虽然没有实现，但可以来说明下如何进一步扩展异步调用

- 如何实现**等待可中断**的异步调用

  等待可中断指的是RpcFuture#get方法不能一直阻塞，可以在给定时间内超时返回。

  AQS框架中刚好有个tryAcquireNanos方法，如果tryAcquire(1)没有成功，再后续几次重试后，就放弃等待啦。

- 如何实现**完全异步**调用

  完全异步调用指的是调用方RpcFuture#get方法这一步都不用做了，第三方拿到Future后注册一个**Callback**到Future，Callback封装了调用方如何处理RpcResponse的逻辑，响应到达后，直接由第三方触发Callback，调用方啥都不用管了。

### SPI原理和组件接口设计

Service Provider Interface 服务者提供接口简称 SPI，在JDK内部也有对应的实现，主要用于服务的扩展实现。

SPI机制在很多场景中都有应用，比如数据库连接，JDK在rt.jar中提供了java.sql.Driver接口，这个驱动类在JDK中并没有实现，而是由不同的数据库厂商来实现，比如Oracle、MySQL，这些数据库驱动包都会实现这个接口，举例，当用户call Class.forName("foo.bah.Driver")，JDK利用SPI机制从classpath下找到相应的驱动class进行load和register。

在<<深入理解Java虚拟机>>一书中提过，java.sql.Driver由启动类加载器 Bootstrap ClassLoader 加载，它需要调用回由实现并部署在classpath下SPI的代码，这实际上破坏了双亲委派模型，启动类加载器将通过**线程上下文类加载器**（默认是应用程序类加载器 Application ClassLoader)去加载所需要的SPI代码。

这种插拔式的扩展加载方式，同样遵循一定的协议约定，所有的扩展点必须放在指定目录下，比如resources/META-INF/services目录下。

本项目参考了**Dubbo SPI**的实现，Dubbo SPI并没有使用JDK内置的SPI机制，只是利用了SPI的实现，做了调整和优化，比如自适应扩展，支持IOC和AOP和Spring集成等等。

SPI的实现其实很简单，**反射+缓存+必要同步手段**（确保单例）就可以实现。

当你想要一个接口的扩展实例时，就去缓存取，有直接返回，缓存没有呢，就创建。

如果要创建，最起码得先拿到扩展类的class，于是去缓存取扩展类class，取到后反射调用无参构造器创建扩展类实例，存到缓存，返回。

如果没有扩展类class，就去Load 扩展类class。如何load class呢，先用Class Loader根据接口名在resources/META-INF/SPI目录下一次性获取所有的resources，用 JDK IO库读取每一个resource的内容，找到扩展类的全限定名，利用Class.forName完成对扩展类的加载，并保存到缓存，该接口的所有扩展类class都将被存入缓存，之后就是前面的步骤了。

本项目设计了4个接口用于支持Rpc的组件扩展，@**SPI**注解将指定**默认的扩展**实现，这四个接口**彼此独立，没有耦合**。

- 服务通信客户端接口

  ```Java
  @SPI("netty")
  public interface IConsumer {
      /**
       * 和服务端建立连接
       * @param rpcProtocol 有效地址连接
       * @param request rpc请求
       * @return 返回结果
       */
      Object connect(RpcProtocol rpcProtocol, RpcRequest request) throws Exception;
      /**
       * 关闭连接
       */
      void stop() throws Exception;
  }
  ```

- 服务通信服务端接口

  ```Java
  @SPI("netty")
  public interface IProvider {
      /**
       * 启动服务端
       * @param serviceAddress 暴露服务的地址
       * @param handleMap 暴露的服务信息map
       * @throws Exception
       */
      void start(String serviceAddress, Map<String, Object> handleMap) throws Exception;
  
      /*关闭服务端*/
      void stop() throws Exception;
  }
  ```

- 服务管理接口

  ```Java
  @SPI("zookeeper")
  public interface INameService {
      /**
       * 开启客户端，服务注册
       * @param registryAddress 注册中心地址ip:host
       * @param serviceAddress 服务暴露地址ip:host
       * @param serviceMap serviceKey : serviceBean
       */
      void register(String registryAddress, String serviceAddress, Map<String, Object> serviceMap);
      /**
       * 开启客户端，服务发现
       * @param discoverAddress 注册中心地址ip:host
       */
      void discover(String discoverAddress);
      /*获取最新的RpcProtocols*/
      List<RpcProtocol> getNewestProtocols();
      /*关闭客户端*/
      void stop();
  }
  ```

- 负载均衡接口

  ```Java
  @SPI("random")
  public interface ILoadBalance {
      /**
       * 负载均衡
       * @param serviceKey 接口名+"#"+版本号
       * @param rpcProtocols 同一个serviceKey对应的服务地址
       * @return 负载均衡完返回选择的服务地址
       */
      RpcProtocol route(String serviceKey, List<RpcProtocol> rpcProtocols);
  }
  ```

由于本项目SPI和Spring做了集成，用户可以通过resources/application.properties文件配置指定的扩展，前面演示过。

具体实现以服务管理接口的扩展点为例：

```Java
// Spring集成SPI
@Value("${zeusRpc.INameService}")
private String nameservice;
@Bean
public INameService iNameService(){
    if (!nameservice.equals("${zeusRpc.INameService}"))
        // 自定义实现
    	return SpiFactory.getExtension(INameService.class, nameservice);
    else
        // 默认实现
    	return SpiFactory.getExtension(INameService.class, "default");
}
```

如果用户对zeusRpc.INameService做了配置，Spring注入用户指定的扩展类。如果用户没有配置，Spring将注入默认的扩展类实现。

### SPI default

在module  zeus-spi-default里分别基于SPI接口提供了Netty、ZooKeeper、Random的实现。

#### Netty通信

- **拆包序列化**

  本项目使用Netty提供的基于长度的解码器**LengthFieldBasedFrameDecoder**来解决tcp粘包问题。

  maxFrameLength = 64 * 1024

  lengthFieldOffset = 0 

  lengthFieldLength = 4

  lengthAdjustment = 0

  initialBytesToStrip = 0

  发送数据包的长度 = 长度域的值 + lengthFieldOffset + lengthFieldLength + lengthAdjustment

  = 长度域的值

  **一个消息由Netty节点的发送到接收，经过的编解码器按顺序是**

   1. **发送方的RpcEncoder**。它将利用ProtoStuff对消息RpcRequest/RpcResponse进行序列化处理，写入ByteBuf，ByteBuf的头部是4字节的消息体长度值，尾部才是实际的消息体

        ```Java
        // 序列化
        byte[] data = serializer.serialize(msg);
        // 写入一个int，刚好是4个字节
        out.writeInt(data.length);
        out.writeBytes(data);
        ```
        
    2. **接收方的LengthFieldBasedFrameDecoder**。它将把粘在一起的tcp包拆成一个个头部为**4字节的长度域**+尾部为长度域值那么长的**消息体**
  
    3. **接收方的RpcDecoder**。它将忽略ByteBuf的前4个字节，把后续的字节序列反序列化成一个消息RpcRequest/RpcResponse。
  
        ```Java
        // 跳过前4个字节，刚好是一个int
        int dataLength =  in.readInt();
        ...
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        Object obj = null;
        try{
            // 反序列化
            obj = serializer.deserialize(data, genericClass);
            ...
        }
        ```
  
- **Channel复用**

  Channel复用指的是基于同一个RpcProtocol的连接用完不要急着销毁，留给后续可能的Rpc请求使用，具体说来，是通过**缓存**实现。

  每当需要进行一次Rpc通信的时候，先去缓存查看这个RpcProrocol是否有可用的Channel，如果有就不需要创建新的Channel，如果没有，就创建一个，保存到缓存中。

  如果一个Channel一直没有得到重新使用，那么保存在缓存中就是浪费。

  有**两种考虑方向**。

  - 设定一个缓存空间最大值，采用**LRU**最近最久未使用算法淘汰缓存，具体说来这个缓存是一个线程安全的LRU MAP。此时需要依
  
    赖后面提到的心跳管理，让客户端在连接在空闲时间阈值到达及时发心跳包给服务端，避免服务端回收这个连接
  
  - 利用心跳管理对空闲时间进行监控，设定一个空闲连接时间阈值，让客户端在阈值到达时主动释放连接，把Channel从缓存中淘汰
  
  可以看出Channel复用需要一定的心跳管理做配合，**目前本项目实现的第二种**。
  
- **心跳管理**

  为什么要做心跳管理？

  - 对服务端来说，因为处理的连接数非常多，要检测空闲连接和异常连接，并及时释放资源。于是需要定一个空闲时间阈值t1，当服务端检测到一个Channel已经空闲t1，回收连接。

  - 对客户端来说，是为了**检测连接可用性**及时关闭和**复用Channel**。客户端也定一个空闲时间阈值t2（t1比t2大得多），当客户端检测到一个Channel已经空闲t2，给服务端发送一个心跳包，让服务端的空闲计时器置0。如果客户端发现这个心跳包发送失败了，客户端会及时关闭这个Channel，并且从缓存中清除这个Channel。极端情况下，Channel空闲且服务端连续几次都收不到心跳包，这时候Channel又还没有断，那么服务端将直接回收掉这个Channel。
  
  如果Channel复用的时候，缓存是采用LRU淘汰算法，那么心跳管理的逻辑就是以上。
  
如果没有采用LRU，缓存会越来越大，而很多Channel实际上一直空闲一直在发心跳包，非常浪费。
  
  可以设定一个更大的空闲时间阈值t3，比如令t3 等于10倍 t2，当客户端连续发了10次心跳包来维持这个Channel的时候，这个Channel就不值得再被缓存，客户端将主动关闭这个Channel，从缓存中清除。

  ```Java
// 发送心跳包的时间间隔
  public static final int BEAT_INTERVAL = 30;
// 服务端空闲时间阈值
  public static final int BEAT_TIMEOUT = 3 * BEAT_INTERVAL;
// 连续10次发送心跳包
  public static final int BEAT_EXPIRITION = 10 * BEAT_INTERVAL;
```
  
  t1、t2、t3分别对应BEAT_TIMEOUT、BEAT_INTERVA、BEAT_EXPIRITION。
  
  
  关于心跳管理的其他原因，说的比较笼统和底层，知乎来的
  - Channel连接基于tcp，tcp的超时时间较长，无法及时给应用层及时的反馈。
  
  - 另外虽然tcp有keep alive机制，但是那只能说明连接还在，不能说明基于连接的服务可用，比如服务在某个处理阻塞了。应
  
    用层并不是Socket事件的真正执行者，出问题时非常可能出现和内核状态不一致，因此需要在应用层实现心跳来实现
  
    Channel连接的keep alive。
  
  - 心跳包可以携带更多的状态
  
  Netty提供了**IdleStateHandler**用于Netty客户端和服务端的心跳管理。
  
  IdleStateHandler将在阈值时间到达后，触发一个IdleSateEvent事件来调用fireUserEventTriggered()方法。
  
  服务端的Channnel或者客户端的入站事件处理器将调用userEventTriggered()方法对IdleSateEvent事件进行处理。
  
  ```Java
  expire += Beats.BEAT_INTERVAL;
  // 空闲时间超过 10 * BRAT_TIMEOUT
  if (expire > Beats.BEAT_EXPIRITION) {
      ctx.channel().close();
      VALID_CONNECT_NODES.remove(rpcProtocol);
      logger.info("Idle connection on rpcProtocol: {}:{} removed from connection pool", 				rpcProtocol.getHost(), rpcProtocol.getPort());
  }
  else {
      // Send ping
      handle(Beats.BEAT_PING);
      logger.debug("Client send beat-ping to " + remotePeer);
  }
  ```
  
  客户端的userEventTriggered()方法分为两种情况处理。
  
  一种是连接空闲，但空闲时间不超过**BEAT_EXPIRITION**，发送心跳包即可。
  
  一种是空闲时间超过**BEAT_EXPIRITION**，停止发送心跳包，关闭连接，缓存清除对应Channel。
  
  ```Java
  // 直接关掉
  ctx.channel().close();
  logger.warn("Channel idle last {} seconds, close it", Beats.BEAT_TIMEOUT);
  ```
  
  服务端的userEventTriggered()方法比较简单，直接关闭Channel即可。

#### ZooKeeper服务管理

ZooKeeper管理服务端注册的RpcProtocol信息，客户端从ZooKeeper读取RpcProrocol信息，从而客户端能够直到服务端的地址，从而进行负载均衡和远程通信。

- 服务注册

  ZooKeeper有个临时节点**EPHEMERAL**的特性，ZooKeeper和服务端通过心跳维持一个Session，一旦服务端下线，这个ZNode也自动被清除，从而实现了**自动感知下线**。

- 服务发现

  ZooKeeper有个**Watcher机制**的特征，客户端可以在服务路径ZK path上注册一个Watcher，监听Children Node的3种事件update/create/delete ，从而客户端能够及时获取到最新的服务信息，实现了**自动感知上线**。由于ZooKeeper原生的客户端实现的Watcher注册只能用一次，因此本项目采用Curator，它利用视图机制避免了**反复注册Watcher**。

### SPI demo extension

module  zeus-demo里分别基于SPI接口提供了Tomcat、Redis、一致性Hash的实现。这部分的功能实现比较简单。

#### Tomcat通信

tomcat直接使用Http来建立Rpc客户端和服务端之间的通信。

- 服务端

  使用**Http1.1 NIO**模式，对Rpc请求进行处理是非阻塞的。

- 客户端

  由调用方直接发起Http通信和服务端建立连接，不委托第三方，是**完全同步调用**。

#### Redis服务管理

ZooKeeper是在一个ZK path下用一个Znode保存一个RpcProtocol，这里Redis使用一个列表List的每一个item保存RpcProtocol。

Redis没有直接能够使用的可以动态感知服务上下线的特性，需要**客户端和服务端自己维护**。

## 功能扩展
- [ ] SPI的扩展注册实现IOC
- [ ] 引入微服务、服务治理的思想
- [ ] 支持多线程的高效的LRU MAP






