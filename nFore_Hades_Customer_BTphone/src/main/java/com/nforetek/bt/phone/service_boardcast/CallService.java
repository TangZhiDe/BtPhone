package com.nforetek.bt.phone.service_boardcast;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
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
import com.adayo.proxy.settings.SettingExternalManager;
import com.adayo.proxy.sourcemngproxy.Control.SrcMngAudioSwitchProxy;
import com.adayo.proxy.sourcemngproxy.Control.SrcMngSwitchProxy;
import com.adayo.proxy.sourcemngproxy.Interface.IAdayoFocusChange;
import com.adayo.systemserviceproxy.SystemServiceManager;
import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.aidl.NfPbapContact;
import com.nforetek.bt.aidl.UiCallbackBluetooth;
import com.nforetek.bt.aidl.UiCallbackHfp;
import com.nforetek.bt.aidl.UiCallbackPbap;
import com.nforetek.bt.aidl.UiCommand;
import com.nforetek.bt.phone.CallingActivity;
import com.nforetek.bt.phone.IncomingActivity;
import com.nforetek.bt.phone.MyApplication;
import com.nforetek.bt.phone.R;
import com.nforetek.bt.phone.tools.BtUtils;
import com.nforetek.bt.phone.tools.CallInterfaceManagement;
import com.nforetek.bt.phone.tools.GetInfoFormContacts;
import com.nforetek.bt.phone.tools.ShareInfoUtils;
import com.nforetek.bt.phone.tools.WindowDialog;
import com.nforetek.bt.res.NfDef;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CallService extends Service {
    protected static String TAG = CallService.class.getCanonicalName()+MyApplication.Verson;
    private final int CALL = 1;
    private MediaPlayer mediaPlayer;
    private SrcMngAudioSwitchProxy mSrcMngAudioSwitchProxy;
    private View dialogView;
    public static boolean backCarState = false;//倒车状态 true-倒车  false-非倒车
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
        Log.d(TAG, "onCreate: service 启动");
        String str_sev_action = (String) this.getResources().getText(R.string.str_sev_action);
        String str_sev_package = (String) this.getResources().getText(R.string.str_sev_package);
        Intent mIntent = new Intent(str_sev_action);
        mIntent.setPackage(str_sev_package);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        registerBroadcast();
        //注册ShareInfo
        ShareInfoUtils.registerShareDataListener(this);
        if (SystemServiceManager.getInstance().conectsystemService()) {
            Log.i(TAG, "---------conectsystemService------------");
            mAdayoVersion = SystemServiceManager.getInstance().getSystemConfigInfo((byte) 0x00);
            Log.i(TAG, "--mAdayoVersion--" + mAdayoVersion);
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }


    private void setBluetoothName() {
        String name = "DFSK";
        String btLocalAddress = getBluetoothAddress();
        Log.d(TAG, "handleMessage: btLocalAddress=" + btLocalAddress);
        if (btLocalAddress != null) {
            String s = btLocalAddress.replaceAll(":", "");
            if (s.length() > 6) {
                name = s.substring(6);
            }
        }
        try {
            mCommand.setBtLocalName(name);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String getBluetoothAddress() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.d(TAG, "BluetoothAdapter: 蓝牙适配器为空");
            return null;
        }
        Class<? extends BluetoothAdapter> btAdapterClass = adapter.getClass();
        try {
            Field mServiceField = adapter.getClass().getDeclaredField("mService");
            mServiceField.setAccessible(true);
            Object btManagerService = mServiceField.get(adapter);
            if (btManagerService != null) {
                return (String) btManagerService.getClass().getMethod("getAddress").invoke(btManagerService);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static final String mConfigChangeAction = "com.adayo.setting.configChange";
    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter("adayo.keyEvent.onKeyUp");
        intentFilter.addAction(mConfigChangeAction);
        fangKongReceiver = new FangKongReceiver();
        registerReceiver(fangKongReceiver, intentFilter);
        Log.d(TAG, "registerBroadcast: action = adayo.keyEvent.onKeyUp");
    }

    private void unRegisterBroadcast() {
        if (fangKongReceiver != null) {
            unregisterReceiver(fangKongReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: service销毁");
        try {
            mCommand.unregisterHfpCallback(mCallbackHfp);
            mCommand.unregisterPbapCallback(mCallbackPbap);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setAction("com.nforetek.bt.phone04.startService");
        sendBroadcast(intent);
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
                            mCommand.registerBtCallback(mCallbackBluetooth);
                            //设置蓝牙名称为地址后6位。
                            setBluetoothName();
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
                    Log.d(TAG, "handleMessage: 通话结束");
                    MyApplication.isKeyboardShow = false;
                    releaseAudioFocus();//结束通话释放焦点
                    WindowDialog.initInstance();
                    if (MyApplication.mBPresenter != null) {
                        BtUtils.finish(MyApplication.mBPresenter.getCallingActivity());
                        if (MyApplication.mBPresenter.getRecordsFragment() != null) {
                            MyApplication.mBPresenter.getRecordsFragment().getList();
                        }
                    }
                    break;

                case 0x04:
                    Log.d(TAG, "handleMessage: 蓝牙断开");
                    MyApplication.isKeyboardShow = false;
                    releaseAudioFocus();//结束通话释放焦点
                    WindowDialog.initInstance();
                    BtUtils.finish(CallingActivity.callingActivity);
                    BtUtils.finish(IncomingActivity.bTphoneCallActivity);
                    if (MyApplication.mBPresenter != null) {
                        if (MyApplication.mBPresenter.getContactFragment() != null) {
                            MyApplication.mBPresenter.getContactFragment().clearContacts();
                        }
                        if (MyApplication.mBPresenter.getRecordsFragment() != null) {
                            MyApplication.mBPresenter.getRecordsFragment().clearRecords();
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
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            return false;
        }
    });





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


    private UiCallbackBluetooth mCallbackBluetooth = new UiCallbackBluetooth.Stub() {

        @Override
        public void onBluetoothServiceReady() throws RemoteException {

        }

        @Override
        public void onAdapterStateChanged(int i, int i1) throws RemoteException {
            if (i1 == NfDef.BT_STATE_ON) {
                //蓝牙开
                //设置蓝牙名称为地址后6位。
                setBluetoothName();

            } else if (i1 == NfDef.BT_STATE_OFF) {
                //蓝牙关

            }
        }

        @Override
        public void onAdapterDiscoverableModeChanged(int i, int i1) throws RemoteException {

        }

        @Override
        public void onAdapterDiscoveryStarted() throws RemoteException {

        }

        @Override
        public void onAdapterDiscoveryFinished() throws RemoteException {

        }

        @Override
        public void retPairedDevices(int i, String[] strings, String[] strings1, int[] ints, byte[] bytes) throws RemoteException {

        }

        @Override
        public void onDeviceFound(String s, String s1, byte b) throws RemoteException {

        }

        @Override
        public void onDeviceBondStateChanged(String s, String s1, int i, int i1) throws RemoteException {

        }

        @Override
        public void onDeviceUuidsUpdated(String s, String s1, int i) throws RemoteException {

        }

        @Override
        public void onLocalAdapterNameChanged(String s) throws RemoteException {

        }

        @Override
        public void onDeviceOutOfRange(String s) throws RemoteException {

        }

        @Override
        public void onDeviceAclDisconnected(String s) throws RemoteException {

        }

        @Override
        public void onBtRoleModeChanged(int i) throws RemoteException {

        }

        @Override
        public void onBtAutoConnectStateChanged(String s, int i, int i1) throws RemoteException {

        }

        @Override
        public void onBtBasicConnectStateChanged(String s, int i, int i1) throws RemoteException {

        }

        @Override
        public void onHfpStateChanged(String s, int i, int i1) throws RemoteException {

        }

        @Override
        public void onA2dpStateChanged(String s, int i, int i1) throws RemoteException {

        }

        @Override
        public void onAvrcpStateChanged(String s, int i, int i1) throws RemoteException {

        }

        @Override
        public void onPairStateChanged(String s, String s1, int i, int i1) throws RemoteException {

        }

        @Override
        public void onMainDevicesChanged(String s, String s1) throws RemoteException {

        }
    };
    private UiCallbackHfp mCallbackHfp = new UiCallbackHfp.Stub() {

        @Override
        public void onHfpServiceReady() throws RemoteException {

        }

        @Override
        public void onHfpStateChanged(String address, int prevState, int newState) throws RemoteException {
            if (newState == NfDef.STATE_READY) {//蓝牙已断开连接
                BtUtils.finish(IncomingActivity.bTphoneCallActivity);
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
            Log.d(TAG, "onHfpCallChanged: 车机型号==" + mAdayoVersion + " backCarState 倒车状态= " + backCarState);
            // 去电：7 来电： 挂断：
            String callNumber = call.getNumber();
            String callName = "";
            if (MyApplication.mBPresenter != null) {
                callName = GetInfoFormContacts.getNameFromContacts(callNumber);
            }
            if (call.getState() == NfHfpClientCall.CALL_STATE_ACTIVE) {//通话中
//                    stopBell();
                Log.d(TAG, "onHfpCallChanged:通话中: " + callName);
                applyAudioFocus(call.getState());
                if (!backCarState && !MyApplication.iCall_state) {
                    //不在倒车状态 显示界面
                    CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                    management.showCallInterface(CallService.this, CallInterfaceManagement.SHOW_TYPE_OUT);
                }
                if (MyApplication.iCall_state && MyApplication.mBPresenter != null) {
                    Log.d(TAG, "onHfpCallChanged: ibCall正在进行 蓝牙电话转为私密模式");
                    MyApplication.mBPresenter.reqHfpAudioTransferToPhone();
                }
                BtUtils.finish(IncomingActivity.bTphoneCallActivity);
            } else if (call.getState() == NfHfpClientCall.CALL_STATE_TERMINATED) {  //结束通话
//                    stopBell();         //停止来电铃声
                BtUtils.finish(IncomingActivity.bTphoneCallActivity);
                List<NfHfpClientCall> hfpCallList = mCommand.getHfpCallList();
                if (hfpCallList.size() == 0) {
                    mhandler.sendEmptyMessage(0x03);

                }

            }
            if (call.isOutgoing() == false) {
//来电
                if (call.getState() == NfHfpClientCall.CALL_STATE_INCOMING) {   //来电
                    Log.d(TAG, "------------CALL_STATE_INCOMING-----来电---------------");
                    applyAudioFocus(call.getState());     //申请焦点
                    collapsingNotification(CallService.this);
                    if (!backCarState) {
                        //不在倒车状态 显示界面
                        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                        management.showCallInterface(CallService.this, CallInterfaceManagement.SHOW_TYPE_IN);
                    }


                } else if (call.getState() == NfHfpClientCall.CALL_STATE_DIALING || call.getState() == NfHfpClientCall.CALL_STATE_ALERTING) {     //去电
                    Log.d(TAG, "onHfpCallChanged:去电电话1: " + callName);
                    if (!backCarState) {
                        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                        management.showCallInterface(CallService.this, CallInterfaceManagement.SHOW_TYPE_OUT);
                    }


                }
            } else {
                //去电 ||call.getState() == NfHfpClientCall.CALL_STATE_ALERTING
                if (call.getState() == NfHfpClientCall.CALL_STATE_DIALING || call.getState() == NfHfpClientCall.CALL_STATE_ALERTING) {
                    Log.d(TAG, "onHfpCallChanged:去电电话2: " + callName);
                    applyAudioFocus(call.getState());
                    if (!backCarState) {
                        //不在倒车状态 显示界面
                        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                        management.showCallInterface(CallService.this, CallInterfaceManagement.SHOW_TYPE_OUT);
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

    private UiCallbackPbap mCallbackPbap = new UiCallbackPbap.Stub() {

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

    /**
     * 获取日期时间
     **/
    private static String getDatetime() {
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");//设置日期格式
        SimpleDateFormat time = new SimpleDateFormat("HHmmss");//设置时间格式
        return date.format(new Date()) + "T" + time.format(new Date());
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
        String currentAudioFocus =  SrcMngSwitchProxy.getInstance().getCurrentAudioFocus();
        Log.d(TAG, "releaseAudioFocus: 当前焦点  == "+currentAudioFocus);
        if(!currentAudioFocus.equals(AdayoSource.ADAYO_SOURCE_BT_PHONE)){
            Log.d(TAG, "releaseAudioFocus: 当前焦点不在蓝牙电话，不需要释放");
            return;
        }
        setParms(false,7);
        new Thread() {
            @Override
            public void run() {
                mSrcMngAudioSwitchProxy = SrcMngAudioSwitchProxy.getInstance();
                if (mSrcMngAudioSwitchProxy != null) {
                    if (mSrcMngAudioSwitchProxy.abandonAdayoAudioFocus()) {
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
    private void applyAudioFocus(final int state) {
        if (MyApplication.iCall_state) {
            Log.d(TAG, "applyAudioFocus: i/bCall 正在通话不申请焦点");
            return;
        }
        String currentAudioFocus =  SrcMngSwitchProxy.getInstance().getCurrentAudioFocus();
        Log.d(TAG, "applyAudioFocus: 当前焦点  == "+currentAudioFocus);
        if(currentAudioFocus.equals(AdayoSource.ADAYO_SOURCE_BT_PHONE)){
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
                            Log.d(TAG, "onGainAfterSwitchChannel: ");

                        }

                        @Override
                        public void onGainBeforeSwitchChannel() {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onGainBeforeSwitchChannel: ");

                        }

                        @Override
                        public void onLossAfterSwitchChannel() {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onLossAfterSwitchChannel: ");

                        }

                        @Override
                        public void onLossBeforeSwitchChannel() {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onLossBeforeSwitchChannel: ");
                        }

                        @Override
                        public void onLossTransientAfterSwitchChannel() {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onLossTransientAfterSwitchChannel: ");
                        }

                        @Override
                        public void onLossTransientBeforeSwitchChannel() {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onLossTransientBeforeSwitchChannel: ");
                        }

                        @Override
                        public void onLossTransientCanDuckAfterSwitchChannel() {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onLossTransientCanDuckAfterSwitchChannel: ");
                        }

                        @Override
                        public void onLossTransientCanDuckBeforeSwitchChannel() {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onLossTransientCanDuckBeforeSwitchChannel: ");
                        }
                    }, CallService.this);

                    //AudioManager.STREAM_VOICE_CALL   AUDIOFOCUS_GAIN_TRANSIENT AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                    if (mSrcMngAudioSwitchProxy.requestAdayoAudioFocus(6, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)) {//经江文尧工确认，换成混音AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                        Log.i(TAG, "------------------------------------------申请焦点成功-----AUDIOFOCUS_GAIN_TRANSIENT-----------------------------------------");
                        setParms(true,state);
                    } else {
                        Log.i(TAG, "------------------------------------------申请焦点失败-----------AUDIOFOCUS_GAIN_TRANSIENT-----------------------------------");
                    }
                } else {
                    Log.d(TAG, "mSrcMngAudioSwitchProxy is null");
                }
            }

        }.start();

    }
private  boolean muteSwitch = false;
    private void setParms(boolean isFocus,int state){
        SettingExternalManager settingsManager = SettingExternalManager.getSettingsManager();
        Log.d(TAG, "setParms: isFocus="+isFocus +"--state--"+state);
        if(isFocus){
            try {
                if(state != NfHfpClientCall.CALL_STATE_INCOMING){
                    mCommand.startHfpRender();
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            boolean muteSwitch1 = settingsManager.getMuteSwitch();
            this.muteSwitch = muteSwitch1;
            Log.d(TAG, "setParms: muteSwitch = "+muteSwitch);
            if(muteSwitch){
                settingsManager.setMuteSwitch(false);
            }
        }else {
            try {
                mCommand.pauseHfpRender();

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "setParms: muteSwitch = "+muteSwitch);
            if(this.muteSwitch){
                Log.d(TAG, "setParms: 电话之前是静音，挂断后设置为静音");
                settingsManager.setMuteSwitch(true);
            }

        }
    }


}

