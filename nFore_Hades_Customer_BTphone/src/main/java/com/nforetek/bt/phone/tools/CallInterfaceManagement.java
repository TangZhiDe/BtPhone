package com.nforetek.bt.phone.tools;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.phone.CallingActivity;
import com.nforetek.bt.phone.IncomingActivity;
import com.nforetek.bt.phone.MyApplication;
import com.nforetek.bt.phone.R;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.phone.service_boardcast.CallService;

import java.util.List;

/**
 * 这是管理通话界面的工具类
 * @author tzd
 *
 * @version 1.0
 */

public class CallInterfaceManagement {

    private static String version = "1.1";
    private static String TAG = CallInterfaceManagement.class.getCanonicalName()+version;

    public final static int SHOW_TYPE_IN = 0;//来电
    public final static int SHOW_TYPE_DIALOG = 1;//显示弹窗
    public final static int SHOW_TYPE_Activity = 2;//显示Activity
    public final static int SHOW_TYPE_OUT = 3;//去电
    public final static int SHOW_TYPE_YUAN = 4;//显示弹窗
    private static CallInterfaceManagement mModel;
    private Context context;

    private BtPresenter btPresenter;
    public static CallInterfaceManagement getCallInterfaceManagementInstance() {
        if (mModel == null) {
            synchronized (CallInterfaceManagement.class) {
                if (mModel == null) {
                    mModel = new CallInterfaceManagement();
                }
            }
        }
        return mModel;
    }
    public void setParms(Context context,BtPresenter btPresenter){
        this.context = context;
        this.btPresenter = btPresenter;
    }

    public void showCallInterface(Context context,int type){
        if(CallService.backCarState){
            Log.d(TAG, "showCallInterface: 正在倒车，不显示通话界面");
            return;
        }
        boolean isBtAtTop = getTopAppPackageName(context);
        switch (type){
            case SHOW_TYPE_IN:
                if(isBtAtTop){
                    Log.d(TAG, "showCallInterface: ----------------来电显示CallingActivity-------------");
                    if(IncomingActivity.bTphoneCallActivity == null){
                        Intent intent = new Intent();
                        intent.setClass(context, CallingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }

                }else {
                    Log.d(TAG, "showCallInterface: ----------------来电显示Window-------------");
                    myHandler.sendEmptyMessage(0);
                }
                break;
            case SHOW_TYPE_OUT:
                if(isBtAtTop){
                    List<NfHfpClientCall> hfpCallList = null;
                    try {
                        hfpCallList = btPresenter.getHfpCallList();
                        if(hfpCallList != null && hfpCallList.size()>0){
                            if (MyApplication.callingActivity == null) {
                                Log.d(TAG, "------------显示Activity-----去电----------");
                                Intent intent = new Intent();
                                intent.setClass(context, CallingActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        }

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }else {
                    Log.d(TAG, "showCallInterface: ----------------去电显示Window-------------");
                    myHandler.sendEmptyMessage(0);
                }
                break;
            case SHOW_TYPE_DIALOG:
                ((Activity)context).moveTaskToBack(true);
                myHandler.sendEmptyMessage(0);
                break;
            case SHOW_TYPE_YUAN:
                myHandler.sendEmptyMessage(0);
                break;
            case SHOW_TYPE_Activity:
                WindowDialog instance = WindowDialog.getInstance();
                if(instance != null){
                    instance.dismiss();
                }
                List<NfHfpClientCall> hfpCallList = null;
                try {
                    hfpCallList = btPresenter.getHfpCallList();
                    if(hfpCallList != null && hfpCallList.size()>0){
                        NfHfpClientCall call = hfpCallList.get(0);
                        if(call.getState() ==NfHfpClientCall.CALL_STATE_INCOMING){
                            setTopApp(true);
                        }else {
                            setTopApp(false);
                        }
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }

    }



    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    List<NfHfpClientCall> hfpCallList = null;
                    try {
                        hfpCallList = btPresenter.getHfpCallList();
                        if(hfpCallList != null && hfpCallList.size()>0){
                            Log.d(TAG, "handleMessage: hfpCallList size="+hfpCallList.size());
                            WindowDialog instance = WindowDialog.getInstance(context);
                            if(!instance.mIsShow){
                                instance.show();
                            }
                        }

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
            }

            return false;
        }
    });

    private void show(boolean isBtAtTop){

    }


    /**
     * 获取前台应用
     */
    public static boolean getTopAppPackageName(Context context) {
        try {
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
            if (!rti.isEmpty()) {
                String packageName = rti.get(0).topActivity.getPackageName();
                Log.d(TAG, "getTopAppPackageName:topActivity==== " + rti.get(0).topActivity.getClassName());
                Log.d(TAG, "getTopAppPackageName:packageName==== " + packageName);
                if (packageName.equals(context.getPackageName())) {
                    return true;
                }
            }
        } catch (Exception ignored) {

        }
        return false;
    }

    /**
     * 将本应用置顶到最前端
     * 当本应用位于后台时，则将它切换到最前端
     *
     *
     */
    public  void setTopApp(boolean isIncomming) {
//        if (!getTopAppPackageName(context)) {
            /**获取ActivityManager*/
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            /**获得当前运行的task(任务)*/
            List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
            boolean isYou = false;
            if(isIncomming){
                for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                    /**找到本应用的 task，并将它切换到前台*/
                    if (taskInfo.topActivity.getClassName().equals(context.getPackageName()+".IncomingActivity")) {
                        Log.d(TAG, "setTopApp: "+taskInfo.topActivity.getClassName());
                        activityManager.moveTaskToFront(taskInfo.id, 0);
                        Log.d(TAG, "setTopApp: 将CallingActivity切换到前台" );
                        isYou = true;
                        break;
                    }
                }
            }else {
                for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                    /**找到本应用的 task，并将它切换到前台*/
                    if (taskInfo.topActivity.getClassName().equals(context.getPackageName()+".CallingActivity")) {
                        Log.d(TAG, "setTopApp: "+taskInfo.topActivity.getClassName());
                        activityManager.moveTaskToFront(taskInfo.id, 0);
                        Log.d(TAG, "setTopApp: 将CallingActivity切换到前台" );
                        isYou = true;
                        break;
                    }
                }
            }

            if(!isYou){
                Log.d(TAG, "setTopApp: 启动CallingActivity" );
                if(isIncomming){
                    Intent intent = new Intent();
                    intent.setClass(context, IncomingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }else {
                    Intent intent = new Intent();
                    intent.setClass(context, CallingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }

            }
//            if(isIncomming){
//                Intent intent = new Intent();
//                intent.setClass(context, IncomingActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//            }else {
//                Intent intent = new Intent();
//                intent.setClass(context, CallingActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//            }
//        }
    }


}
