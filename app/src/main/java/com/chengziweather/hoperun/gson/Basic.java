package com.chengziweather.hoperun.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wangdan on 18-4-24.
 */

public class Basic {
    @SerializedName("city")  //由于json中的一些字段可能不太适合直接作为java字段来命名，使用这注解来建立两者之间的映射关系
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Upadate update;

    public class Upadate {
        @SerializedName("loc")
        public String updateTime;
    }
}
