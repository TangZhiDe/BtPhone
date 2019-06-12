package com.nforetek.bt.phone.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.base.jar.NforeBtBaseJar;
import com.nforetek.bt.phone.BtPhoneMainActivity;
import com.nforetek.bt.phone.CallingActivity;
import com.nforetek.bt.phone.IncomingActivity;
import com.nforetek.bt.phone.MyApplication;
import com.nforetek.bt.phone.R;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.res.NfDef;

import java.util.List;

public class WindowDialog implements View.OnClickListener{
//,BtPresenter.UiBluetoothPhoneChangeListerer,BtPresenter.UiBluetoothSettingChangeListerer
    private WindowManager.LayoutParams wmParams;
    private WindowManager wm;
    private Context context;
    private View mView;
    public boolean mIsShow = false;
    private int x;
    private int y;
    private static WindowDialog instance = null;

    private static BtPresenter mBPresenter;
    private ImageView window_answer;
    private ImageView window_decice;
    private ImageView window_finish;
    private TextView window_firName;
    private ImageView window_hangup;
    private ImageView window_keyboard_btn;
    private TextView window_name;
    private LinearLayout window_thz;
    private TextView window_type;
    private ImageView window_yuying;
    private View window_tbd;
    private static AnimationDrawable window_answer_animation;

