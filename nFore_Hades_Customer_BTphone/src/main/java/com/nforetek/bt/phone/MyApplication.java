package com.nforetek.bt.phone;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import com.nforetek.bt.bean.CallLogs;
import com.nforetek.bt.bean.Contacts;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.phone.tools.BtUtils;
import com.nforetek.bt.phone.tools.CallInterfaceManagement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tzd
 *
 */

public class MyApplication extends Application {
    public static List<Contacts> contactList = new ArrayList<>();
    public static List<CallLogs> recordsList = new ArrayList<>();
    public static String connectAddress ="";
    public static BtPresenter mBPresenter;
    private static final String TAG = MyApplication.class.getCanonicalName();
    public static boolean isKeyboardShow = false;
    public static boolean iCall_state = false;//   i/bCall是否在通话中
    public static boolean answerSouce = true;//   是否响应切源
    public static boolean backCarState = false;//倒车状态 true-倒车  false-非倒车
    public static String Verson = "_V3.3";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mBPresenter = BtPresenter.getInstance(this);
//        CallInterfaceManagement instance = CallInterfaceManagement.getCallInterfaceManagementInstance();
//        instance.setParms(this,mBPresenter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: uiMode===" +newConfig.uiMode+"---locale="+newConfig.locale.toString());
        if(newConfig.uiMode == 19 || newConfig.uiMode == 35){
//            finishApp();
        }

        if(newConfig.locale.toString().equals("zh_CN") || newConfig.locale.toString().equals("en")){
//            finishApp();
        }

    }

    public static void finishApp(){
        Log.d(TAG, "finishApp: " );
        BtUtils.finish(CallingActivity.callingActivity);
        BtUtils.finish(BtPhoneMainActivity.btPhoneMainActivity);
        BtUtils.finish(IncomingActivity.bTphoneCallActivity );
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if(mBPresenter != null){
            mBPresenter.unregisterServiceListener();
            mBPresenter = null;
        }
    }



}
