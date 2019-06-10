package com.nforetek.bt.phone;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.nforetek.bt.bean.CallLogs;
import com.nforetek.bt.bean.Contacts;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.phone.service_boardcast.CallService;
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
    public static CallingActivity callingActivity = null;
    public static String connectAddress ="";
    public static BtPresenter mBPresenter;
    private boolean isRegisterServiceListener;
    private static final String TAG = MyApplication.class.getCanonicalName();
    public static boolean isKeyboardShow = false;
    public static boolean isPbapDownload = false;//是否在下载联系人
    @Override
    public void onCreate() {
        super.onCreate();
        mBPresenter = new BtPresenter();
        isRegisterServiceListener = mBPresenter.registerServiceListener(this);
        CallInterfaceManagement instance = CallInterfaceManagement.getCallInterfaceManagementInstance();
        instance.setParms(this,mBPresenter);
//        Intent service = new Intent(this,CallService.class);
//        service.setPackage("com.nforetek.bt.phone");
//        startService(service);
        Log.d(TAG, "onCreate: 实例化BtPresenter，绑定服务isRegisterServiceListener="+isRegisterServiceListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: uiMode===" +newConfig.uiMode+"---locale="+newConfig.locale.toString());
        if(newConfig.uiMode == 19 || newConfig.uiMode == 35){
            finishApp();
        }

        if(newConfig.locale.toString().equals("zh_CN") || newConfig.locale.toString().equals("en")){
            finishApp();
        }

    }

    public void finishApp(){
        Log.d(TAG, "finishApp: " );
        if(callingActivity != null){
            Log.d(TAG, "finishApp: callingActivity" );
            callingActivity.finish();
        }
        if(IncomingActivity.bTphoneCallActivity != null){
            Log.d(TAG, "finishApp: IncomingActivity" );
            IncomingActivity.bTphoneCallActivity.finish();
        }
        if(BtPhoneMainActivity.btPhoneMainActivity != null){
            Log.d(TAG, "finishApp: BtPhoneMainActivity" );
            BtPhoneMainActivity.btPhoneMainActivity.finish();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if(isRegisterServiceListener){
            mBPresenter.unregisterServiceListener();
            isRegisterServiceListener = false;
        }
        if(mBPresenter!=null){
            mBPresenter = null;
        }
    }


}