    private WindowDialog(){

    }
    private WindowDialog(Context context){
        this.context = context;
        this.mBPresenter = MyApplication.mBPresenter;
        this.x = 0;
        this.y = 0;
        mBPresenter.setWindowDialog(this);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_window, null);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        this.mView = view;
        this.init();
        this.initView();
    }

    public static WindowDialog getInstance(Context context){
        if(null == instance){
            Log.d(TAG, "getInstance: 实例化" );
            instance = new WindowDialog(context);
        }
        return instance;
    }

    public static WindowDialog getInstance(){
        Log.d(TAG, "getInstance: " );
        return instance;
    }
    public static void initInstance(){
        if(instance != null){
            Log.d(TAG, "initInstance: ===null" );
            instance.dismiss();
            instance = null;
            mBPresenter.setWindowDialog(null);
            if(window_answer_animation != null && window_answer_animation.isRunning()){
                window_answer_animation.stop();
            }
        }
    }

    private void initView(){
        if(mView == null)
            return;
        window_answer = mView.findViewById(com.nforetek.bt.phone.R.id.window_answer);
        window_decice = mView.findViewById(com.nforetek.bt.phone.R.id.window_decice);
        window_finish = mView.findViewById(com.nforetek.bt.phone.R.id.window_finish);
        window_firName = mView.findViewById(com.nforetek.bt.phone.R.id.window_firName);
        window_hangup = mView.findViewById(com.nforetek.bt.phone.R.id.window_hangup);
        window_keyboard_btn = mView.findViewById(com.nforetek.bt.phone.R.id.window_keyboard_btn);
        window_name = mView.findViewById(com.nforetek.bt.phone.R.id.window_name);
        window_thz = mView.findViewById(com.nforetek.bt.phone.R.id.window_thz);
        window_type = mView.findViewById(com.nforetek.bt.phone.R.id.window_type);
        window_yuying = mView.findViewById(com.nforetek.bt.phone.R.id.window_yuying);
        window_tbd = mView.findViewById(com.nforetek.bt.phone.R.id.window_tbd);
        window_answer.setOnClickListener(this);
        window_hangup.setOnClickListener(this);
        window_decice.setOnClickListener(this);
        window_finish.setOnClickListener(this);
        window_keyboard_btn.setOnClickListener(this);
        window_yuying.setOnClickListener(this);
        window_answer_animation = (AnimationDrawable) window_answer.getBackground();
        List<NfHfpClientCall> hfpCallList = null;
        try {
            hfpCallList = mBPresenter.getHfpCallList();
            if(!hfpCallList.isEmpty()){
                NfHfpClientCall call = hfpCallList.get(0);
                String callNumber = call.getNumber();
                String callName1 = GetInfoFormContacts.getNameFromContacts(callNumber);
                if("".equals(callName1)){
                    callName1 = callNumber;
                }
                if(call.getState() == NfHfpClientCall.CALL_STATE_DIALING){
                    window_type.setText(context.getResources().getString(R.string.string18));
                }else if(call.getState() ==NfHfpClientCall.CALL_STATE_ACTIVE){
//                    window_type.setText("通话中");
                }else if(call.getState() ==NfHfpClientCall.CALL_STATE_INCOMING){
                    window_type.setText(context.getResources().getString(R.string.string17));
                }
                window_firName.setText(callName1.substring(0));
                window_name.setText(callName1);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.window_answer:
                try {
                    if(mBPresenter != null && mBPresenter.isHfpConnected()){
                        Log.d(TAG, "------------reqHfpAnswerCall---------------");
                        mBPresenter.reqHfpAnswerCall(NfDef.CALL_ACCEPT_NONE);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.window_hangup:
                try {
                    List<NfHfpClientCall> hfpCallList = mBPresenter.getHfpCallList();
                    if(mBPresenter != null && mBPresenter.isHfpConnected()  && hfpCallList != null && hfpCallList.size() >0){
                        if(hfpCallList.get(0).getState() == NfHfpClientCall.CALL_STATE_INCOMING){
                            Log.d(TAG, "------------reqHfpRejectIncomingCall---------------");
                            mBPresenter.reqHfpRejectIncomingCall();
                        }else {
                            Log.d(TAG, "------------reqHfpTerminateCurrentCall---------------");
                            mBPresenter.reqHfpTerminateCurrentCall();
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.window_finish:
                //影藏弹窗，显示Activity

                CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                management.showCallInterface(context,CallInterfaceManagement.SHOW_TYPE_Activity);

                break;
            case R.id.window_decice:
                if(mBPresenter != null){
                    try {
                        if (mBPresenter.getHfpAudioConnectionState() == NfDef.STATE_CONNECTED){
                            mBPresenter.reqHfpAudioTransferToPhone();
//                            window_decice.setImageDrawable(context.getDrawable(R.drawable.select_mini_private));
                        }else {
                            //NfDef.STATE_READY
                            mBPresenter.reqHfpAudioTransferToCarkit();
//                            window_decice.setImageDrawable(context.getDrawable(R.drawable.select_mini_hands_free));
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.window_keyboard_btn:
                //影藏弹窗，显示Activity,打开键盘
                MyApplication.isKeyboardShow = true;

                CallInterfaceManagement management1 = CallInterfaceManagement.getCallInterfaceManagementInstance();
                management1.showCallInterface(context,CallInterfaceManagement.SHOW_TYPE_Activity);

                break;
            case R.id.window_yuying:
                //静音
                if(mBPresenter != null){
                    try {
                        Log.d(TAG, " isHfpMicMute()="+mBPresenter.isHfpMicMute());
                        if (mBPresenter.isHfpMicMute()) {
                            mBPresenter.muteHfpMic(false);
                            window_yuying.setImageDrawable(context.getDrawable(R.drawable.select_mini_voice));
                            Log.d(TAG, "muteHfpMic(false)  isHfpMicMute()="+mBPresenter.isHfpMicMute());
                        }else {
                            //mic非静音
                            mBPresenter.muteHfpMic(true);//设置mic静音
                            window_yuying.setImageDrawable(context.getDrawable(R.drawable.select_mini_mute));
                            Log.d(TAG, "muteHfpMic(true)  isHfpMicMute()="+mBPresenter.isHfpMicMute());
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
    /**
     * 将本应用置顶到最前端
     * 当本应用位于后台时，则将它切换到最前端
     *
     * @param context
     */
    public static void setTopApp(Context context,boolean isIncomming) {
        if (!getTopAppPackageName(context)) {
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
        }
    }
    /**
     * 获取前台应用
     */
    public static boolean getTopAppPackageName(Context context) {
        try {
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
            if(!rti.isEmpty()){
                String packageName = rti.get(0).topActivity.getPackageName();
                Log.d(TAG, "getTopAppPackageName:packageName==== " + packageName);
                if(packageName.equals(context.getPackageName())){
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
    private void init() {
        wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.TRANSLUCENT;
        wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        wmParams.width = 1360;
        wmParams.height = 160;
        wmParams.x = x;
        wmParams.y = y;
//        if (childView != null) {
//            addView(childView);
//        }
        //   wm.addView(this, wmParams);
    }

    public void dismiss() {
        if (wm != null && mIsShow) {
            try {
                if (null != mView) {
                    wm.removeView(mView);
                    Log.d(TAG, "dismiss: " );
                    mIsShow = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private static String TAG = WindowDialog.class.getCanonicalName();
    public void show() {
        Log.d(TAG, "------------show---------------"+mIsShow);
        if (!mIsShow  ) {
            if (null != wm && null != mView) {
                try {
                    Log.d(TAG, "------------show---------------");
                    wm.addView(mView, wmParams);
                    mIsShow = true;
                    if(mBPresenter != null){
                        mHandler.sendEmptyMessage(0x03);
                        try {
//                            if (mBPresenter.getHfpAudioConnectionState() != NfDef.STATE_CONNECTED){
//                                window_decice.setImageDrawable(context.getDrawable(com.nforetek.bt.phone.R.drawable.select_hands_free));
//                            }else {
//                                window_decice.setImageDrawable(context.getDrawable(com.nforetek.bt.phone.R.drawable.select_private));
//                            }
                            if(mBPresenter.getHfpAudioConnectionState() == NfDef.STATE_READY){
                                window_decice.setImageDrawable(context.getDrawable(R.drawable.select_mini_private));
                            }else if(mBPresenter.getHfpAudioConnectionState() == NfDef.STATE_CONNECTED){
                                window_decice.setImageDrawable(context.getDrawable(R.drawable.select_mini_hands_free));
                            }
                            Log.d(TAG, "onStart: isHfpMicMute()="+mBPresenter.isHfpMicMute()+"----getHfpAudioConnectionState="+mBPresenter.getHfpAudioConnectionState());
                            if (mBPresenter.isHfpMicMute()) {
                                window_yuying.setImageDrawable(context.getDrawable(R.drawable.select_mini_mute));
                            }else {
                                //mic非静音
                                window_yuying.setImageDrawable(context.getDrawable(R.drawable.select_mini_voice));
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (WindowManager.BadTokenException e) {
                    tryShowAgain();
                } catch (Exception e) {

                }
            }else {
                Log.d(TAG, "------------wm---------mView==null------");
            }
        }
    }

    private void tryShowAgain() {
        if (wmParams.type == WindowManager.LayoutParams.TYPE_TOAST) {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            try {
                wm.addView(mView, wmParams);
                mIsShow = true;
            } catch (Exception e) {

            }
        } else if (wmParams.type == WindowManager.LayoutParams.TYPE_PHONE) {
            wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            try {
                wm.addView(mView, wmParams);
                mIsShow = true;
            } catch (Exception e) {

            }
        }
    }

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            switch (msg.what){
                case 0:
                    int arg1 = msg.arg1;
                    if(arg1 == NfDef.STATE_READY){
                        window_decice.setImageDrawable(context.getDrawable(R.drawable.select_mini_private));
                    }else if(arg1 == NfDef.STATE_CONNECTED){
                        window_decice.setImageDrawable(context.getDrawable(R.drawable.select_mini_hands_free));
                    }
                    break;
                case 0x01:

                    break;
                case 0x02:
                    String time = data.getString("time");
                    if("00:00".equals(time)){

                    }else {
                        window_type.setText(time);
                    }
                    break;
                case 0x03:
                    try {

                        List<NfHfpClientCall> hfpCallList = mBPresenter.getHfpCallList();
                        if(!hfpCallList.isEmpty()){
                            NfHfpClientCall call = hfpCallList.get(0);
                            int state = call.getState();
                            String callNumber = call.getNumber();
                            String callName1 = GetInfoFormContacts.getNameFromContacts(callNumber);
                            if("".equals(callName1)){
                                callName1 = callNumber;
                            }
                            window_firName.setText(callName1.substring(0));
                            window_name.setText(callName1);
                            if(state == NfHfpClientCall.CALL_STATE_ACTIVE){
                                //通话中
                                window_thz.setVisibility(View.VISIBLE);
                                window_tbd.setVisibility(View.GONE);
                                window_answer.setVisibility(View.GONE);
                                if(window_answer_animation != null && window_answer_animation.isRunning()){
                                    window_answer_animation.stop();
                                }
                            }else if(state == NfHfpClientCall.CALL_STATE_INCOMING) {
                                //来电
                                window_thz.setVisibility(View.GONE);
                                window_tbd.setVisibility(View.VISIBLE);
                                window_answer.setVisibility(View.VISIBLE);
                                if(window_answer_animation != null ){
                                    window_answer_animation.start();
                                }
                            }else {
                                //去电
                                window_thz.setVisibility(View.GONE);
                                window_tbd.setVisibility(View.VISIBLE);
                                window_answer.setVisibility(View.GONE);
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


}
