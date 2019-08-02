package com.chengziweather.hoperun.gson;

/**
 * Created by wangdan on 18-4-24.
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
