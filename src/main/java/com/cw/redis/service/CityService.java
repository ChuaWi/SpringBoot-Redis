package com.cw.redis.service;

import com.cw.redis.domain.City;

/**
 * @author
 * @date 2020-05-27
 */

public interface CityService {
    /**
     * 根据城市 ID,查询城市信息
     *
     * @param id
     * @return
     */
    City findCityById(Long id);

    /**
     * 根据城市 ID,删除城市信息
     *
     * @param id
     * @return
     */
    Long deleteCity(Long id);
}