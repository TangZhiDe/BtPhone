package com.nforetek.bt.phone.service_boardcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    //重写onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) {
        //后边的XXX.class就是要启动的服务
        Log.v("TAG", "开机自动服务自动启动.....");
        Intent service = new Intent();
        service.setAction("com.nforetek.bt.phone.callService");
        service.setPackage(context.getPackageName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
        Log.v("TAG", "开机自动服务自动启动111.....");
    }
}