package com.nforetek.bt.phone.service_boardcast;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.share.interfaces.IShareDataListener;
import com.adayo.proxy.sourcemngproxy.Control.SrcMngAudioSwitchProxy;
import com.adayo.proxy.sourcemngproxy.Interface.IAdayoFocusChange;
import com.adayo.systemserviceproxy.SystemServiceManager;
import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.aidl.NfPbapContact;
import com.nforetek.bt.aidl.UiCallbackHfp;
import com.nforetek.bt.aidl.UiCallbackPbap;
import com.nforetek.bt.aidl.UiCommand;
import com.nforetek.bt.base.jar.NforeBtBaseJar;
import com.nforetek.bt.bean.Contacts;
import com.nforetek.bt.phone.BtPhoneMainActivity;
import com.nforetek.bt.phone.CallingActivity;
import com.nforetek.bt.phone.IncomingActivity;
import com.nforetek.bt.phone.MyApplication;
import com.nforetek.bt.phone.R;
import com.nforetek.bt.phone.tools.CallInterfaceManagement;
import com.nforetek.bt.phone.tools.GetInfoFormContacts;
import com.nforetek.bt.phone.tools.ShareInfoUtils;
import com.nforetek.bt.phone.tools.WindowDialog;
import com.nforetek.bt.res.NfDef;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CallService extends Service {
    protected static String TAG = CallService.class.getCanonicalName();
    private final int CALL = 1;
    private MediaPlayer mediaPlayer;
    private SrcMngAudioSwitchProxy mSrcMngAudioSwitchProxy;
    private View dialogView;
    public static boolean backCarState = false;//倒车状态 true-倒车  false-非倒车
    private boolean audioFocus = false; //音频焦点是否申请成功
    private int mProperty = NfDef.PBAP_PROPERTY_MASK_FN |
            NfDef.PBAP_PROPERTY_MASK_N |
            NfDef.PBAP_PROPERTY_MASK_TEL |
            NfDef.PBAP_PROPERTY_MASK_VERSION |
            NfDef.PBAP_PROPERTY_MASK_ADR |
            NfDef.PBAP_PROPERTY_MASK_EMAIL |
            NfDef.PBAP_PROPERTY_MASK_PHOTO |
            NfDef.PBAP_PROPERTY_MASK_ORG;
    private String mAdayoVersion;
    private FangKongReceiver fangKongReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: service 启动" );
        String str_sev_action = (String) this.getResources().getText(R.string.str_sev_action);
        String str_sev_package = (String) this.getResources().getText(R.string.str_sev_package);
        Intent mIntent = new Intent(str_sev_action);
        mIntent.setPackage(str_sev_package);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        registerBroadcast();
        //注册ShareInfo
        ShareInfoUtils.registerShareDataListener(this);
        if(SystemServiceManager. getInstance().conectsystemService()){
            Log.i(TAG,"---------conectsystemService------------");
            mAdayoVersion = SystemServiceManager. getInstance().getSystemConfigInfo((byte) 0x00);
            Log.i(TAG,"--mAdayoVersion--"+ mAdayoVersion);
        }
    }


    private void registerBroadcast(){
        IntentFilter intentFilter = new IntentFilter("adayo.keyEvent.onKeyUp");
        fangKongReceiver = new FangKongReceiver();
        registerReceiver(fangKongReceiver,intentFilter);
        Log.d(TAG, "registerBroadcast: action = adayo.keyEvent.onKeyUp");
    }

    private void unRegisterBroadcast(){
        if(fangKongReceiver != null){
            unregisterReceiver(fangKongReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: service销毁" );
        try {
            mCommand.unregisterHfpCallback(mCallbackHfp);
            mCommand.unregisterPbapCallback(mCallbackPbap);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        ShareInfoUtils.unregisterShareDataListener();
        unbindService(mConnection);
        unRegisterBroadcast();
    }

    private UiCommand mCommand;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "--------------------onServiceConnected-------------------");

            mCommand = UiCommand.Stub.asInterface(service);
            mhandler.sendEmptyMessage(000);


            Log.d(TAG, "--------------------end onServiceConnected-------------------");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "--------------------onServiceDisconnected-------------------");
        }
    };

    Handler mhandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case CALL:
                    break;
                case 000:
                    boolean isn = false;
                    if (mCommand != null) {
                        try {
                            mCommand.registerHfpCallback(mCallbackHfp);
                            mCommand.registerPbapCallback(mCallbackPbap);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        isn = true;
                    }
                    Log.d(TAG, "handleMessage: 绑定回调了 mCommand====" + isn);
                    break;
                case 0x02:
                    WindowDialog instance = WindowDialog.getInstance(getApplication());
                    instance.show();
                    break;
                case 0x03:
                    Log.d(TAG, "handleMessage: 通话结束或蓝牙断开");
                    MyApplication.isKeyboardShow = false;
                    releaseAudioFocus();//结束通话释放焦点
                    WindowDialog.initInstance();
                    if (MyApplication.callingActivity != null) {
                        Log.d(TAG, "handleMessage: callingActivity finish");
                        MyApplication.callingActivity.finish();
                    }
                    if (MyApplication.mBPresenter != null) {
                        if (MyApplication.mBPresenter.getRecordsFragment() != null) {
                            MyApplication.mBPresenter.getRecordsFragment().getList();
                        }
                    }
                    break;
                case 0x06:
                    Log.d(TAG, "handleMessage: 蓝牙断开");
                    MyApplication.isKeyboardShow = false;
                    releaseAudioFocus();//结束通话释放焦点
                    WindowDialog.initInstance();
                    if (MyApplication.callingActivity != null) {
                        MyApplication.callingActivity.finish();
                    }
                    if (MyApplication.mBPresenter != null) {
                        if (MyApplication.mBPresenter.getRecordsFragment() != null) {
                            MyApplication.mBPresenter.getRecordsFragment().getList();
                        }
                    }


                    break;
                case 0x04:
                    Log.d(TAG, "handleMessage: 蓝牙断开" );
                    MyApplication.isKeyboardShow = false;
                    releaseAudioFocus();//结束通话释放焦点
                    WindowDialog.initInstance();
                    if (MyApplication.callingActivity != null) {
                        MyApplication.callingActivity.finish();
                    }
                    if(IncomingActivity.bTphoneCallActivity != null){
                        IncomingActivity.bTphoneCallActivity.finish();
                    }
                    if (MyApplication.mBPresenter != null) {
                        if (MyApplication.mBPresenter.getContactFragment() != null) {
                            MyApplication.mBPresenter.getContactFragment().notifyDataSetChanged();
                        }
                        if (MyApplication.mBPresenter.getRecordsFragment() != null) {
                            MyApplication.mBPresenter.getRecordsFragment().notifyDataSetChanged();
                        }
                    }
                    break;
                case 0x05:
                    try {
                        if (mCommand != null) {
                            Bundle data = message.getData();
                            String address = data.getString("address");
                            String btDevConnAddr = mCommand.getBtDevConnAddr();
                            Log.d(TAG, "上次连接地址<------>当前连接地址 ----" + btDevConnAddr + "<-------->" + address);
//                            if (MyApplication.mBPresenter != null) {
//                                if (MyApplication.mBPresenter.getContactFragment() != null) {
//                                    MyApplication.mBPresenter.getContactFragment().getList();
//                                }
//                                if (MyApplication.mBPresenter.getRecordsFragment() != null) {
//                                    MyApplication.mBPresenter.getRecordsFragment().getList();
//                                }
//                            }
//                            if (address != null && !btDevConnAddr.equals(address)) {
//                                Log.d(TAG, "连接地址不同=====清空联系人及通话记录 =>" + btDevConnAddr + "<-------->" + address);
//                                mCommand.cleanTable(NforeBtBaseJar.CLEAN_TABLE_ALL);
//                                mCommand.setBtDevConnAddr(address);
//                                if (MyApplication.mBPresenter != null) {
//                                    if (MyApplication.mBPresenter.getContactFragment() != null) {
//                                        MyApplication.mBPresenter.getContactFragment().notifyDataSetChanged();
//                                    }
//                                    if (MyApplication.mBPresenter.getRecordsFragment() != null) {
//                                        MyApplication.mBPresenter.getRecordsFragment().notifyDataSetChanged();
//                                    }
//
//                                }
//                            } else {
//                                Log.d(TAG, "连接地址相同 =====刷新联系人和通话记录");
//                                if (MyApplication.mBPresenter != null) {
//                                    if (MyApplication.mBPresenter.getContactFragment() != null) {
//                                        MyApplication.mBPresenter.getContactFragment().getList();
//                                    }
//                                    if (MyApplication.mBPresenter.getRecordsFragment() != null) {
//                                        MyApplication.mBPresenter.getRecordsFragment().getList();
//                                    }
//                                }
//                            }

                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return false;
        }
    });



    public void onBackCarStateChange(){
        List<NfHfpClientCall> hfpCallList = null;
        if(mCommand == null){
            Log.d(TAG, "onBackCarStateChange: mCommand = null");
            return;
        }
        try {
            hfpCallList = mCommand.getHfpCallList();
            if (hfpCallList.size() == 1) {


            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

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
     * 折叠通知栏
     *
     * @param context
     */
    public static void collapsingNotification(Context context) {
        @SuppressLint("WrongConstant") Object service = context.getSystemService("statusbar");
        if (null == service)
            return;
        try {
            Class<?> clazz = Class.forName("android.app.StatusBarManager");
            int sdkVersion = Build.VERSION.SDK_INT;
            Method collapse = null;
            if (sdkVersion <= 16) {
                collapse = clazz.getMethod("collapse");
            } else {
                collapse = clazz.getMethod("collapsePanels");
            }
            collapse.setAccessible(true);
            collapse.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showView(){

    }


    public  void showCall(){
        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
        management.showCallInterface(CallService.this,CallInterfaceManagement.SHOW_TYPE_OUT);
    }


    private UiCallbackHfp mCallbackHfp = new UiCallbackHfp.Stub() {

        @Override
        public void onHfpServiceReady() throws RemoteException {

        }

        @Override
        public void onHfpStateChanged(String address, int prevState, int newState) throws RemoteException {
            if (newState == NfDef.STATE_READY) {//蓝牙已断开连接
                if (IncomingActivity.bTphoneCallActivity != null) {
                    IncomingActivity.bTphoneCallActivity.finish();
                }
//                stopBell();
                Log.d(TAG, "蓝牙已断开连接");
                mhandler.sendEmptyMessage(0x04);

            } else if (newState == NfDef.STATE_CONNECTED) {
                Message message = mhandler.obtainMessage();
                message.what = 0x05;
                Bundle bundle = new Bundle();
                bundle.putString("address", address);
                message.setData(bundle);
                mhandler.sendMessage(message);
            }

        }

        @Override
        public void onHfpAudioStateChanged(String address, int prevState, int newState) throws RemoteException {

        }

        @Override
        public void onHfpVoiceDial(String address, boolean isVoiceDialOn) throws RemoteException {

        }

        @Override
        public void onHfpErrorResponse(String address, int code) throws RemoteException {

        }

        @Override
        public void onHfpRemoteTelecomService(String address, boolean isTelecomServiceOn) throws RemoteException {

        }

        @Override
        public void onHfpRemoteRoamingStatus(String address, boolean isRoamingOn) throws RemoteException {

        }

        @Override
        public void onHfpRemoteBatteryIndicator(String address, int currentValue, int maxValue, int minValue) throws RemoteException {

        }

        @Override
        public void onHfpRemoteSignalStrength(String address, int currentStrength, int maxStrength, int minStrength) throws RemoteException {

        }

        @Override
        public void onHfpCallChanged(String address, NfHfpClientCall call) throws RemoteException {
            Log.d(TAG, "onHfpCallChanged getNumber!!" + call.getNumber());
            Log.d(TAG, "onHfpCallChanged getState!!" + call.getState());
            Log.d(TAG, "onHfpCallChanged: 车机型号=="+mAdayoVersion +" backCarState 倒车状态= "+backCarState);
            // 去电：7 来电： 挂断：
            String callNumber = call.getNumber();
            String callName = "";
            if(MyApplication.mBPresenter != null){
                callName = GetInfoFormContacts.getNameFromContacts(callNumber);
            }

            if (call.getState() == NfHfpClientCall.CALL_STATE_ACTIVE) {  //通话中
//                    stopBell();
                Log.d(TAG, "onHfpCallChanged:通话中: " + callName );
                applyAudioFocus();
                if(!backCarState){
                    //不在倒车状态 显示界面
                    CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                    management.showCallInterface(CallService.this,CallInterfaceManagement.SHOW_TYPE_OUT);
//                    if (getTopAppPackageName(CallService.this)) {
//                        if (MyApplication.callingActivity == null) {
//                            Log.d(TAG, "onClick: 进入通话界面2");
//                            Intent intent = new Intent();
//                            intent.putExtra("number", callNumber);
//                            intent.putExtra("name", callName);
//                            intent.setClass(getBaseContext(), CallingActivity.class);
//                            //获取当前view的bitmap
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            getApplication().startActivity(intent);
//                        }
//                    } else {
//                        mhandler.sendEmptyMessage(0x02);
//                    }
                }

                if (IncomingActivity.bTphoneCallActivity != null) {
                    IncomingActivity.bTphoneCallActivity.finish();
                }
            }else if (call.getState() == NfHfpClientCall.CALL_STATE_TERMINATED) {  //结束通话
//                    stopBell();         //停止来电铃声
                if (IncomingActivity.bTphoneCallActivity != null) {
                    IncomingActivity.bTphoneCallActivity.finish();
                }

                List<NfHfpClientCall> hfpCallList = mCommand.getHfpCallList();
                if (hfpCallList.size() == 0) {
                    mhandler.sendEmptyMessage(0x03);

                }

            }
            if (call.isOutgoing() == false) {
//来电
                if (call.getState() == NfHfpClientCall.CALL_STATE_INCOMING) {   //来电
                    Log.d(TAG, "------------CALL_STATE_INCOMING-----来电---------------");
                    applyAudioFocus();     //申请焦点
                    collapsingNotification(CallService.this);
                    if(!backCarState){
                        //不在倒车状态 显示界面
//                        if (getTopAppPackageName(CallService.this)) {
//                            Log.d(TAG, "------------显示Activity---------------");
//                            if (IncomingActivity.bTphoneCallActivity == null) {
//                                Intent intent = new Intent(getBaseContext(), IncomingActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                getApplication().startActivity(intent);
//                            }
//                        } else {
//                            Log.d(TAG, "------------显示Dialog---------------");
//                            mhandler.sendEmptyMessage(0x02);
//                        }


                        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                        management.showCallInterface(CallService.this,CallInterfaceManagement.SHOW_TYPE_IN);
                    }


                }else if (call.getState() == NfHfpClientCall.CALL_STATE_DIALING || call.getState() == NfHfpClientCall.CALL_STATE_ALERTING) {     //去电
                    Log.d(TAG, "onHfpCallChanged:去电电话1: " + callName);
                    if(!backCarState) {
                        //不在倒车状态 显示界面
//                        if (getTopAppPackageName(CallService.this)) {
//                            if (MyApplication.callingActivity == null) {
//                                Intent intent = new Intent(getBaseContext(), CallingActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.putExtra("number", callNumber);
//                                intent.putExtra("name", callName);
//                                getApplication().startActivity(intent);
//                            }
//                        } else {
//                            mhandler.sendEmptyMessage(0x02);
//                        }

                        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                        management.showCallInterface(CallService.this,CallInterfaceManagement.SHOW_TYPE_OUT);
                    }



                }
            } else {
                //去电 ||call.getState() == NfHfpClientCall.CALL_STATE_ALERTING
                if (call.getState() == NfHfpClientCall.CALL_STATE_DIALING ||call.getState() == NfHfpClientCall.CALL_STATE_ALERTING) {
                    Log.d(TAG, "onHfpCallChanged:去电电话2: " + callName);
                    applyAudioFocus();
                    if(!backCarState) {
                        //不在倒车状态 显示界面
//                        if (getTopAppPackageName(CallService.this)) {
//                            Log.d(TAG, "-------去电-----显示Activity---------------");
//                            if (MyApplication.callingActivity == null) {
//                                Log.d(TAG, "------------显示Activity-----去电----------");
//                                Intent intent = new Intent();
//                                intent.setClass(getBaseContext(), CallingActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                getApplication().startActivity(intent);
//                            }
//                        } else {
//                            Log.d(TAG, "------------显示Dialog---去电------------");
//                            mhandler.sendEmptyMessage(0x02);
//                        }

                        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                        management.showCallInterface(CallService.this,CallInterfaceManagement.SHOW_TYPE_OUT);
                    }



                }
            }
        }


        @Override
        public void retPbapDatabaseQueryNameByNumber(String address, String target, String name, boolean isSuccess) throws RemoteException {

        }

        @Override
        public void onHfpCallingTimeChanged(String time) throws RemoteException {

        }

        @Override
        public void onHfpMissedCall(String s, int i) throws RemoteException {

        }

        @Override
        public void onHfpManufactureIdentificationUpdated(String s, String s1) throws RemoteException {

        }

        @Override
        public void onHfpModelIdentificationUpdated(String s, String s1) throws RemoteException {

        }

        @Override
        public void onHfpClockOfDeviceUpdated(String s, String s1) throws RemoteException {

        }
    };

    private  UiCallbackPbap mCallbackPbap = new UiCallbackPbap.Stub(){

        @Override
        public void onPbapServiceReady() throws RemoteException {

        }

        @Override
        public void onPbapStateChanged(String s, int i, int i1, int i2, int i3) throws RemoteException {

        }

        @Override
        public void retPbapDownloadedContact(NfPbapContact nfPbapContact) throws RemoteException {

        }

        @Override
        public void retPbapDownloadedCallLog(String s, String s1, String s2, String s3, String s4, int i, String s5) throws RemoteException {

        }

        @Override
        public void onPbapDownloadNotify(String s, int i, int i1, int i2) throws RemoteException {

        }

        @Override
        public void retPbapDatabaseQueryNameByNumber(String s, String s1, String s2, boolean b) throws RemoteException {

        }

        @Override
        public void retPbapDatabaseQueryNameByPartialNumber(String s, String s1, String[] strings, String[] strings1, boolean b) throws RemoteException {

        }

        @Override
        public void retPbapDatabaseAvailable(String s) throws RemoteException {

        }

        @Override
        public void retPbapDeleteDatabaseByAddressCompleted(String s, boolean b) throws RemoteException {

        }

        @Override
        public void retPbapCleanDatabaseCompleted(boolean b) throws RemoteException {

        }
    };
    /** 获取日期时间 **/
    private static String getDatetime(){
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");//设置日期格式
        SimpleDateFormat time = new SimpleDateFormat("HHmmss");//设置时间格式
        return date.format(new Date())+"T"+time.format(new Date());
    }

    private void playBell() {
        Log.i(TAG, "------------------------------------------------------进入播放铃声判断--------------------------------------------------------");

        try {
            if (mediaPlayer == null) {
                if (mCommand.isHfpInBandRingtoneSupport()) {

                } else {
                    //创建播放实例--默认歌曲
                    Log.d(TAG, "自定义播放来电铃声文件'" + "'不存在，播放默认来电铃声！");
                    mediaPlayer = MediaPlayer.create(CallService.this, com.nforetek.bt.phone.R.raw.friendships);
                    //设置是否循环播放
                    mediaPlayer.setLooping(true);
                    //设置播放起始点
                    mediaPlayer.seekTo(0);
                    //开始播放
                    mediaPlayer.start();
                }


            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    /**
     * 停止铃声
     */
    private void stopBell() {
        Log.i(TAG, "停止播放来电铃声");
        if (mediaPlayer != null) {
            //停止播放
            mediaPlayer.stop();
            //释放资源
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    //释放焦点
    private void releaseAudioFocus() {
        new Thread() {
            @Override
            public void run() {
                mSrcMngAudioSwitchProxy = SrcMngAudioSwitchProxy.getInstance();
                if (mSrcMngAudioSwitchProxy != null) {
                    if (mSrcMngAudioSwitchProxy.abandonAdayoAudioFocus()) {
                        audioFocus = false;
                        Log.i(TAG, "------------------------------------------释放焦点成功----------------------------------------------");
                    } else {
                        Log.i(TAG, "------------------------------------------释放焦点失败----------------------------------------------");
                    }
                } else {
                    Log.d(TAG, "mSrcMngAudioSwitchProxy is null");
                }
            }
        }.start();
    }

    //申请焦点
    private void applyAudioFocus() {
        if(audioFocus){
            Log.d(TAG, "applyAudioFocus: 音频焦点已经获取,不需要再次获取");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                //SourceConstants.SOURCE_BT_PHONE
                mSrcMngAudioSwitchProxy = SrcMngAudioSwitchProxy.getInstance();
                if (mSrcMngAudioSwitchProxy != null) {
                    mSrcMngAudioSwitchProxy.setAudioSwitchInfo(AdayoSource.ADAYO_SOURCE_BT_PHONE, new IAdayoFocusChange() {

                        @Override
                        public void onGainAfterSwitchChannel() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onGainBeforeSwitchChannel() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onLossAfterSwitchChannel() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onLossBeforeSwitchChannel() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onLossTransientAfterSwitchChannel() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onLossTransientBeforeSwitchChannel() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onLossTransientCanDuckAfterSwitchChannel() {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onLossTransientCanDuckBeforeSwitchChannel() {
                            // TODO Auto-generated method stub

                        }
                    }, CallService.this);

                    //AudioManager.STREAM_VOICE_CALL   AUDIOFOCUS_GAIN_TRANSIENT
                    if (mSrcMngAudioSwitchProxy.requestAdayoAudioFocus(6, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)) {//经江文尧工确认，换成混音AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                        audioFocus = true;
                        Log.i(TAG, "------------------------------------------申请焦点成功----------------------------------------------");
                    } else {
                        audioFocus = false;
                        Log.i(TAG, "------------------------------------------申请焦点失败----------------------------------------------");
                    }
                } else {
                    Log.d(TAG, "mSrcMngAudioSwitchProxy is null");
                }
            }

        }.start();

    }

}

