## TSMSP-后端模板使用说明

### 前置要求

- 准备一个支持Scala的IDE，推荐使用[Intellij IDEA](https://www.jetbrains.com/idea/download/#section=windows)
- 下载并安装JDK11
- 下载并以默认方式安装[Postgresql](https://www.postgresql.org/)。请安装至少13.0以后的版本。
- 在Postgresql中创建一个超级用户，并以该用户的身份创建一个数据库。然后在src/main/resources/application.conf中，找到tsmsp的properties一栏，将你创建的数据库名称填写在`databaseName`中，将你创建的用户名和密码填写在`user`和`password`中。

完成以上前置要求之后，就可以编译运行我们的后端模板了。

### 后端模板文件结构
> src
> > main 包含了框架中的主要代码
> > > resources 仅存放后端模板的配置文件，一般来讲除了application.conf中的数据库`tsmsp`相关配置以外，其余都不需要改动。
> >
> > > scala 所有scala代码存放的目录
> > > > Exceptions 定义后端模板在运行过程中可能抛出的错误信息
> > > 
> > > > Globals 定义后端模板在运行过程中所需要使用的一些全局变量
> > > 
> > > > Impl 规定好的接口的后端逻辑实现代码
> > > 
> > > > Process 后端在执行过程中的主要过程
> > > 
> > > > Tables 数据库的表结构和数据库交互实现
> > > 
> > > > Utils 一些后端实现中使用的Utilities
> 
> build.sbt 定义了项目的包引用依赖。如果需要加入一些其他的scala或者java的包，请在这个文件中加入。
> 

### 后端模板逻辑简要说明
本项目基于Http协议与前端交互，在Process.TSMSPPortalHttpServer中定义了路由。后端在接到对/api路由的请求后，会将请求内容反序列化为一个Impl.Messages.TSMSPMessage的子类，通过子类中定义的reaction方法进行逻辑处理，并得到一个TSMSPReply，最后将TSMSPReply回传给前端。
这个框架本身是异步的，因此在后续实现别的逻辑接口时，应当注意避免因为异步而产生的错误。