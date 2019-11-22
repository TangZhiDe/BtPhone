package com.nforetek.bt.phone.tools;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.adayo.adayosource.AdayoSource;
import com.adayo.app.utils.LogUtils;
import com.adayo.proxy.settings.SettingExternalManager;
import com.adayo.proxy.share.ShareDataManager;
import com.adayo.proxy.share.interfaces.IShareDataListener;
import com.nforetek.bt.phone.MyApplication;
import com.nforetek.bt.phone.service_boardcast.CallService;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * Created by admin on 2018/9/30.
 */

public class ShareInfoUtils {
    private static final int backCarStateID = 16;//倒车
    private static final int srcID = 14;//源管理
    private static final int iCallID = 52;//源管理call_state
    private static final int voiceID = 7;//源管理call_state  mute_switch
    private static ShareDataManager mShareDataManager = ShareDataManager.getShareDataManager();
    /**
     * 从shareInfo中读取设置项
     */
    public static void loadSkinInfo() {
        String strCommonSettingJSON = mShareDataManager.getShareData(backCarStateID);
        try {
            if (strCommonSettingJSON == null) {
                Log.d("loadSkinInfo", "strCommonSettingJSON = null");
                return;
            } else {
                JSONObject jsonObject = new JSONObject(strCommonSettingJSON);
                boolean backCarState = jsonObject.getBoolean("backCarState");
                Log.d("loadSkinInfo", "backCarState = "+backCarState);
                MyApplication.backCarState = backCarState;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public  static void parseSkinInfo(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if(jsonObject.has("backCarState")){
                boolean backCarState = jsonObject.getBoolean("backCarState");
                Log.d("parseSkinInfo", "backCarState = "+backCarState+"  context="+context);
                MyApplication.backCarState = backCarState;
                if(context != null){
                    if(backCarState){
                        //在倒车 隐藏弹窗
                        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                        management.showCallInterface(CallInterfaceManagement.SHOW_TYPE_HIDE);

                    }else {
                        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                        management.showCallInterface(CallInterfaceManagement.SHOW_TYPE_OUT);
                    }
                }


            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    public static String currentUID = "";
    public  static void parseSkinInfo1(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if(jsonObject.has("UID")){
                String UID = jsonObject.getString("UID");
                Log.d("parseSkinInfo", "UID = "+UID+"  context="+context);
                if(!TextUtils.isEmpty(UID) && !UID.equals(currentUID) && context != null && !MyApplication.iCall_state){
                    currentUID = UID;
                    CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                    if(UID.equals("ADAYO_SOURCE_BT_PHONE")){
                        management.showCallInterface(CallInterfaceManagement.SHOW_TYPE_TURN_CALL);
                    }else if(UID.equals(AdayoSource.ADAYO_SOURCE_AVM) || UID.equals(AdayoSource.ADAYO_SOURCE_RVC)){
                        management.showCallInterface(CallInterfaceManagement.SHOW_TYPE_HIDE);
                    } else {
                        management.showCallInterface(CallInterfaceManagement.SHOW_TYPE_YUAN);
                    }
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    public  static void parseSkinInfo2(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if(jsonObject.has("call_state")){
                boolean call_state = jsonObject.getBoolean("call_state");
                Log.d("parseSkinInfo", "call_state = "+call_state);
                MyApplication.iCall_state = call_state;
                if(!call_state){
                    Log.i("parseSkinInfo", "parseSkinInfo2: ibCall结束显示通话界面");
                    CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                    management.showCallInterface(CallInterfaceManagement.SHOW_TYPE_OUT);
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    public  static void parseSkinInfo3(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if(jsonObject.has("mute_switch")){
                boolean mute_switch = jsonObject.getBoolean("mute_switch");
                Log.d("parseSkinInfo", "mute_switch = "+mute_switch);
                if(mute_switch){

                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }


    public static IShareDataListener shareDataListener = new IShareDataListener(){
        @Override
        public void notifyShareData(int dataType,String s) {
            Log.d("update share info","notifyShareData:"+s+",dataType="+dataType);
            if (dataType == backCarStateID){
                parseSkinInfo(s);
            }
            if (dataType == srcID){
                parseSkinInfo1(s);
            }
            if (dataType == iCallID){
                parseSkinInfo2(s);
            }
            if (dataType == voiceID){
                parseSkinInfo3(s);
            }
        }
    };
    private static Context context;

    /**
     * 注册客户端回调函数
     */
    public static void registerShareDataListener(Context context1){
        //mContext = context;
        mShareDataManager = ShareDataManager.getShareDataManager();
        context = context1;
        if(mShareDataManager != null){
            loadSkinInfo();
            mShareDataManager.registerShareDataListener(backCarStateID,shareDataListener);
            mShareDataManager.registerShareDataListener(srcID,shareDataListener);
            mShareDataManager.registerShareDataListener(iCallID,shareDataListener);
        } else {
            Log.d("register","ShareDataManager is null");
        }
    }
    /**
     * 注销客户端回调函数
     */
    public static void unregisterShareDataListener(){
        if(mShareDataManager != null){
            mShareDataManager.unregisterShareDataListener(backCarStateID,shareDataListener);
            mShareDataManager.unregisterShareDataListener(srcID,shareDataListener);
            mShareDataManager.unregisterShareDataListener(iCallID,shareDataListener);
            mShareDataManager = null;
        }else{
//            Log.d("unregister","ShareDataManager is null");
        }
    }


}
