package com.cw.redis.dao;

import com.cw.redis.domain.City;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * @author
 * @date 2020-05-27
 */

@Component(value = "CityDao")
public interface CityDao {

    /**
     * 根据城市 ID，获取城市信息
     *
     * @param id
     * @return
     */
    City findById(@Param("id") Long id);

    /**
     * 根据城市 ID，删除城市信息
     *
     * @param id
     * @return
     */
    Long deleteCity(Long id);
}
