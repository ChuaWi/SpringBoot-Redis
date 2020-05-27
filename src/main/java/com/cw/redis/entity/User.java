package com.cw.redis.entity;

import java.io.Serializable;

/**
 * @author XiaoChai
 * @date 2020-05-27
 */

public class User implements Serializable {

    private static final long serialVersionUID = -1L;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
