package com.chengziweather.hoperun.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.chengziweather.hoperun.R;

/**
 * 源代码查看顺序：1.创建数据库和表：配置litepal并创建db对应的实体类。
 *               2.遍历全国省市县：首先在utls创建一个HttpUtil工具类用于与服务器交互，在创建一个Utility工具类
 *                  用于解析Json数据。先建一个choose_area布局文件，新建ChooseAreaFragment继承Fragment
 *                  ，将碎片添加到活动中，修改style文件替换原生标题，添加网络协议。
 *              3.显示天气信息：定义Gson实体类；编写天气界面新建活动weatherActivity(新建title.xml作为头布局，
 *                  新建now作为当前天气信息的布局，新建forecast作为未来几天天气信息的布局，新建aqi作为空气质量
 *                  信息的布局，新建suggestion作为生活建议的布局最后将其引入activity_weather布局文件中)，在
 *                  Utility中添加解析天气Json数据的方法，修改WeatherActivity中的代码，修改chooseareafragment
 *                  添加跳转代码（从省市县数据到天气界面），获取每日必应一图....（之后就是在这三个view文件中添加代码了）
 */
public class MainActivity extends AppCompatActivity {
    /**
     * 加入对缓存数据的判断，若读取过天气数据则没必要再次选择城市，直接跳转到天气界面
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getString("weather",null)!=null){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
