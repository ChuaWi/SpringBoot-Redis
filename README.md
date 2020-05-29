# SpringBoot封装RedisTemplate实现Redis数据缓存
**Redis**是一个开源的内存数据结构存储，用作数据库，缓存和消息代理。以（key,value）的形式存储数据的数据库，是当前互联网世界最为流行的[ NoSQL](https://baike.baidu.com/item/NoSQL/8828247?fr=aladdin)（Not Only SQL）数据库。主要用Redis实现缓存数据的存储，可以设置过期时间。适合高频读写、临时存储的数据。

Redis 可以存储键与5种不同数据结构类型之间的映射，这5种数据结构类型分别为String（字符串）、List（列表）、Set（集合）、Hash（散列）和 Zset（有序集合）。
| 结构类型 | 结构存储的值 | 结构的读写能力 |
|--|--|--|
| String | 可以是字符串、整数或者浮点数 | 对整个字符串或者字符串的其中一部分执行操作；对象和浮点数执行自增(increment)或者自减(decrement) |
| List | 一个链表，链表上的每个节点都包含了一个字符串 | 从链表的两端推入或者弹出元素；根据偏移量对链表进行修剪(trim)；读取单个或者多个元素；根据值来查找或者移除元素 |
| Set | 包含字符串的无序收集器，并且被包含的每个字符串都是独一无二的、各不相同 | 添加、获取、移除单个元素；检查一个元素是否存在于某个集合中；计算交集、并集、差集；从集合里随机获取元素 |
| Hash | 包含键值对的无序散列表 | 添加、获取、移除单个键值对；获取所有键值对 |
| Zset | 字符串成员(member)与浮点数分值(score)之间的有序映射，元素的排列顺序由分值的大小决定 | 添加、获取、删除单个元素；根据分值范围(range)或者成员来获取元素 |

<code>RedisTemplate</code>中定义了对五种数据结构操作

> redisTemplate.opsForValue(); //操作字符串

> redisTemplate.opsForList(); //操作列表

> redisTemplate.opsForSet(); //操作集合

> redisTemplate.opsForHash(); //操作散列

> redisTemplate.opsForZSet(); //操作有序集合

点击这里>[查看RedisTemplate详细数据结构](https://www.jianshu.com/p/7bf5dc61ca06/)
## 实现Redis数据缓存
**1.引入Redis、Mybatis依赖**
```java
<!-- SpringBoot Redis 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-redis</artifactId>
    <version>${spring-boot-starter-redis-version}</version>
</dependency>
<!-- SpringBoot Mybatis 依赖 -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>${mybatis-spring-boot}</version>
</dependency>
```
**2.application.properties配置文件，添加Redis、Mybatis相关配置**
```java
## Redis 配置
## Redis数据库索引（默认为0）
spring.redis.database=0
## Redis服务器地址
spring.redis.host=127.0.0.1
## Redis服务器连接端口
spring.redis.port=6379
## Redis服务器连接密码（默认为空）
spring.redis.password=123456
## 连接池最大连接数（使用负值表示没有限制）
spring.redis.pool.max-active=8
## 连接池最大阻塞等待时间（使用负值表示没有限制）
spring.redis.pool.max-wait=-1
## 连接池中的最大空闲连接
spring.redis.pool.max-idle=8
## 连接池中的最小空闲连接
spring.redis.pool.min-idle=0
## 连接超时时间（毫秒）
spring.redis.timeout=0

## Mybatis 配置
mybatis.typeAliasesPackage=com.cw.redis.entity
mybatis.mapperLocations=classpath:mapper/*.xml
```
**3.自定义实体User类**
```java
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 学号
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 描述
     */
    private String description;
```
**4.自定义服务类**
```java
public interface UserService {
    /**
     * 根据用户ID,查询用户信息
     *
     * @param id
     * @return
     */
    User findUserById(Long id);
    /**
     * 根据用户ID,删除用户信息
     *
     * @param id
     * @return
     */
    Long deleteUser(Long id);
}
```
**5.业务逻辑实现类**
```java
    /**
     * 查询用户逻辑：
     * 如果缓存存在，从缓存中获取用户信息
     * 如果缓存不存在，从DB中获取用户信息，然后插入缓存
     */
    public User findUserById(Long id) {
        // 从缓存中获取用户信息
        String key = "user_" + id;
        ValueOperations<String, User> operations = redisTemplate.opsForValue();
        // 缓存存在
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            long start = System.currentTimeMillis();
            User user = operations.get(key);
            LOGGER.info("UserServiceImpl.findUserById() : 从缓存中获取用户 >> " + user.toString());
            long end = System.currentTimeMillis();
            LOGGER.info("查询Redis花费的时间是"+(end-start)+" ms");
            return user;
        }else{
            // 从 DB 中获取用户信息
            long start = System.currentTimeMillis();
            User user = userDao.findById(id);
            // 插入缓存，缓存有效时间30秒
            operations.set(key, user, 30, TimeUnit.SECONDS);
            LOGGER.info("UserServiceImpl.findUserById() : 从 DB 中获取用户 >> " + user.toString());
            long end = System.currentTimeMillis();
            LOGGER.info("查询DB花费的时间是"+(end-start)+" ms");
            return user;
        }
    }
    /**
     * 删除用户逻辑：
     * 如果缓存存在，从缓存中删除用户信息
     * 如果缓存不存在，从DB中删除用户信息
     */
    @Override
    public Long deleteUser(Long id) {
        Long del = userDao.deleteUser(id);
        // 缓存存在，删除缓存
        String key = "user_" + id;
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            LOGGER.info("UserServiceImpl.deleteUser() : 从缓存中删除用户ID >> " + id);
            redisTemplate.delete(key);
            return del;
        }else {
            LOGGER.info("UserServiceImpl.deleteUser() : 从 DB 中删除用户ID >> " + id);
            userDao.deleteUser(id);
        }
        return del;
    }
```
**6.请求方法**
```java
    @RequestMapping(value = "/api/user/{id}", method = RequestMethod.GET)
    public User findOneUser(@PathVariable("id") Long id) {
        return userService.findUserById(id);
    }
    @RequestMapping(value = "/api/user/{id}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
    }
```

**下面进行测试，启动项目前，需要先连接Redis服务[点击这里>查看如何连接Redis服务](https://blog.csdn.net/weixin_44316527/article/details/106365859)

然后使用[点击这里下载>postman](https://www.postman.com)测试<font color="red">api/user/1</font>接口**

1.如果缓存不存在，从数据库中查询用户信息，然后插入缓存，缓存有效时间30秒

<font color=green>**GET**</font> http://127.0.0.1:8080/api/user/1

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200529171538382.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDMxNjUyNw==,size_16,color_FFFFFF,t_70)


```java
2020-05-27 17:32:00.129  INFO 5880 --- [nio-8080-exec-1] c.cw.redis.service.impl.UserServiceImpl  : UserServiceImpl.findUserById() : 从 DB 中查询用户 >> User{id=1, userId=12305, userName='蔡小柴', description='就读于国立中央大学资讯工程'}
2020-05-27 17:32:00.129  INFO 5880 --- [nio-8080-exec-1] c.cw.redis.service.impl.UserServiceImpl  : 查询DB花费的时间是251 ms
```

2.如果缓存存在，从缓存中查询用户信息
在缓存时间（30秒）内，再次访问

<font color=green>**GET**</font> http://127.0.0.1:8080/api/user/1

```java
2020-05-27 17:32:07.026  INFO 5880 --- [nio-8080-exec-2] c.cw.redis.service.impl.UserServiceImpl  : UserServiceImpl.findUserById() : 从缓存中查询用户 >> User{id=1, userId=12305, userName='蔡小柴', description='就读于国立中央大学资讯工程'}
2020-05-27 17:32:07.026  INFO 5880 --- [nio-8080-exec-2] c.cw.redis.service.impl.UserServiceImpl  : 查询Redis花费的时间是7 ms
```

<table><tr><td bgcolor=#D1EEEE><font color=blue>可以看到从缓存中直接查询用户信息的时间要比从数据库中快很多。</font></td></tr></table>

3.如果缓存存在，从缓存中删除用户信息

<font color=red>**DELETE**</font> http://127.0.0.1:8080/api/user/1

```java
2020-05-27 17:32:13.251  INFO 5880 --- [nio-8080-exec-3] c.cw.redis.service.impl.UserServiceImpl  : UserServiceImpl.deleteUser() : 从缓存中删除用户ID >> 1
```

4.如果缓存不存在，从数据库中删除用户信息

<font color=red>**DELETE**</font> http://127.0.0.1:8080/api/user/1

```java
2020-05-27 17:32:20.210  INFO 5880 --- [nio-8080-exec-4] c.cw.redis.service.impl.UserServiceImpl  : UserServiceImpl.deleteUser() : 从 DB 中删除用户ID >> 1
```

点击这里>[Github项目源码地址-SpringBoot封装RedisTemplate实现Redis数据缓存](https://github.com/ChuaWi/SpringBoot-Redis)
