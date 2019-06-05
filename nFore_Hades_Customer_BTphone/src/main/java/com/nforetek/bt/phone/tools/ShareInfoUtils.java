package com.nforetek.bt.phone.tools;

import android.content.Context;
import android.util.Log;

import com.adayo.app.utils.LogUtils;
import com.adayo.proxy.share.ShareDataManager;
import com.adayo.proxy.share.interfaces.IShareDataListener;
import com.nforetek.bt.phone.service_boardcast.CallService;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by admin on 2018/9/30.
 */

public class ShareInfoUtils {
    private static final int backCarStateID = 16;
    private static ShareDataManager mShareDataManager = ShareDataManager.getShareDataManager();
    /**
     * 从shareInfo中读取设置项
     */
    public static void loadSkinInfo() {
        String strCommonSettingJSON = mShareDataManager.getShareData(backCarStateID);
        try {
            if (strCommonSettingJSON == null) {
                Log.e("loadSkinInfo", "strCommonSettingJSON = null");
                return;
            } else {
                JSONObject jsonObject = new JSONObject(strCommonSettingJSON);
                boolean backCarState = jsonObject.getBoolean("backCarState");
                Log.e("loadSkinInfo", "backCarState = "+backCarState);
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
                Log.e("parseSkinInfo", "backCarState = "+backCarState+"  context="+context);
                CallService.backCarState = backCarState;
                if(!backCarState && context != null){
                    CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                    management.showCallInterface(context,CallInterfaceManagement.SHOW_TYPE_OUT);
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }



    public static IShareDataListener shareDataListener = new IShareDataListener(){
        @Override
        public void notifyShareData(int dataType,String s) {
            Log.e("update share info","notifyShareData:"+s+",dataType="+dataType);
            if (dataType == backCarStateID){
                parseSkinInfo(s);
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
        }else{
            Log.e("register","ShareDataManager is null");
        }
    }
    /**
     * 注销客户端回调函数
     */
    public static void unregisterShareDataListener(){
        if(mShareDataManager != null){
            mShareDataManager.unregisterShareDataListener(backCarStateID,shareDataListener);
            mShareDataManager = null;
        }else{
            Log.e("unregister","ShareDataManager is null");
        }
    }


}
