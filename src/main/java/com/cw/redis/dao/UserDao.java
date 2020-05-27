package com.cw.redis.dao;

import com.cw.redis.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * @author XiaoChai
 * @date 2020-05-27
 */

@Component(value = "UserDao")
public interface UserDao {

    /**
     * 根据用户ID，获取用户信息
     *
     * @param id
     * @return
     */
    User findById(@Param("id") Long id);

    /**
     * 根据用户ID，删除用户信息
     *
     * @param id
     * @return
     */
    Long deleteUser(Long id);
}
