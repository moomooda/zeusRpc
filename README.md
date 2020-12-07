# zeusRpc

本项目是一个轻量级RPC框架，也是对SPI的一次实践，通过SPI可自定义扩展通信模块、服务管理中心、负载均衡算法。对于这三个部分，本项目分别提供的默认实现是Netty、ZooKeeper、一致性Hash算法；并在demo模块里面做了自定义扩展组件的演示，扩展的实现分别是Tomcat、Redis、RoundRobin算法。

<!-- TOC -->

- [zeusRpc](#zeusrpc)
  - [如何使用](#如何使用)
  - [Rpc 设计](#rpc-设计)
    - [数据传输](#数据传输)
    - [代理实现透明调用](#代理实现透明调用)
    - [Future实现异步调用](#future实现异步调用)
    - [SPI组件接口设计和原理](#spi组件接口设计和原理)
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

      1. 提供WelcomeServiceI的两个实现HelloImpl和HiImpl并且打上各自的**@RpcService**注解，用于**服务远程暴露**，以HiImpl为例

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

         1. 定义一个类WelcomeImpl，打上**@Component**注解，并且利用**@RpcAutowired**注解引用Api接口的实现，根据指定的注解字段**version** = 2.0，WelcomeImpl对象初始化完成后，可以认为已经持有了远程服务端HiImpl的实例，并可以调用这个实例的方法。
         
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

      4. 最后只需要和2一样对application.properties做下修改，即可完成对组件的替换
## Rpc 设计

### 数据传输

- 协议
- Rpc请求
- Rpc响应

### 代理实现透明调用

### Future实现异步调用

### SPI组件接口设计和原理

### SPI default	

#### Netty通信

#### ZooKeeper服务管理

#### 一致性Hash负载均衡

### SPI demo extension

#### Tomcat通信

#### Redis服务管理

#### RoundRobin负载均衡

## 功能扩展
- [ ] SPI的扩展注册实现依赖注入
- [ ] 引入微服务的思想






