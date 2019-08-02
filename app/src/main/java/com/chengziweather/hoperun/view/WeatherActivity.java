package com.chengziweather.hoperun.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chengziweather.hoperun.R;
import com.chengziweather.hoperun.gson.Forecast;
import com.chengziweather.hoperun.gson.Weather;
import com.chengziweather.hoperun.service.AutoUpdateService;
import com.chengziweather.hoperun.utils.HttpUtil;
import com.chengziweather.hoperun.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//My key of Hefeng is:b14b738353a846b7ac53b99565700930
public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    private Button navButton;
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         *实现背景图与状态栏融合到一起的效果的简单实现方式，要求Android5.0及以上的系统
         *判断之后调用getWindow().getDecorView()获取当前活动的DecorView，在调用它的
         *View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE
         *表示活动的布局会显示在状态栏上面，最后调用setStatusBarColor将状态栏设置为透明色
         */
        if(Build.VERSION.SDK_INT>=21){
            View decorView =getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        /**
         *初始化各控件
         */
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_text);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.aqi_text);
        pm25Text=(TextView)findViewById(R.id.pm25_text);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.sport_text);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        /**
         * 上面获取SwipeRefreshLayout实例后，下面调用setColorSchemeResources方法设置下拉刷新进度条的颜色
         */
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_button);
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        final String weatherId;
        if(weatherString!=null){
            /**
             * 有缓存时直接解析天气数据
             */
            Weather weather=Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
    /**
     *无缓存时先从Intent中取出weather_id,请求数据时先将ScrollView隐藏，不然会出现空界面，从服务器请求天气数据
     */
            weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        /**
         *上面定义了一个weatherId用于记录城市的天气id然后调用setOnRefreshListener设置下拉刷新监听器
         */
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        /**
         *加入滑动菜单的逻辑处理，调用drawerLayout.openDrawer(GravityCompat.START)来打开滑动菜单就可以了，
         * 然后就需要做城市切换后的逻辑了
         */
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(WeatherActivity.this).load(bingPic).dontAnimate().into(bingPicImg);
        }else {
            loadBingPic();
        }
    }

    /**
     *根据天气id请求城市天气信息
     *先是使用参数中传入的天气id和之前申请申请好的APIKey拼装出一个接口地址,然后调用HttpUtil.sendOkHttpRequest()
     * 方法来向该地址发出请求。服务器会将相应城市的天气信息以Json格式返回。然后我们在onResponse()方法中调用
     * Utility.handleWeatherResponse()方法将返回的Json数据转换成Weather对象，再将当前线程切换到主线程。
     * 之后判断服务器返回的status，ok则说明请求成功，将返回的数据缓存到SharedPreferences当中，调用
     * showWeatherInfo()方法进行内容显示
     * */
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+
                weatherId+"&key=b14b738353a846b7ac53b99565700930";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                final Weather weather= Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor= PreferenceManager
                               .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气失败！",Toast.LENGTH_SHORT).show();
                        }
                /**
                *注意请求结束后，设置swipeRefresh.setRefreshing(false)，表示刷新事件结束，隐藏进度条
                */
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气失败！",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this)
                             .load(bingPic).dontAnimate()
                           //  .diskCacheStrategy(DiskCacheStrategy.SOURCE)  缓存策略
                             .into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
   /**
    *处理并展示Weather实体类中的数据
    *从Weather对象中获取数据并显示到相应的控件上，在未来几天天气预报中我们使用了一个for循环来处理每天的天气信息
    *并在循环中动态加载了forecast_item布局并设置相应的数据，然后添加到父布局当中。设置完后将ScrollView设置可见
    **/
    private void showWeatherInfo(Weather weather) {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item
                    ,forecastLayout,false);
            TextView dataText=(TextView)view.findViewById(R.id.data_text);//空指针异常，因为这是定义在布局里的控件。忘记加view了。。。。。。。
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度： "+weather.suggestion.comfort.info;
        String carWash="洗车指数： "+weather.suggestion.carWash.info;
        String sport="运动建议： "+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        /**
         * 在这里激活AutoUpdateService，在这里启动服务，一旦选中了某个城市并成功更新天气后，服务就会一直在后台运行。
         * 并每小时一更新。
         */
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
