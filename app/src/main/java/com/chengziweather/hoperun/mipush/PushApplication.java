package com.chengziweather.hoperun.mipush;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;

import com.xiaomi.mipush.sdk.MiPushClient;

import org.litepal.LitePalApplication;

import java.util.List;

public class PushApplication extends LitePalApplication {
    // 使用自己APP的ID（官网注册的）,使用自己APP的Key（官网注册的）.
    private static final String APP_ID = "2882303761518091348";
    private static final String APP_KEY = "5471809168348";

    //为了提高推送服务的注册率，我建议在Application的onCreate中初始化推送服务
    //你也可以根据需要，在其他地方初始化推送服务
    @Override
    public void onCreate() {
        super.onCreate();

        if (shouldInit()) {
            //注册推送服务,注册成功后会向DemoMessageReceiver发送广播.
            // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
            android.util.Log.e("forrest","RegID == "+MiPushClient.getRegId(getApplicationContext()));
        }
    }

    //通过判断手机里的所有进程是否有这个App的进程,从而判断该App是否有打开.
    private boolean shouldInit() {
        //通过ActivityManager我们可以获得系统里正在运行的activities
        //包括进程(Process)等、应用程序/包、服务(Service)、任务(Task)信息。
        ActivityManager activityManager = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        String mainProcessName = getPackageName();

        int myPid = Process.myPid();      //获取本App的唯一标识
        //利用一个增强for循环取出手机里的所有进程
        for (ActivityManager.RunningAppProcessInfo info:processInfos) {
            //通过比较进程的唯一标识和包名判断进程里是否存在该App
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
