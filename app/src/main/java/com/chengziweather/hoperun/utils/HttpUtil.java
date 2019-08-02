package com.chengziweather.hoperun.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 *Created by hoperun on 18-4-20.
 * 用于和服务器进行交互的工具类，现在我们只需要调用sendOkHttpRequest()方法，传入请求地址，并注册一个回调来处理服务器响应就可以了。
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
