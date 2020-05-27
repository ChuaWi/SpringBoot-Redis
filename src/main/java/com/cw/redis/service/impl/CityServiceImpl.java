package com.cw.redis.service.impl;

import com.cw.redis.dao.CityDao;
import com.cw.redis.domain.City;
import com.cw.redis.service.CityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author
 * @date 2020-05-27
 */

@Service
public class CityServiceImpl implements CityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CityServiceImpl.class);

    @Autowired
    private CityDao cityDao;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取城市逻辑：
     * 如果缓存存在，从缓存中获取城市信息
     * 如果缓存不存在，从DB中获取城市信息，然后插入缓存
     */
    public City findCityById(Long id) {
        // 从缓存中获取城市信息
        String key = "city_" + id;
        ValueOperations<String, City> operations = redisTemplate.opsForValue();
        // 缓存存在
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            long start = System.currentTimeMillis();
            City city = operations.get(key);
            LOGGER.info("CityServiceImpl.findCityById() : 从缓存中获取城市 >> " + city.toString());
            long end = System.currentTimeMillis();
            LOGGER.info("查询Redis花费的时间是"+(end-start)+" ms");
            return city;
        }else{
            // 从 DB 中获取城市信息
            long start = System.currentTimeMillis();
            City city = cityDao.findById(id);
            // 插入缓存，缓存有效时间30秒
            operations.set(key, city, 30, TimeUnit.SECONDS);
            LOGGER.info("CityServiceImpl.findCityById() : 从数据库中获取城市 >> " + city.toString());
            long end = System.currentTimeMillis();
            LOGGER.info("查询DB花费的时间是"+(end-start)+" ms");
            return city;
        }
    }

    /**
     * 删除城市逻辑：
     * 如果缓存存在，从缓存中删除城市信息
     * 如果缓存不存在，从DB中删除城市信息
     */
    @Override
    public Long deleteCity(Long id) {
        Long del = cityDao.deleteCity(id);
        // 缓存存在，删除缓存
        String key = "city_" + id;
        boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey) {
            LOGGER.info("CityServiceImpl.deleteCity() : 从缓存中删除城市ID >> " + id);
            redisTemplate.delete(key);
            return del;
        }else {
            LOGGER.info("CityServiceImpl.deleteCity() : 从DB中删除城市ID >> " + id);
            cityDao.deleteCity(id);
        }
        return del;
    }
}