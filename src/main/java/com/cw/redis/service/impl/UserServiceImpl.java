package com.cw.redis.service.impl;

import com.cw.redis.dao.UserDao;
import com.cw.redis.entity.User;
import com.cw.redis.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author XiaoChai
 * @date 2020-05-27
 */

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询用户逻辑：
     * 如果缓存存在，从缓存中查询用户信息
     * 如果缓存不存在，从DB中查询用户信息，然后插入缓存，缓存有效时间30秒
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
            LOGGER.info("UserServiceImpl.findUserById() : 从缓存中查询用户 >> " + user.toString());
            long end = System.currentTimeMillis();
            LOGGER.info("查询Redis花费的时间是"+(end-start)+" ms");
            return user;
        }else{
            // 从 DB 中获取用户信息
            long start = System.currentTimeMillis();
            User user = userDao.findById(id);
            // 插入缓存，缓存有效时间30秒
            operations.set(key, user, 30, TimeUnit.SECONDS);
            LOGGER.info("UserServiceImpl.findUserById() : 从 DB 中查询用户 >> " + user.toString());
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
}