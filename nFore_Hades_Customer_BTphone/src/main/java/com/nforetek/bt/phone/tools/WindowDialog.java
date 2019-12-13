package com.nforetek.bt.phone.tools;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.phone.MyApplication;
import com.nforetek.bt.phone.R;
import com.nforetek.bt.phone.presenter.BtPresenter;

import java.util.List;

public class WindowDialog implements View.OnClickListener{
    private WindowManager.LayoutParams wmParams;
    private WindowManager wm;
    private Context context;
    private View mView;
    public boolean mIsShow = false;
    private int x;
    private int y;
    private static WindowDialog instance = null;

    private static BtPresenter mBPresenter;

    private TextView window_type;
    private LinearLayout window_dialog;
    private TextView window_time;

    private WindowDialog(){

    }
    private WindowDialog(Context context){
        this.context = context;
        this.mBPresenter = MyApplication.mBPresenter;
        this.x = 0;
        this.y = 900;
        if(mBPresenter != null){
            mBPresenter.setWindowDialog(this);
        }
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_window, null);
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
    public static void initInstance(){
        if(instance != null){
            Log.d(TAG, "initInstance: ===null" );
            instance.dismiss();
            instance = null;
            if(mBPresenter != null){
                mBPresenter.setWindowDialog(null);
            }

        }
    }

    private void initView(){
        if(mView == null)
            return;
        window_dialog = mView.findViewById(R.id.window_dialog);
        window_type = mView.findViewById(R.id.window_type);
        window_time = mView.findViewById(R.id.window_time);
        window_dialog.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.window_dialog:
                Log.d(TAG, "onClick: MyApplication.iCall_state ="+MyApplication.iCall_state);
                if(MyApplication.iCall_state){
                    initInstance();
                }else {
                    dismiss();
                    CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                    management.showCallInterface(CallInterfaceManagement.SHOW_TYPE_Activity);
                }
                break;

        }
    }


    private void init() {
        wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;//WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;TYPE_APPLICATION_OVERLAY ;
        wmParams.windowAnimations = R.style.windowAnimation;
        wmParams.format = PixelFormat.TRANSLUCENT;
        wmParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;//| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        wmParams.width = 268;
        wmParams.height = 60;
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



    private static String TAG = WindowDialog.class.getCanonicalName()+MyApplication.Verson;
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
                        window_time.setText(time);
                    }
                    break;
                case 0x03:
                    if(mBPresenter != null){
                        List<NfHfpClientCall> hfpCallList = null;
                        try {
                            hfpCallList = mBPresenter.getHfpCallList();
                            if(!hfpCallList.isEmpty()){
                                NfHfpClientCall call = hfpCallList.get(0);
                                if(call.getState() == NfHfpClientCall.CALL_STATE_DIALING){
                                    window_type.setText(context.getResources().getString(R.string.string18));
                                    window_time.setVisibility(View.GONE);
                                }else if(call.getState() ==NfHfpClientCall.CALL_STATE_ACTIVE){
                                    window_type.setText(context.getResources().getString(R.string.string20));
                                    window_time.setVisibility(View.VISIBLE);
                                }else if(call.getState() ==NfHfpClientCall.CALL_STATE_INCOMING){
                                    window_type.setText(context.getResources().getString(R.string.string17));
                                    window_time.setVisibility(View.GONE);
                                }
                            }

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
            }
            return false;
        }
    });


}
