package com.nforetek.bt.phone.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
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
import com.nforetek.bt.phone.CallingActivity;
import com.nforetek.bt.phone.IncomingActivity;
import com.nforetek.bt.phone.MyApplication;
import com.nforetek.bt.phone.R;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.res.NfDef;

import java.util.List;

public class WindowDialog1 implements View.OnClickListener{
//,BtPresenter.UiBluetoothPhoneChangeListerer,BtPresenter.UiBluetoothSettingChangeListerer
    private WindowManager.LayoutParams wmParams;
    private WindowManager wm;
    private Context context;
    private View mView;
    public boolean mIsShow = false;
    private int x;
    private int y;
    private static WindowDialog1 instance = null;

    private static BtPresenter mBPresenter;
    private ImageView window_finish;
    private TextView window_firName;
    private TextView window_name;
    private TextView window_type;

    private WindowDialog1(){

    }
    private WindowDialog1(Context context){
        this.context = context;
        this.mBPresenter = MyApplication.mBPresenter;
        this.x = 0;
        this.y = 0;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_window1, null);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        this.mView = view;
        this.init();
        this.initView();

    }

    public static WindowDialog1 getInstance(Context context){
        if(null == instance){
            Log.d(TAG, "getInstance: 实例化" );
            instance = new WindowDialog1(context);
        }
        return instance;
    }

    public static WindowDialog1 getInstance(){
        Log.d(TAG, "getInstance: " );
        return instance;
    }
    public static void initInstance(){
        if(instance != null){
            Log.d(TAG, "initInstance: ===null" );
            instance.dismiss();
            instance = null;
        }
    }

    private void initView(){
        if(mView == null)
            return;
        window_finish = mView.findViewById(R.id.window1_finish);
        window_firName = mView.findViewById(R.id.window1_firName);
        window_name = mView.findViewById(R.id.window1_name);
        window_type = mView.findViewById(R.id.window1_type);
        window_finish.setOnClickListener(this);
        if(mBPresenter != null){
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.window_finish:
                //影藏弹窗
                initInstance();
                break;
        }
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



    private static String TAG = WindowDialog1.class.getCanonicalName();
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
                    }
                } catch (WindowManager.BadTokenException e) {
                    tryShowAgain();
                } catch (Exception e) {

                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: 3s后自动关闭弹窗");
                        initInstance();
                    }
                },3000);
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

                            }else if(state == NfHfpClientCall.CALL_STATE_INCOMING) {

                            }else {

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
