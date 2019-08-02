package com.chengziweather.hoperun.mipush;

import android.content.Context;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

public class Mipush_Broadcast extends PushMessageReceiver {

    //透传消息到达客户端时调用
    //作用：可通过参数miPushMessage从而获得透传消息，具体请看官方SDK文档
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        super.onReceivePassThroughMessage(context, miPushMessage);
        //打印消息,方便测试
        System.out.println("透传消息到达了");
        System.out.println("透传消息是"+miPushMessage.toString());
    }

    //通知消息到达客户端时调用
    //注：应用在前台时不弹出通知的通知消息到达客户端时也会回调函数
    //作用：通过参数miPushMessage从而获得通知消息，具体请看官方SDK文档
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageArrived(context, miPushMessage);
        System.out.println("通知消息到达了");
        android.util.Log.e("forrest","通知消息到达了");
        System.out.println("通知消息是"+miPushMessage.toString());
    }

    //用户手动点击通知栏消息时调用
    //注：应用在前台时不弹出通知的通知消息到达客户端时也会回调函数
    //作用：1. 通过参数miPushMessage从而获得通知消息，具体请看官方SDK文档
    //2. 设置用户点击消息后打开应用 or 网页 or 其他页面
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
        super.onNotificationMessageClicked(context, miPushMessage);
        System.out.println("用户点击了通知消息");
        System.out.println("通知消息是" + miPushMessage.toString());
        System.out.println("点击后,会进入应用" );
    }

    //用来接收客户端向服务器发送命令后的响应结果。
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onCommandResult(context, miPushCommandMessage);
        String command = miPushCommandMessage.getCommand();
        System.out.println(command);

        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS) {
                //打印信息便于测试注册成功与否
                android.util.Log.e("forrest","注册成功");
                System.out.println("注册成功");
            } else {
                android.util.Log.e("forrest","注册失败");
                System.out.println("注册失败");
            }
        }
    }

    //用于接收客户端向服务器发送注册命令后的响应结果。
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        super.onReceiveRegisterResult(context, miPushCommandMessage);
        String command = miPushCommandMessage.getCommand();
        System.out.println(command );

        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS) {

                //打印日志：注册成功
                System.out.println("注册成功");
            } else {
                //打印日志：注册失败
                System.out.println("注册失败");
            }
        } else {
            System.out.println("其他情况"+miPushCommandMessage.getReason());
        }
    }
}
