package com.nforetek.bt.phone.service_boardcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.proxy.sourcemngproxy.Control.SrcMngSwitchProxy;
import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.phone.MyApplication;
import com.nforetek.bt.res.NfDef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FangKongReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String TAG = "FangKongReceiver";
        if (intent.getAction().equals("adayo.keyEvent.onKeyUp")) {
            String hardKey = intent.getStringExtra("hardKey");
            Log.d(TAG, "onReceive: hardKey=" + hardKey);
            if (MyApplication.mBPresenter != null) {

                switch (hardKey) {
                    case "K_PHONE_OFF":
                        try {
                            Log.d(TAG, "onReceive: 挂断");
                            MyApplication.mBPresenter.reqHfpTerminateCurrentCall();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "K_PHONE_ON":
                        try {
                            List<NfHfpClientCall> hfpCallList = MyApplication.mBPresenter.getHfpCallList();
                            if (hfpCallList.isEmpty()) {
                                Log.d(TAG, "onReceive:没有电话 跳转到蓝牙界面");
                                SrcMngSwitchProxy srcMngSwitchProxy = SrcMngSwitchProxy.getInstance();
                                Map<String, String> map = new HashMap<>();
                                SourceInfo info = new SourceInfo(AdayoSource.ADAYO_SOURCE_BT_PHONE, map,
                                        AppConfigType.SourceSwitch.APP_ON.getValue(), AppConfigType.SourceType.UI.getValue());
                                srcMngSwitchProxy.onRequest(info);
                            } else {
                                Log.d(TAG, "onReceive: 接听");
                                MyApplication.mBPresenter.reqHfpAnswerCall(NfDef.CALL_ACCEPT_NONE);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "K_PHONE":
                        String input_number = intent.getStringExtra("number");
                        Log.d(TAG, "onReceive: input_number===" + input_number);
                        try {
                            if (input_number != null && MyApplication.mBPresenter.isHfpConnected()) {
                                MyApplication.mBPresenter.reqHfpDialCall(input_number);
                                Log.d(TAG, "onReceive: 拨打" + input_number);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                }

            }

        }
    }
}