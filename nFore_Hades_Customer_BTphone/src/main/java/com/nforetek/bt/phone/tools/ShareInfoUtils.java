package com.nforetek.bt.phone.tools;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.adayo.app.utils.LogUtils;
import com.adayo.proxy.share.ShareDataManager;
import com.adayo.proxy.share.interfaces.IShareDataListener;
import com.nforetek.bt.phone.service_boardcast.CallService;

import org.json.JSONException;
import org.json.JSONObject;

import static com.nforetek.bt.phone.service_boardcast.CallService.backCarState;


/**
 * Created by admin on 2018/9/30.
 */

public class ShareInfoUtils {
    private static final int backCarStateID = 16;//倒车
    private static final int srcID = 14;//源管理
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
                CallService.backCarState = backCarState;
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
                CallService.backCarState = backCarState;
                if(backCarState){
                    //在倒车 隐藏弹窗
                    WindowDialog instance = WindowDialog.getInstance();
                    if(instance != null && instance.mIsShow){
                        instance.dismiss();
                    }
                }
                if(!backCarState && context != null){
                    CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                    management.showCallInterface(context,CallInterfaceManagement.SHOW_TYPE_OUT);
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
                if(!TextUtils.isEmpty(UID) && !UID.equals(currentUID) && context != null){
                    currentUID = UID;
                    CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                    if(UID.equals("ADAYO_SOURCE_BT_PHONE")){
                        Log.d("parseSkinInfo", "parseSkinInfo1: ");
                        management.showCallInterface(context,CallInterfaceManagement.SHOW_TYPE_Activity);
                    } else {
                        management.showCallInterface(context,CallInterfaceManagement.SHOW_TYPE_YUAN);
                    }
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
            mShareDataManager = null;
        }else{
            Log.d("unregister","ShareDataManager is null");
        }
    }


}
