package com.cw.redis.controller;

import com.cw.redis.domain.City;
import com.cw.redis.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author
 * @date 2020-05-27
 */

@RestController
public class CityRestController {

    @Autowired
    private CityService cityService;

    @RequestMapping(value = "/api/city/{id}", method = RequestMethod.GET)
    public City findOneCity(@PathVariable("id") Long id) {
        return cityService.findCityById(id);
    }

    @RequestMapping(value = "/api/city/{id}", method = RequestMethod.DELETE)
    public void deleteCity(@PathVariable("id") Long id) {
        cityService.deleteCity(id);
    }
}