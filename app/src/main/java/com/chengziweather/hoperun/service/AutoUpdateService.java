package com.chengziweather.hoperun.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.chengziweather.hoperun.gson.Weather;
import com.chengziweather.hoperun.utils.HttpUtil;
import com.chengziweather.hoperun.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 *加入后台自动更新天气的功能，可以保证用户每次打开软件时看到的都是最新的天气信息，想要实现这个功能就需要创建一个长期在
 * 在后台运行的定时任务，这时就需要使用到服务了
 */
public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

  /**
   *调用updateWeather更新天气，然后调用updateBingPic更新背景图片，在方法内将更新后的数据存储在SharedPreferences
   * 中。然后就是创建定时任务的技巧，8小时后AutoUpdateService的onStartCommand()方法就会重新执行。实现后台定时更新功能。
   */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this, AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * 更新天气信息
     */
    private void updateWeather(){
        SharedPreferences pres= PreferenceManager.
                getDefaultSharedPreferences(this);
        String weatherString=pres.getString("weather",null);
        if(weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            String weatherId=weather.basic.weatherId;
            String weatherUrl="http://guolin.tech/api/weather?cityid="+
                    weatherId+"&key=b14b738353a846b7ac53b99565700930";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();
                    Weather weather= Utility.handleWeatherResponse(responseText);
                    if(weather!=null&&"ok".equals(weather.status)){
                         SharedPreferences.Editor editor= PreferenceManager
                                .getDefaultSharedPreferences(AutoUpdateService.this).edit();
                         editor.putString("weather",responseText);
                         editor.apply();
                    }
                }
            });
        }
    }
    private void updateBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                Log.d("WeatherActivity2"," "+bingPic);
                SharedPreferences.Editor editor=PreferenceManager.
                        getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
}
