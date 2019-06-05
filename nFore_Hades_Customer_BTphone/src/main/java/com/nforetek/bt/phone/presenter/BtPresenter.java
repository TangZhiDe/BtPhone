package com.nforetek.bt.phone.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.base.jar.NforeBtBaseJar;
import com.nforetek.bt.base.listener.BluetoothMusicChangeListener;
import com.nforetek.bt.base.listener.BluetoothPhoneChangeListener;
import com.nforetek.bt.base.listener.BluetoothServiceConnectedListener;
import com.nforetek.bt.base.listener.BluetoothSettingChangeListener;
import com.nforetek.bt.base.listener.BluetoothSppChangeListener;
import com.nforetek.bt.bean.CallLogs;
import com.nforetek.bt.bean.Contacts;
import com.nforetek.bt.phone.CallingActivity;
import com.nforetek.bt.phone.ContactFragment;
import com.nforetek.bt.phone.RecordsFragment;
import com.nforetek.bt.phone.pvcontract.IBtContract;
import com.nforetek.bt.phone.tools.WindowDialog;

import java.util.List;

/**
 * @author tzd
 *
 */
public class BtPresenter implements
		BluetoothServiceConnectedListener,
		BluetoothSettingChangeListener,
		BluetoothPhoneChangeListener,
		BluetoothMusicChangeListener,
		BluetoothSppChangeListener {

//	private final static BtPresenter INSTANCE = new BtPresenter();
	private static final String TAG = BtPresenter.class.getCanonicalName();
	public static List<Contacts> contactsList = null;
	public static List<CallLogs> callLogsList = null;
	private ContactFragment contactFragment;
	private RecordsFragment recordsFragment;
	
	private UiBluetoothServiceConnectedListener mServiceConnectedListener;
    private UiBluetoothSettingChangeListerer mSettingChangeListener;
    private UiBluetoothPhoneChangeListerer mPhoneChangeListener;
    private UiBluetoothMusicChangeListerer mMusicChangeListener;
    private UiBluetoothSppChangeListerer mSppChangeListener;
	private IBtContract mView;

	private CallingActivity callingActivity;
	private WindowDialog windowDialog;
    
    /** 是否已储存ui通话记录到数据库 **/
    private static boolean isSetUiCallLogsToDB = false;
//    private BtPresenter(){};
    /** 获得Presenter实例 **/
//    public static BtPresenter getInstance(){
//    	Log.i(TAG,"----------getInstance-----------");
//    	return INSTANCE;
//    }
//

	public BtPresenter(){

	}

	public void registerView(IBtContract view){
		mView = view;
	}

	public void refresh(){
		Log.e(TAG, "refresh: presenter" );
		mView.refresh();
	}

    /** 获取缓存联系人 **/
    public List<Contacts> getContactsList() {
		return contactsList;
	}

    /** 缓存联系人 **/
	public void setContactsList(List<Contacts> mContactsList) {
		contactsList = mContactsList;
//		StaticDefConf.isDownContact = true;
	}

	/** 获取缓存通话记录 **/
	public List<CallLogs> getCallLogsList() {
		return callLogsList;
	}

	/** 缓存通话记录 **/
	public void setCallLogsList(List<CallLogs> mCallLogsList) {
		callLogsList = mCallLogsList;
//		StaticDefConf.isDownCallLogs = true;
	}

	public ContactFragment getContactFragment() {
		return contactFragment;
	}

	public void setContactFragment(ContactFragment contactFragment) {
		this.contactFragment = contactFragment;
	}

	public RecordsFragment getRecordsFragment() {
		return recordsFragment;
	}

	public void setRecordsFragment(RecordsFragment recordsFragment) {
		this.recordsFragment = recordsFragment;
	}

	public CallingActivity getCallingActivity() {
		return callingActivity;
	}

	public void setCallingActivity(CallingActivity callingActivity) {
		this.callingActivity = callingActivity;
	}

	public WindowDialog getWindowDialog() {
		return windowDialog;
	}

	public void setWindowDialog(WindowDialog windowDialog) {
		this.windowDialog = windowDialog;
	}

	/** 蓝牙服务连接监听，调用者应实现此接口以获取蓝牙服务连接状态包含的回调 **/
    public interface UiBluetoothServiceConnectedListener{
    	 /** service连接状态 **/
        void onServiceConnectedChanged(boolean isConnected);
    }
    
    /** 蓝牙设置各接口回调监听，调用者应实现此接口以获取蓝牙设置功能包含的回调 **/
    public interface UiBluetoothSettingChangeListerer{
    	/** 蓝牙开关状态回调 **/
        void onEnableChanged(boolean isEnable);
        /** 蓝牙连接状态回调，只要有任意一个协议（hfp,a2dp,avrcp）连接，则返回true **/
        void onConnectedChanged(String address, int connectState);
        /** 蓝牙hfp连接状态回调 **/
        void onHfpStateChanged(String address, int connectState);
        /** 蓝牙 HFP 连接远程设备的音频状态变化 **/
        void onHfpAudioStateChanged(String address, int prevState, int newState);
        /** 蓝牙avrcp连接状态回调 **/
        void onAvrcpStateChanged(String address, int connectState);
        /** 蓝牙a2dp连接状态回调 **/
        void onA2dpStateChanged(String address, int connectState);
        /** 开始扫描新设备回调 **/
        void onAdapterDiscoveryStarted();
        /** 结束扫描新设备回调 **/
        void onAdapterDiscoveryFinished();
        /** 历史配对设备回调 **/
        void retPairedDevices(int elements, String[] address, String[] name, int[] supportProfile);
        /** 扫描到新设备回调 **/
        void onDeviceFound(String address, String name);
        /** 配对状态回调 **/
        void onDeviceBondStateChanged(String address, String name, int newState);
        /** 车机蓝牙名称变化回调 **/
        void onLocalAdapterNameChanged(String name);
    }
    
    /** 蓝牙电话各接口回调监听，调用者应实现此接口以获取蓝牙电话功能包含的回调 **/
    public interface UiBluetoothPhoneChangeListerer{
        /** 通话状态发生变化回调 **/
    	void onHfpCallChanged(String address, NfHfpClientCall call);
    	 /** 通话时间回调 **/
    	void onHfpCallingTimeChanged(String time);
    	/**
         * 通讯录或通话记录下载状态改变回调
         * @param sycnState
         */
        void onPbapStateChanged(int sycnState);
    }
    
    /** 蓝牙音乐各接口回调监听，调用者应实现此接口以获取蓝牙音乐功能包含的回调 **/
    public interface UiBluetoothMusicChangeListerer{
        /** 响应来自A2DP/AVRCP连接的远程设备对reqAvrcp13GetCapabilitiesSupportEvent的回调 **/
    	void retAvrcp13CapabilitiesSupportEvent(byte[] eventIds);
    	/** 响应来自A2DP/AVRCP连接的远程设备对reqAvrcp13GetPlayerSettingAttributesList的回调 **/
    	void retAvrcp13PlayerSettingAttributesList(byte[] attributeIds);
    	/** 响应来自A2DP/AVRCP连接的远程设备对reqAvrcp13GetElementAttributesPlaying的回调 **/
    	void retAvrcp13ElementAttributesPlaying(int[] metadataAtrributeIds, String[] texts);
    	/** 响应来自A2DP/AVRCP连接的远程设备对reqAvrcp13GetPlayStatus的回调 **/
    	void retAvrcp13PlayStatus(long songLen, long songPos, byte statusId);
    	/** 响应来自A2DP/AVRCP连接的远程设备播放状态改变的回调 **/
    	void onAvrcp13EventPlaybackStatusChanged(byte statusId);
    	/** 回调以通知A2DP/AVRCP连接的远程设备的track改变。只有在事件AVRCP_EVENT_ID_TRACK_CHANGED之前使用reqAvrcpRegisterEventWatcher进行了注册时才会调用该回调。 **/
    	void onAvrcp13EventTrackChanged(long elementId);
    	/** 回调通知A2DP/AVRCP连接的远程设备的播放器应用程序设置的更改。只有在事件AVRCP_EVENT_ID_PLAYER_APPLICATION_SETTING_CHANGED之前使用reqAvrcpRegisterEventWatcher进行了注册时才会调用该回调。  **/
    	void onAvrcp13EventPlayerSettingChanged(byte[] attributeIds, byte[] valueIds);
    	/** 
    	 * 回调通知A2DP/AVRCP连接的远程设备的ID3信息
    	 * @param artist 艺术家 
    	 * @param album 专辑
    	 * @param title 歌曲名
    	 */
    	void retAvrcpUpdateSongStatus(String artist, String album, String title);
    }
    
    public interface UiBluetoothSppChangeListerer{
    	/** 通知 SPP 服务准备就绪 **/
    	void onSppServiceReady();
    	/** 回调,告知 SPP 连接远程设备的状态变化 **/
    	void onSppStateChanged(String address, String deviceName, int prevState, int newState);
    	/** 回调,以告知 SPP 连接远程设备的错误响应 **/
    	void onSppErrorResponse(String address, int errorCode);
    	/** 回调对 reqSppConnectedDeviceAddressList 蓝牙硬件地址的响应将以 “ 00:11:22:33:AA:BB”的格式 **/
    	void retSppConnectedDeviceAddressList(int totalNum, String[] addressList, String[] nameList);
    	/** 回调用 SPP 连接的远程设备从设备接收数据 **/
    	void onSppDataReceived(String address, byte[] receivedData);
    	/** 回调通知数据已发送到 SPP 连接的远程设备 **/
    	void onSppSendData(String address, int length);
    	/** 回调通知这个苹果设备需要做 iAP 认证。蓝牙硬件地址的格式为 “ 00:11:22:33:AA:BB” **/
    	void onSppAppleIapAuthenticationRequest(String address);
    }

    public void registerUiBluetoothServiceConnectedListener(UiBluetoothServiceConnectedListener listener){
    	Log.i(TAG,"-----------------registerUiBluetoothServiceConnectedListener--------------------");
        mServiceConnectedListener = listener;
    }
    
    public void registerUiBluetoothSettingChangeListerer(UiBluetoothSettingChangeListerer listener){
    	Log.i(TAG,"-----------------registerUiBluetoothSettingChangeListerer--------------------");
        mSettingChangeListener = listener;
    }
    
    public void registerUiBluetoothPhoneChangeListerer(UiBluetoothPhoneChangeListerer listener){
    	Log.i(TAG,"-----------------registerUiBluetoothPhoneChangeListerer--------------------");
        mPhoneChangeListener = listener;
    }
    
    public void registerUiBluetoothMusicChangeListerer(UiBluetoothMusicChangeListerer listener){
    	Log.i(TAG,"-----------------registerUiBluetoothMusicChangeListerer--------------------");
        mMusicChangeListener = listener;
    }
    
    public void registerUiBluetoothSppChangeListerer(UiBluetoothSppChangeListerer listener){
    	Log.i(TAG,"-----------------registerUiBluetoothSppChangeListerer--------------------");
    	mSppChangeListener = listener;
    }
    
    public void unregisterUiBluetoothServiceConnectedListener(){
    	Log.i(TAG,"-----------------unregisterUiBluetoothServiceConnectedListener--------------------");
        mServiceConnectedListener = null;
    }

    public void unregisterUiBluetoothSettingChangeListerer(){
    	Log.i(TAG,"-----------------unregisterUiBluetoothSettingChangeListerer--------------------");
        mSettingChangeListener = null;
    }

    public void unregisterUiBluetoothPhoneChangeListerer(){
    	Log.i(TAG,"-----------------unregisterUiBluetoothPhoneChangeListerer--------------------");
        mPhoneChangeListener = null;
    }

    public void unregisterUiBluetoothMusicChangeListerer(){
    	Log.i(TAG,"-----------------unregisterUiBluetoothMusicChangeListerer--------------------");
        mMusicChangeListener = null;
    }

    public void unregisterUiBluetoothSppChangeListerer(){
    	Log.i(TAG,"-----------------unregisterUiBluetoothSppChangeListerer--------------------");
    	mSppChangeListener = null;
    }


    /** 注册服务及serviceCallback **/
	public boolean registerServiceListener(Context context) {
		Log.i(TAG, "---------------------registerServiceListener--------------------------");
		boolean isConnected = NforeBtBaseJar.init(context);
		NforeBtBaseJar.registerBluetoothServiceConnectedListener(this);
		NforeBtBaseJar.registerBluetoothSettingChangeListener(this);
		NforeBtBaseJar.registerBluetoothPhoneChangeListener(this);
		NforeBtBaseJar.registerBluetoothMusicChangeListener(this);
		NforeBtBaseJar.registerBluetoothSppChangeListener(this);
		return isConnected;
	}

	/** 注销服务及serviceCallback **/
	public void unregisterServiceListener() {
		Log.i(TAG, "-----------------unregisterServiceListener------------------------");
		NforeBtBaseJar.release();
	}

	//-------------------------------------------------------------------------------------------------------------
	/**
     * 获取蓝牙连接状态，只要有一个协议连接就算是连接了
     * @return
     */
    public boolean isBtConnected(){
    	Log.i(TAG,"-----------------isBtConnected--------------------");
        return NforeBtBaseJar.isBtConnected(NforeBtBaseJar.getBtMainDevices());
    }
    

    /**
     * 获取当前联系人或者通话记录同步状态
     */
    public int getPbapDownLoadState(){
    	Log.i(TAG,"-----------------getPbapDownLoadState--------------------");
        return NforeBtBaseJar.getPbapDownLoadState();
    }

    /**
     * 播放下一曲蓝牙音乐
     */
    public void playNext(){
    	Log.i(TAG,"-----------------playNext--------------------");
    	NforeBtBaseJar.playNext();
    }

    /**
     * 播放/暂停蓝牙音乐
     * 蓝牙音乐根据目前播放的一个状态自动进行暂停还是播放
     */
//    public void play(){
//    	Log.i(TAG,"-----------------play--------------------");
//    	NforeBtBaseJar.play();
//    }

    /**
     * 播放上一曲蓝牙音乐
     */
    public void playPrev(){
    	Log.i(TAG,"-----------------playPrev--------------------");
    	NforeBtBaseJar.playPrev();
    }

    /**
     * 挂断电话，包含来电拒接，通话后挂断
     */
//    public void hangUp(){
//    	Log.i(TAG,"-----------------hangUp--------------------");
//    	NforeBtBaseJar.hangUp();
//    }

    /**
     * 接听电话
     */
//    public void accept(){
//    	Log.i(TAG,"-----------------accept--------------------"+System.currentTimeMillis());
//    	NforeBtBaseJar.accept();
//    }

    /**
     * 拨打电话
     * @param number 电话号码
     */
//    public void callNumber(String number){
//    	Log.i(TAG,"-----------------callNumber--------------------");
//    	NforeBtBaseJar.callNumber(number);
//    }
    
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------//

    /** 检查 AVRCP 服务是否准备好了 **/
    public boolean isAvrcpServiceReady() throws RemoteException {
	    Log.v(TAG, "isAvrcpServiceReady()");
	    return NforeBtBaseJar.isAvrcpServiceReady();
	}
	
    /** 检查 A2DP 服务是否准备好了 **/
	public boolean isA2dpServiceReady() throws RemoteException {
	    Log.v(TAG, "isA2dpServiceReady()");
	    return NforeBtBaseJar.isA2dpServiceReady();
	}
	
	/** 检查 Spp 服务是否准备好了 **/
	public boolean isSppServiceReady() throws RemoteException {
	    Log.v(TAG, "isSppServiceReady()");
	    return NforeBtBaseJar.isSppServiceReady();
	}
	
	/** 检查 Bluetooth 服务是否准备好了 **/
	public boolean isBluetoothServiceReady() throws RemoteException {
	    Log.v(TAG, "isBluetoothServiceReady()");
	    return NforeBtBaseJar.isBluetoothServiceReady();
	}
	
	/** 检查 Hfp 服务是否准备好了 **/
	public boolean isHfpServiceReady() throws RemoteException {
	    Log.v(TAG, "isHfpServiceReady()");
	    return NforeBtBaseJar.isHfpServiceReady();
	}
	
	/** 检查 Pbap 服务是否准备好了 **/
	public boolean isPbapServiceReady() throws RemoteException {
	    Log.v(TAG, "isPbapServiceReady()");
	    return NforeBtBaseJar.isPbapServiceReady();
	}
	
	/** 获取 UiService 版本名称 **/
	public String getUiServiceVersionName() throws RemoteException {
		Log.v(TAG, "getUiServiceVersionName()");
	    return NforeBtBaseJar.getUiServiceVersionName();
	}
	
	/** 获取 A2dp 远程设备的当前连接状态 **/
	public int getA2dpConnectionState() throws RemoteException {
	    Log.v(TAG,"getA2dpConnectionState()");
	    return NforeBtBaseJar.getA2dpConnectionState();
	}
	
	/** 检查本地设备 A2DP 是否已连接远程设备 **/
	public boolean isA2dpConnected() throws RemoteException {
	    Log.v(TAG,"isA2dpConnected()");
	    return NforeBtBaseJar.isA2dpConnected(NforeBtBaseJar.getBtMainDevices());
	}
	
	/** 得到 A2DP 连接远程设备的蓝牙硬件地址 **/
	public String getA2dpConnectedAddress() throws RemoteException {
	    Log.v(TAG,"getA2dpConnectedAddress()");
	    return NforeBtBaseJar.getA2dpConnectedAddress();
	}
	
	/** 请求连接 A2DP 到远程设备 **/
	public boolean reqA2dpConnect(String address) throws RemoteException {
	    Log.v(TAG,"reqA2dpConnect() " + address);
	    return NforeBtBaseJar.reqA2dpConnect(address);
	}
	
	/** 请求断开连接 A2DP 到远程设备 **/
	public boolean reqA2dpDisconnect(String address) throws RemoteException {
	    Log.v(TAG,"reqA2dpDisconnect() " + address);
	    return NforeBtBaseJar.reqA2dpDisconnect(address);
	}
	
	/** 停止发送 A2DP 流音频数据 **/
	public void pauseA2dpRender() throws RemoteException {
	    Log.v(TAG,"pauseA2dpRender()");
	    NforeBtBaseJar.pauseA2dpRender();
	}
	
	/** 开始发送 A2DP 流音频数据 **/
	public void startA2dpRender() throws RemoteException {
	    Log.v(TAG,"startA2dpRender()");
	    NforeBtBaseJar.startA2dpRender();
	
	}
	
	/** 请求设置 A2DP 流媒体音乐音量 **/
	public boolean setA2dpLocalVolume(float vol) throws RemoteException {
	    Log.v(TAG,"setA2dpLocalVolume() " + vol);
	    return NforeBtBaseJar.setA2dpLocalVolume(vol);
	
	}
	
	/** 请求设置当前流类型的音频系统 **/
	public boolean setA2dpStreamType(int type) throws RemoteException {
	    Log.v(TAG,"setA2dpStreamType() " + type);
	    return NforeBtBaseJar.setA2dpStreamType(type);
	
	}
	
	/** 请求获取当前流类型的音频系统 **/
	public int getA2dpStreamType() throws RemoteException {
	    Log.v(TAG,"getA2dpStreamType()");
	    return NforeBtBaseJar.getA2dpStreamType();
	
	}
	
	/** 获取 Avrcp 远程设备的当前连接状态 **/
	public int getAvrcpConnectionState() throws RemoteException {
	    Log.v(TAG,"getAvrcpConnectionState()");
	    return NforeBtBaseJar.getAvrcpConnectionState();
	}
	
	/** 检查本地设备 Avrcp 是否已连接远程设备 **/
	public boolean isAvrcpConnected() throws RemoteException {
	    Log.v(TAG,"isAvrcpConnected()");
	    return NforeBtBaseJar.isAvrcpConnected(NforeBtBaseJar.getBtMainDevices());
	}
	
	/** 得到 AVRCP 连接远程设备的蓝牙硬件地址 **/
	public String getAvrcpConnectedAddress() throws RemoteException {
	    Log.v(TAG,"getAvrcpConnectedAddress()");
	    return NforeBtBaseJar.getAvrcpConnectedAddress();
	}
	
	/** 请求连接 AVRCP 到远程设备 **/
	public boolean reqAvrcpConnect(String address) throws RemoteException {
	    Log.v(TAG,"reqAvrcpConnect() " + address);
	    return NforeBtBaseJar.reqAvrcpConnect(address);
	}
	
	/** 请求断开连接 AVRCP 到远程设备 **/
	public boolean reqAvrcpDisconnect(String address) throws RemoteException {
	    Log.v(TAG,"reqAvrcpDisconnect() " + address);
	    return NforeBtBaseJar.reqAvrcpDisconnect(address);
	}
	
	/** 请求远程设备是否支持 AVRCP 1.3 **/
	public boolean isAvrcp13Supported(String address) throws RemoteException {
	    Log.v(TAG,"isAvrcp13Supported() " + address);
	    return NforeBtBaseJar.isAvrcp13Supported(address);
	}
	
	/** 请求远程设备是否支持 AVRCP 1.4 **/
	public boolean isAvrcp14Supported(String address) throws RemoteException {
	    Log.v(TAG,"isAvrcp14Supported() " + address);
	    return NforeBtBaseJar.isAvrcp14Supported(address);
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备做“播放”操作 **/
	public boolean reqAvrcpPlay() throws RemoteException {
	    Log.v(TAG,"reqAvrcpPlay()");
	    return NforeBtBaseJar.reqAvrcpPlay();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备做“停止”操作 **/
	public boolean reqAvrcpStop() throws RemoteException {
	    Log.v(TAG,"reqAvrcpStop()");
	    return NforeBtBaseJar.reqAvrcpStop();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备做“暂停”操作 **/
	public boolean reqAvrcpPause() throws RemoteException {
	    Log.v(TAG,"reqAvrcpPause()");
	    return NforeBtBaseJar.reqAvrcpPause();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备做“音量+”操作 **/
	public boolean reqAvrcpVolumeUp() throws RemoteException {
	    Log.v(TAG,"reqAvrcpVolumeUp()");
	    return NforeBtBaseJar.reqAvrcpVolumeUp();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备做“音量-”操作 **/
	public boolean reqAvrcpVolumeDown() throws RemoteException {
	    Log.v(TAG,"reqAvrcpVolumeDown()");
	    return NforeBtBaseJar.reqAvrcpVolumeDown();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备开始“快进”操作 **/
	public boolean reqAvrcpStartFastForward() throws RemoteException {
	    Log.v(TAG,"reqAvrcpStartFastForward()");
	    return NforeBtBaseJar.reqAvrcpStartFastForward();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备停止“快进”操作 **/
	public boolean reqAvrcpStopFastForward() throws RemoteException {
	    Log.v(TAG,"reqAvrcpStopFastForward()");
	    return NforeBtBaseJar.reqAvrcpStopFastForward();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备开始“快退”操作 **/
	public boolean reqAvrcpStartRewind() throws RemoteException {
	    Log.v(TAG,"reqAvrcpStartRewind()");
	    return NforeBtBaseJar.reqAvrcpStartRewind();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备停止“快退”操作 **/
	public boolean reqAvrcpStopRewind() throws RemoteException {
	    Log.v(TAG,"reqAvrcpStopRewind()");
	    return NforeBtBaseJar.reqAvrcpStopRewind();
	}
	
	/** 请求从 A2DP/AVRCP 连接远程设备有能力支持的事件 **/
	public boolean reqAvrcp13GetCapabilitiesSupportEvent() throws RemoteException {
	    Log.v(TAG,"reqAvrcp13GetCapabilitiesSupportEvent()");
	    return NforeBtBaseJar.reqAvrcp13GetCapabilitiesSupportEvent();
	}
	
	/** 请求获得支持的播放器应用程序从 A2DP/AVRCP 连接远程设备设置属性 **/
	public boolean reqAvrcp13GetPlayerSettingAttributesList()
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcp13GetPlayerSettingAttributesList()");
	    return NforeBtBaseJar.reqAvrcp13GetPlayerSettingAttributesList();
	}
	
	/** 请求从 A2DP/AVRCP 连接远程设备获取请求的播放器应用程序设置属性的可能值集 **/
	public boolean reqAvrcp13GetPlayerSettingValuesList(byte attributeId)
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcp13GetPlayerSettingValuesList() attributeId: " + attributeId);
	    return NforeBtBaseJar.reqAvrcp13GetPlayerSettingValuesList(attributeId);
	}
	
	/** 请求从 A2DP/AVRCP 连接远程设备的当前设置值,用于提供的播放器应用程序设置属性 **/
	public boolean reqAvrcp13GetPlayerSettingCurrentValues()
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcp13GetPlayerSettingCurrentValues()");
	    return NforeBtBaseJar.reqAvrcp13GetPlayerSettingCurrentValues();
	}
	
	/** 请求将播放器应用程序设置值设置为 A2DP/AVRCP 连接远程设备,用于相应的定义 **/
	public boolean reqAvrcp13SetPlayerSettingValue(byte attributeId,
	        byte valueId) throws RemoteException {
	    Log.v(TAG,"reqAvrcp13SetPlayerSettingValue() attributeId: " + attributeId + " valueId: " + valueId);
	    return NforeBtBaseJar.reqAvrcp13SetPlayerSettingValue(attributeId, valueId);
	}
	
	/** 请求获取由 A2DP/AVRCP 连接远程设备的参数中指定的元素的属性 **/
	public boolean reqAvrcp13GetElementAttributesPlaying() throws RemoteException {
	    Log.v(TAG,"reqAvrcp13GetElementAttributesPlaying()");
	    return NforeBtBaseJar.reqAvrcp13GetElementAttributesPlaying();
	}
	
	/** 请求从 A2DP/AVRCP 连接远程设备获得当前播放媒体的状态 **/
	public boolean reqAvrcp13GetPlayStatus() throws RemoteException {
	    Log.v(TAG,"reqAvrcp13GetPlayStatus()");
	    return NforeBtBaseJar.reqAvrcp13GetPlayStatus();
	}
	
	/** 请求使用 A2DP/AVRCP 连接远程设备,根据发生的特定事件异步接收通知 **/
	public boolean reqAvrcpRegisterEventWatcher(byte eventId, long interval)
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcpRegisterEventWatcher() eventId: " + eventId + " interval: " + interval);
	    return NforeBtBaseJar.reqAvrcpRegisterEventWatcher(eventId, interval);
	}
	
	/** 请求使用 A2DP/AVRCP 连接远程设备注销特定事件 **/
	public boolean reqAvrcpUnregisterEventWatcher(byte eventId)
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcpUnregisterEventWatcher() eventId: " + eventId);
	    return NforeBtBaseJar.reqAvrcpUnregisterEventWatcher(eventId);
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备,以移动到下一组的第一首歌 **/
	public boolean reqAvrcp13NextGroup() throws RemoteException {
	    Log.v(TAG,"reqAvrcp13NextGroup()");
	    return NforeBtBaseJar.reqAvrcp13NextGroup();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备,以移动到上一组的第一首歌 **/
	public boolean reqAvrcp13PreviousGroup() throws RemoteException {
	    Log.v(TAG,"reqAvrcp13PreviousGroup()");
	    return NforeBtBaseJar.reqAvrcp13PreviousGroup();
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备的浏览渠道是否已建立 **/
	public boolean isAvrcp14BrowsingChannelEstablished() throws RemoteException {
	    Log.v(TAG,"isAvrcp14BrowsingChannelEstablished()");
	    return NforeBtBaseJar.isAvrcp14BrowsingChannelEstablished();
	}
	
	/** 请求 Uiorm A2DP/AVRCP 连接远程设备,我们希望控制的媒体播放器 **/
	public boolean reqAvrcp14SetAddressedPlayer(int playerId)
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcp14SetAddressedPlayer() playerId: " + playerId);
	    return NforeBtBaseJar.reqAvrcp14SetAddressedPlayer(playerId);
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备,以将浏览命令路由到哪个播放器 **/
	public boolean reqAvrcp14SetBrowsedPlayer(int playerId)
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcp14SetBrowsedPlayer() playerId: " + playerId);
	    return NforeBtBaseJar.reqAvrcp14SetBrowsedPlayer(playerId);
	}
	
	/** 请求检索 A2DP/AVRCP 连接远程设备上的文件夹的内容列表 **/
	public boolean reqAvrcp14GetFolderItems(byte scopeId)
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcp14GetFolderItems() scopeId: " + scopeId);
	    return NforeBtBaseJar.reqAvrcp14GetFolderItems(scopeId);
	}
	
	/** 请求在 A2DP/AVRCP 连接远程设备上导航虚拟文件系统 **/
	public boolean reqAvrcp14ChangePath(int uidCounter, long uid, byte direction)
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcp14ChangePath() uidCounter: " + uidCounter + " uid: " + uid + " direction: " + direction);
	    return NforeBtBaseJar.reqAvrcp14ChangePath(uidCounter, uid, direction);
	}
	
	/** 请求检索 A2DP/AVRCP 连接远程设备上的特定媒体元素项或文件夹项的元数据属性 **/
	public boolean reqAvrcp14GetItemAttributes(byte scopeId, int uidCounter,
	        long uid) throws RemoteException {
	    Log.v(TAG,"reqAvrcp14GetItemAttributes() scopeId: " + scopeId + " uidCounter: " + uidCounter);
	    return NforeBtBaseJar.reqAvrcp14GetItemAttributes(scopeId, uidCounter, uid);
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备开始播放 UID 指示的项目 **/
	public boolean reqAvrcp14PlaySelectedItem(byte scopeId, int uidCounter,
	        long uid) throws RemoteException {
	    Log.v(TAG,"reqAvrcp14PlaySelectedItem() scopeId: " + scopeId + " uidCounter: " + uidCounter + " uid: " + uid);
	    return NforeBtBaseJar.reqAvrcp14PlaySelectedItem(scopeId, uidCounter, uid);
	}
	
	/** 请求从当前文件夹和位于浏览器的播放器虚拟文件系统下面的所有文件夹执行基本搜索。不支持正则表达式 **/
	public boolean reqAvrcp14Search(String text) throws RemoteException {
	    Log.v(TAG,"reqAvrcp14Search() text: " + text);
	    return NforeBtBaseJar.reqAvrcp14Search(text);
	}
	
	/** 请求 A2DP/AVRCP 连接远程设备,将 UID 指示的项目添加到 nowplay 队列中 **/
	public boolean reqAvrcp14AddToNowPlaying(byte scopeId, int uidCounter,
	        long uid) throws RemoteException {
	    Log.v(TAG,"reqAvrcp14AddToNowPlaying() scopeId: " + scopeId + " uidCounter: " + uidCounter + " uid: " + uid);
	    return NforeBtBaseJar.reqAvrcp14AddToNowPlaying(scopeId, uidCounter, uid);
	}
	
	/** 通过 AVRCP v1.4 规范,设置蓝牙音量 **/
	public boolean reqAvrcp14SetAbsoluteVolume(byte volume)
	        throws RemoteException {
	    Log.v(TAG,"reqAvrcp14SetAbsoluteVolume() volume: " + volume);
	    return NforeBtBaseJar.reqAvrcp14SetAbsoluteVolume(volume);
	}
	
	/** 获得 nForeService版本名称 **/
	public String getNfServiceVersionName() throws RemoteException {
	    Log.v(TAG,"getNfServiceVersionName()");
	    return NforeBtBaseJar.getNfServiceVersionName();
	}
	
	/** 设置本地蓝牙适配器以启用或禁用 **/
	public boolean setBtEnable(boolean enable) throws RemoteException {
	    Log.v(TAG,"setBtEnable() enable: " + enable);
	    return NforeBtBaseJar.setBtEnable(enable);
	}
	
	/** 设置本地蓝牙适配器,可被发现的持续时间 **/
	public boolean setBtDiscoverableTimeout(int timeout) throws RemoteException {
	    Log.v(TAG,"setBtDiscoverableTimeout() timeout: " + timeout);
	    return NforeBtBaseJar.setBtDiscoverableTimeout(timeout);
	}
	
	/** 扫描新设备 **/
	public boolean startBtDiscovery() throws RemoteException {
	    Log.v(TAG,"startBtDiscovery()");
	    return NforeBtBaseJar.startBtDiscovery();
	}
	
	/** 取消扫描新设备 **/
	public boolean cancelBtDiscovery() throws RemoteException {
	    Log.v(TAG,"cancelBtDiscovery()");
	    return NforeBtBaseJar.cancelBtDiscovery();
	}
	
	/** 请求对给定的蓝牙硬件地址进行配对 **/
	public boolean reqBtPair(String address) throws RemoteException {
	    Log.v(TAG,"reqBtPair() " + address);
	    return NforeBtBaseJar.reqBtPair(address);
	}
	
	/** 请求对给定的蓝牙硬件地址进行取消配对 **/
	public boolean reqBtUnpair(String address) throws RemoteException {
	    Log.v(TAG,"reqBtUnpair() " + address);
	    return NforeBtBaseJar.reqBtUnpair(address);
	}
	
	/** 请求获得配对设备列表 **/
	public boolean reqBtPairedDevices() throws RemoteException {
	    Log.v(TAG,"reqBtPairedDevices()");
	    return NforeBtBaseJar.reqBtPairedDevices(0);
	}
	
	/** 获取本地蓝牙适配器的名称 **/
	public String getBtLocalName() throws RemoteException {
	    Log.v(TAG,"getBtLocalName()");
	    return NforeBtBaseJar.getBtLocalName();
	}
	
	/** 请求指定地址的远程蓝牙设备名称 **/
	public String getBtRemoteDeviceName(String address) throws RemoteException {
	    Log.v(TAG,"getBtRemoteDeviceName() " + address);
	    return NforeBtBaseJar.getBtRemoteDeviceName(address);
	}
	
	/** 获取本地蓝牙适配器的地址 **/
	public String getBtLocalAddress() throws RemoteException {
	    Log.v(TAG,"getBtLocalAddress()");
	    return NforeBtBaseJar.getBtLocalAddress();
	}
	
	/** 设置本地蓝牙适配器的名称 **/
	public boolean setBtLocalName(String name) throws RemoteException {
	    Log.v(TAG,"setBtLocalName() name: " + name);
	    return NforeBtBaseJar.setBtLocalName(name);
	}
	
	/** 检查蓝牙是否启用 **/
	public boolean isBtEnabled() throws RemoteException {
	    Log.v(TAG,"isBtEnabled()");
	    return NforeBtBaseJar.isBtEnabled();
	}
	
	/** 获取本地蓝牙适配器的当前状态 **/
	public int getBtState() throws RemoteException {
	    Log.v(TAG,"getBtState()");
	    return NforeBtBaseJar.getBtState();
	}
	
	/** 检查蓝牙是否正在扫描远程设备 **/
	public boolean isBtDiscovering() throws RemoteException {
	    Log.v(TAG,"isBtDiscovering()");
	    return NforeBtBaseJar.isBtDiscovering();
	}
	
	/** 检查蓝牙是否可以从远程设备中发现 **/
	public boolean isBtDiscoverable() throws RemoteException {
	    Log.v(TAG,"isBtDiscoverable()");
	    return NforeBtBaseJar.isBtDiscoverable();
	}
	
	/** 检查自动连接是否已启用 **/
	public boolean isBtAutoConnectEnable() throws RemoteException {
	    Log.v(TAG,"isBtAutoConnectEnable()");
	    return NforeBtBaseJar.isBtAutoConnectEnable();
	}
	
	/** 请求将 HSP、HFP、A2DP 和 AVRCP 连接到远程设备 **/
	public int reqBtConnectHfpA2dp(String address) throws RemoteException {
	    Log.v(TAG,"reqBtConnectHfpA2dp() " + address);
	    return NforeBtBaseJar.reqBtConnectHfpA2dp(address);
	}
	
	/** 请求断开所有连接 **/
	public int reqBtDisconnectAll() throws RemoteException {
	    Log.v(TAG,"reqBtDisconnectAll()");
	    return NforeBtBaseJar.reqBtDisconnectAll(NforeBtBaseJar.getBtMainDevices());
	}
	
	/** 获取远程设备的支持profiles。所请求的地址必须是已配对的设备 **/
	public int getBtRemoteUuids(String address) throws RemoteException {
	    Log.v(TAG,"getBtRemoteUuids() " + address);
	    return NforeBtBaseJar.getBtRemoteUuids(address);
	}
	
	/** 将蓝牙模式切换到其他角色 **/
	public boolean switchBtRoleMode(int mode) throws RemoteException {
	    Log.v(TAG,"switchBtRoleMode() " + mode);
	    return NforeBtBaseJar.switchBtRoleMode(mode);
	}
	
	/** 在integer中获取蓝牙角色模式 **/
	public int getBtRoleMode() throws RemoteException {
	    Log.v(TAG,"getBtRoleMode()");
	    return NforeBtBaseJar.getBtRoleMode();
	}
	
	/** 设置 nFore 蓝牙自动连接机制条件和时间 **/
	public void setBtAutoConnect(int condition, int period) throws RemoteException {
	    Log.v(TAG,"setBtAutoConnect() condition: " + condition + " period: " + period);
	    NforeBtBaseJar.setBtAutoConnect(condition, period);
	
	}
	
	/** 获得 nFore 蓝牙自动连接条件 **/
	public int getBtAutoConnectCondition() throws RemoteException {
	    Log.v(TAG,"getBtAutoConnectCondition()");
	    return NforeBtBaseJar.getBtAutoConnectCondition();
	}
	
	/** 获得 nFore 蓝牙自动连接周期的设置 **/
	public int getBtAutoConnectPeriod() throws RemoteException {
	    Log.v(TAG,"getBtAutoConnectPeriod()");
	    return NforeBtBaseJar.getBtAutoConnectPeriod();
	}
	
	/** 获得 nFore 蓝牙自动连接的状态 **/
	public int getBtAutoConnectState() throws RemoteException {
	    Log.v(TAG,"getBtAutoConnectState()");
	    return NforeBtBaseJar.getBtAutoConnectState();
	}
	
	/** 获取蓝牙自动连接的地址 **/
	public String getBtAutoConnectingAddress() throws RemoteException {
	    Log.v(TAG,"getBtAutoConnectingAddress()");
	    return NforeBtBaseJar.getBtAutoConnectingAddress();
	}
	
	/** 获取远程设备的当前hfp连接状态 **/
	public int getHfpConnectionState() throws RemoteException {
	    Log.v(TAG,"getHfpConnectionState()");
	    return NforeBtBaseJar.getHfpConnectionState();
	}
	
	/** 检查本地设备是否与远程设备连接hfp协议 **/
	public boolean isHfpConnected() throws RemoteException {
	    Log.v(TAG,"isHfpConnected()");
	    return NforeBtBaseJar.isHfpConnected(NforeBtBaseJar.getBtMainDevices());
	}
	
	/** 获取 HFP 连接远程设备的蓝牙硬件地址 **/
	public String getHfpConnectedAddress() throws RemoteException {
	    Log.v(TAG,"getHfpConnectedAddress()");
	    return NforeBtBaseJar.getHfpConnectedAddress();
	}
	
	/** 获取当前 HFP 连接远程设备的音频状态 **/
	public int getHfpAudioConnectionState() throws RemoteException {
	    Log.v(TAG,"getHfpAudioConnectionState()");
	    return NforeBtBaseJar.getHfpAudioConnectionState();
	}
	
	/** 请求将 HFP 连接到远程设备 **/
	public boolean reqHfpConnect(String address) throws RemoteException {
	    Log.v(TAG,"reqHfpConnect() " + address);
	    return NforeBtBaseJar.reqHfpConnect(address);
	}
	
	/** 请求将 HFP 断开到远程设备 **/
	public boolean reqHfpDisconnect(String address) throws RemoteException {
	    Log.v(TAG,"reqHfpDisconnect() " + address);
	    return NforeBtBaseJar.reqHfpDisconnect(address);
	}
	
	/** 获取 HFP 连接远程设备的信号强度 **/
	public int getHfpRemoteSignalStrength() throws RemoteException {
	    Log.v(TAG,"getHfpRemoteSignalStrength()");
	    return NforeBtBaseJar.getHfpRemoteSignalStrength();
	}
	
	/** 获取 HFP 连接远程设备的通话列表 **/
	public List<NfHfpClientCall> getHfpCallList() throws RemoteException {
	    Log.v(TAG,"getHfpCallList()");
	    return NforeBtBaseJar.getHfpCallList();
	}
	
	/** 检查 HFP 连接的远程设备是否在漫游 **/
	public boolean isHfpRemoteOnRoaming() throws RemoteException {
	    Log.v(TAG,"isHfpRemoteOnRoaming()");
	    return NforeBtBaseJar.isHfpRemoteOnRoaming();
	}
	
	/** 获取 HFP 连接远程设备的电池电量指示 **/
	public int getHfpRemoteBatteryIndicator() throws RemoteException {
	    Log.v(TAG,"getHfpRemoteBatteryIndicator()");
	    return NforeBtBaseJar.getHfpRemoteBatteryIndicator();
	}
	
	/** 检查 HFP 连接远程设备是否有电信服务 **/
	public boolean isHfpRemoteTelecomServiceOn() throws RemoteException {
	    Log.v(TAG,"isHfpRemoteTelecomServiceOn()");
	    return NforeBtBaseJar.isHfpRemoteTelecomServiceOn();
	}
	
	/** 检查 HFP 连接远程设备的语音拨号是否被激活 **/
	public boolean isHfpRemoteVoiceDialOn() throws RemoteException {
	    Log.v(TAG,"isHfpRemoteVoiceDialOn()");
	    return NforeBtBaseJar.isHfpRemoteVoiceDialOn();
	}
	
	/** 请求 HFP 连接远程设备拨电话号码 **/
	public boolean reqHfpDialCall(String number) throws RemoteException {
	    Log.v(TAG,"reqHfpDialCall() number: " + number);
	    return NforeBtBaseJar.reqHfpDialCall(number);
	}
	
	/** 请求 HFP 连接的远程设备重新拨号最后一个发出的呼叫 **/
	public boolean reqHfpReDial() throws RemoteException {
	    Log.v(TAG,"reqHfpReDial()");
	    return NforeBtBaseJar.reqHfpReDial();
	}
	
	/** 请求 HFP 连接远程设备进行Memory的拨号 **/
	public boolean reqHfpMemoryDial(String index) throws RemoteException {
	    Log.v(TAG,"reqHfpMemoryDial() index: " + index);
	    return NforeBtBaseJar.reqHfpMemoryDial(index);
	}
	
	/** 请求 HFP 连接远程设备接听来电 **/
	public boolean reqHfpAnswerCall(int flag) throws RemoteException {
	    Log.v(TAG,"reqHfpAnswerCall() " + flag);
	    return NforeBtBaseJar.reqHfpAnswerCall(NforeBtBaseJar.getBtMainDevices(),flag);
	}
	
	/** 请求 HFP 连接远程设备拒绝来电 **/
	public boolean reqHfpRejectIncomingCall() throws RemoteException {
	    Log.v(TAG,"reqHfpRejectIncomingCall()");
	    return NforeBtBaseJar.reqHfpRejectIncomingCall(NforeBtBaseJar.getBtMainDevices());
	}
	
	/** 请求 HFP 连接远程设备终止正在进行的电话 **/
	public boolean reqHfpTerminateCurrentCall() throws RemoteException {
	    Log.v(TAG,"reqHfpTerminateCurrentCall()");
	    return NforeBtBaseJar.reqHfpTerminateCurrentCall(NforeBtBaseJar.getBtMainDevices());
	}
	
	/** 请求 HFP 连接远程设备发送 DTMF **/
	public boolean reqHfpSendDtmf(String number) throws RemoteException {
	    Log.v(TAG,"reqHfpSendDtmf() number: " + number);
	    return NforeBtBaseJar.reqHfpSendDtmf(NforeBtBaseJar.getBtMainDevices(),number);
	}
	
	/** 请求 HFP 连接远程设备将音频传输到车机 **/
	public boolean reqHfpAudioTransferToCarkit() throws RemoteException {
	    Log.v(TAG,"reqHfpAudioTransfer()");
	    return NforeBtBaseJar.reqHfpAudioTransferToCarkit(NforeBtBaseJar.getBtMainDevices());
	}
	
	/** 请求 HFP 连接远程设备将音频传输到手机 **/
	public boolean reqHfpAudioTransferToPhone() throws RemoteException {
	    Log.v(TAG,"reqHfpAudioTransferToPhone()");
	    return NforeBtBaseJar.reqHfpAudioTransferToPhone(NforeBtBaseJar.getBtMainDevices());
	}
	
	/** 获取 HFP 连接远程设备的电信运营商 **/
	public String getHfpRemoteNetworkOperator() throws RemoteException {
	    Log.v(TAG,"reqHfpRemoteNetworkOperator()");
	    return NforeBtBaseJar.getHfpRemoteNetworkOperator();
	}
	
	/** 获取 HFP 连接远程设备的用户手机号码 **/
	public String getHfpRemoteSubscriberNumber() throws RemoteException {
	    Log.v(TAG,"getHfpRemoteSubscriberNumber()");
	    return NforeBtBaseJar.getHfpRemoteSubscriberNumber();
	}
	
	/** 请求 HFP 连接远程设备进行语音拨号 **/
	public boolean reqHfpVoiceDial(boolean enable) throws RemoteException {
	    Log.v(TAG,"reqHfpVoiceDial() enable: " + enable);
	    return NforeBtBaseJar.reqHfpVoiceDial(enable);
	}
	
	/** 停止发送 HFP 流数据到音频轨道 **/
	public void pauseHfpRender() throws RemoteException {
	    Log.v(TAG,"pauseHfpRender()");
	    NforeBtBaseJar.pauseHfpRender();
	}
	
	/** 开始将 HFP 流数据发送到音频轨道 **/
	public void startHfpRender() throws RemoteException {
	    Log.v(TAG,"startHfpRender()");
	    NforeBtBaseJar.startHfpRender();
	}
	
	/** 检查麦克风是否静音 **/
	public boolean isHfpMicMute() throws RemoteException {
	    Log.v(TAG,"isHfpMicMute()");
	    return NforeBtBaseJar.isHfpMicMute();
	}
	
	/** 在呼叫过程中请求 HFP 静音麦克风 **/
	public void muteHfpMic(boolean mute) throws RemoteException {
	    Log.v(TAG,"muteHfpMic() " + mute);
	    NforeBtBaseJar.muteHfpMic(mute);
	}
	
	/** 检查远程设备是否支持播放来电铃声 **/
	public boolean isHfpInBandRingtoneSupport() throws RemoteException {
	    Log.v(TAG,"isHfpInbandRingtoneSupport()");
	    return NforeBtBaseJar.isHfpInBandRingtoneSupport();
	}
	
	/** 获取远程设备的当前pbap连接状态 **/
	public int getPbapConnectionState() throws RemoteException {
	    Log.v(TAG,"getPbapConnectionState()");
	    return NforeBtBaseJar.getPbapConnectionState();
	}
	
	/** 检查本地设备是否从远程设备正在下载电话簿 **/
	public boolean isPbapDownloading() throws RemoteException {
	    Log.v(TAG,"isPbapDownloading()");
	    return NforeBtBaseJar.isPbapDownloading();
	}
	
	/** 获取 PBAP 下载远程设备的蓝牙硬件地址 **/
	public String getPbapDownloadingAddress() throws RemoteException {
	    Log.v(TAG,"getPbapDownloadingAddress()");
	    return NforeBtBaseJar.getPbapDownloadingAddress();
	}
	
	/** 
	 * 请求从远程设备上下载带 vCard 的电话簿,并下载完成后保存到对应的数据库，调用者可通过监听onPbapStateChanged(int sycnState)回调
	 * 当sycnState＝BluetoothHelper.BT_SYNC_COMPLETE_CONTACT时，调用getContactsListForDB即可获取到联系人列表
	 * 当sycnState＝BluetoothHelper.BT_SYNC_COMPLETE_CALLLOGS时，调用getCallLogsListForDB即可获取到通话记录列表
	 */
	public boolean reqPbapDownload(String address, int storage, int property)
	        throws RemoteException {
	    Log.v(TAG,"reqPbapDownload() " + address + " (" + storage + ")");
	    return NforeBtBaseJar.reqPbapDownload(address, storage, property);
	}
	
	/** 请求中断正在进行的远程设备下载 **/
	public boolean reqPbapDownloadInterrupt(String address) throws RemoteException {
	    Log.v(TAG,"reqPbapDownloadInterrupt() " + address);
	    return NforeBtBaseJar.reqPbapDownloadInterrupt(address);
	}
	
	/** 设置 PBAP 下载通知频率。将在 ServiceManager 重启时设置默认值。默认值为 0,意味着不要回调下载通知 **/
	public boolean setPbapDownloadNotify(int frequency) throws RemoteException {
	    Log.v(TAG,"setPbapDownloadNotify() " + frequency);
	    return NforeBtBaseJar.setPbapDownloadNotify(frequency);
	}
	
	/** 请求 Avrcp 更新歌曲状态 **/
	public void reqAvrcpUpdateSongStatus() throws RemoteException {
	    Log.v(TAG,"reqAvrcpUpdateSongStatus()");
	    NforeBtBaseJar.reqAvrcpUpdateSongStatus();
	}
	
	/** 请求将 SPP 连接到远程设备 **/
	public boolean reqSppConnect(String address) throws RemoteException {
	    Log.v(TAG,"reqSppConnect() " + address);
	    return NforeBtBaseJar.reqSppConnect(address);
	}
	
	/** 请求断开连接到远程设备的连接的 SPP 连接 **/
	public boolean reqSppDisconnect(String address) throws RemoteException {
	    Log.v(TAG,"reqSppDisconnect() " + address);
	    return NforeBtBaseJar.reqSppDisconnect(address);
	}
	
	/** 请求远程 SPP 设备的硬件蓝牙地址 **/
	public void reqSppConnectedDeviceAddressList() throws RemoteException {
	    Log.v(TAG,"reqSppConnectedDeviceAddressList()");
	    NforeBtBaseJar.reqSppConnectedDeviceAddressList();
	}
	
	/** 检查本地设备是否 SPP 连接到远程设备 **/
	public boolean isSppConnected(String address) throws RemoteException {
	    Log.v(TAG,"isSppConnected() " + address);
	    return NforeBtBaseJar.isSppConnected(address);
	}
	
	/** 请求将给定数据发送到远程 SPP 设备。数据大小不应该大于 512 字节 **/
	public void reqSppSendData(String address, byte[] sppData) throws RemoteException {
	    Log.v(TAG,"reqSppSendData() " + address);
	    NforeBtBaseJar.reqSppSendData(address, sppData);
	}
	
	/** 设置默认配对码，默认值为0000 **/
	public void setDefaultPinCode(String pinCode)
	        throws RemoteException {
	    Log.v(TAG, "setDefaultPinCode() pinCode: " + pinCode);
	    NforeBtBaseJar.setDefaultPinCode(pinCode);
	}
	
	/** 获取默认配对码，默认值为0000 **/
	public String getDefaultPinCode() throws RemoteException {
	    Log.v(TAG, "getDefaultPinCode()");
	    return NforeBtBaseJar.getDefaultPinCode();
	}
	
	/** 设置蓝牙自动配对请求，默认值为false关闭 **/
	public void setBtAutoAcceptPairingRequest(boolean auto) throws RemoteException {
	    Log.v(TAG, "setBtAutoAcceptPairingRequest()");
	    NforeBtBaseJar.setBtAutoAcceptPairingRequest(auto);
	}
	
	/** 获取蓝牙自动配对请求开关 **/
	public boolean isBtAutoAcceptPairingRequest() throws RemoteException {
	    Log.v(TAG, "isBtAutoAcceptPairingRequest()");
	    return NforeBtBaseJar.isBtAutoAcceptPairingRequest();
	}
	
	//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	/** 设置蓝牙自动下载开关 **/
	public void setBtAutoDownEnable(boolean isBtAutoDownEnable)
			throws RemoteException {
		Log.v(TAG, "setBtAutoDownEnable()");
		NforeBtBaseJar.setBtAutoDownEnable(isBtAutoDownEnable,0);
	}
	
	/** 获取蓝牙自动下载状态 **/
	public boolean getBtAutoDownState() throws RemoteException {
		Log.v(TAG, "getBtAutoDownState()");
		return NforeBtBaseJar.getBtAutoDownState(0);
	}
	
	/** 设置蓝牙自动接听开关 **/
	public void setBtAutoAnswerEnable(boolean isBtAutoAnswerEnable)
			throws RemoteException {
		Log.v(TAG, "setBtAutoAnswerEnable()");
		NforeBtBaseJar.setBtAutoAnswerEnable(isBtAutoAnswerEnable,5);
	}
	
	/** 获取蓝牙自动接听状态 **/
	public boolean getBtAutoAnswerState() throws RemoteException {
		Log.v(TAG, "getBtAutoAnswerState()");
		return NforeBtBaseJar.getBtAutoAnswerState();
	}
	
	/** 设置蓝牙接听类型 **/
	public void setBtAnswerTypeEnable(boolean isBtAnswerTypeEnable)
			throws RemoteException {
		Log.v(TAG, "setBtAnswerTypeEnable()");
		NforeBtBaseJar.setBtAnswerTypeEnable(isBtAnswerTypeEnable);
	}
	
	/** 获取蓝牙接听类型 **/
	public boolean getBtAnswerType() throws RemoteException {
		Log.v(TAG, "getBtAnswerType()");
		return NforeBtBaseJar.getBtAnswerType();
	}
	
	/** 储存蓝牙设备连接地址 **/
	public void setBtDevConnAddr(String btDevConnAddr)
			throws RemoteException {
		Log.v(TAG, "setBtDevConnAddr("+btDevConnAddr+")");
		NforeBtBaseJar.setBtDevConnAddr(btDevConnAddr);
	}
	
	/** 获取最后一次储存的已连接的蓝牙设备硬件地址 **/
	public String getBtDevConnAddr() throws RemoteException {
		Log.v(TAG, "getBtDevConnAddr()");
		return NforeBtBaseJar.getBtDevConnAddr();
	}
	
	/** 从serviceDB获取联系人列表 ，如果失败返回null **/
	public List<Contacts> getContactsListForDB() throws RemoteException {
		Log.v(TAG, "getContactsListForDB()");
		return NforeBtBaseJar.getContactsListForDB();
	}
	
	/** 储存ui传过来的通话记录到serviceDB，当蓝牙通话中时，调用此接口可储存单条通话记录到通话记录数据表，通话结束后，无需再进行下载通话记录，直接调用getCallLogsListForDB即可 **/
//	public void setUiCallLogsToDB(String name, String number, int type, String time)
//			throws RemoteException {
//		Log.v(TAG, "setUiCallLogsToDB()");
//		if(!isSetUiCallLogsToDB){
//			isSetUiCallLogsToDB = true;
////			StaticDefConf.isCallLogState = true;
//			NforeBtBaseJar.setUiCallLogsToDB(name, number, type, time);
//		}
//	}
	
	/** 从serviceDB获取通话记录列表，如果失败返回null **/
	public List<CallLogs> getCallLogsListForDB(int opt) throws RemoteException {
		Log.v(TAG, "getCallLogsListForDB()");
//		StaticDefConf.isCallLogState = false;
		return NforeBtBaseJar.getCallLogsListForDB(opt);
	}
	
	/** 删除serviceDB数据库 **/
	public boolean deleteDatabase() throws RemoteException {
		Log.v(TAG, "deleteDatabase()");
		return NforeBtBaseJar.deleteDatabase();
	}
	
	/** 清空serviceDB数据库 **/
	public boolean cleanTable(int options) throws RemoteException {
		Log.v(TAG, "cleanTable() "+ options);
		if(options == 0){
			Log.i(TAG,"清空ui缓存数据");
			contactsList = null;
			callLogsList = null;
		}
		return NforeBtBaseJar.cleanTable(options);
	}
	
	/** 从serviceDB通过联系人号码获取名称，需要serviceDB有已储存的数据，如果查不到，返回＂未知号码＂ **/
	public String getCallName(String number) throws RemoteException {
		Log.v(TAG, "getCallName() "+ number);
		String name = "";
		switch (number) {
		case "10086":
			name = "中国移动";
			break;
		case "10010":
			name = "中国联通";
			break;
		case "10000":
			name = "中国电信";
			break;

		default:
			name = NforeBtBaseJar.getCallName(number);
			break;
		}
		return name;
	}

	@Override
	public void retAvrcp13CapabilitiesSupportEvent(byte[] eventIds) {
		Log.i(TAG, "retAvrcp13CapabilitiesSupportEvent");
		if(mMusicChangeListener == null){
			return;
		}
		mMusicChangeListener.retAvrcp13CapabilitiesSupportEvent(eventIds);
	}

	@Override
	public void retAvrcp13PlayerSettingAttributesList(byte[] attributeIds) {
		Log.i(TAG, "retAvrcp13PlayerSettingAttributesList");
		if(mMusicChangeListener == null){
			return;
		}
		mMusicChangeListener.retAvrcp13PlayerSettingAttributesList(attributeIds);
	}

	@Override
	public void retAvrcp13PlayerSettingValuesList(byte b, byte[] bytes) {

	}

	@Override
	public void retAvrcp13PlayerSettingCurrentValues(byte[] bytes, byte[] bytes1) {

	}

	@Override
	public void retAvrcp13SetPlayerSettingValueSuccess() {

	}

	@Override
	public void retAvrcp13ElementAttributesPlaying(int[] metadataAtrributeIds,
			String[] texts) {
		Log.i(TAG, "retAvrcp13ElementAttributesPlaying");
		if(mMusicChangeListener == null){
			return;
		}
		mMusicChangeListener.retAvrcp13ElementAttributesPlaying(metadataAtrributeIds, texts);
	}

	@Override
	public void retAvrcp13PlayStatus(long songLen, long songPos, byte statusId) {
		Log.i(TAG, "retAvrcp13PlayStatus");
		if(mMusicChangeListener == null){
			return;
		}
		mMusicChangeListener.retAvrcp13PlayStatus(songLen, songPos, statusId);
	}

	@Override
	public void onAvrcp13RegisterEventWatcherSuccess(byte b) {

	}

	@Override
	public void onAvrcp13RegisterEventWatcherFail(byte b) {

	}

	@Override
	public void onAvrcp13EventPlaybackStatusChanged(byte statusId) {
		Log.i(TAG, "onAvrcp13EventPlaybackStatusChanged");
		if(mMusicChangeListener == null){
			return;
		}
		mMusicChangeListener.onAvrcp13EventPlaybackStatusChanged(statusId);
	}

	@Override
	public void onAvrcp13EventTrackChanged(long elementId) {
		Log.i(TAG, "onAvrcp13EventTrackChanged");
		if(mMusicChangeListener == null){
			return;
		}
		mMusicChangeListener.onAvrcp13EventTrackChanged(elementId);
	}

	@Override
	public void onAvrcp13EventTrackReachedEnd() {

	}

	@Override
	public void onAvrcp13EventTrackReachedStart() {

	}

	@Override
	public void onAvrcp13EventPlaybackPosChanged(long l) {

	}

	@Override
	public void onAvrcp13EventBatteryStatusChanged(byte b) {

	}

	@Override
	public void onAvrcp13EventSystemStatusChanged(byte b) {

	}

	@Override
	public void onAvrcp13EventPlayerSettingChanged(byte[] attributeIds,
			byte[] valueIds) {
		Log.i(TAG, "onAvrcp13EventPlayerSettingChanged");
		if(mMusicChangeListener == null){
			return;
		}
		mMusicChangeListener.onAvrcp13EventPlayerSettingChanged(attributeIds, valueIds);
	}

	@Override
	public void onAvrcp14EventNowPlayingContentChanged() {

	}

	@Override
	public void onAvrcp14EventAvailablePlayerChanged() {

	}

	@Override
	public void onAvrcp14EventAddressedPlayerChanged(int i, int i1) {

	}

	@Override
	public void onAvrcp14EventUidsChanged(int i) {

	}

	@Override
	public void onAvrcp14EventVolumeChanged(byte b) {

	}

	@Override
	public void retAvrcp14SetAddressedPlayerSuccess() {

	}

	@Override
	public void retAvrcp14SetBrowsedPlayerSuccess(String[] strings, int i, long l) {

	}

	@Override
	public void retAvrcp14FolderItems(int i, long l) {

	}

	@Override
	public void retAvrcp14MediaItems(int i, long l) {

	}

	@Override
	public void retAvrcp14ChangePathSuccess(long l) {

	}

	@Override
	public void retAvrcp14ItemAttributes(int[] ints, String[] strings) {

	}

	@Override
	public void retAvrcp14PlaySelectedItemSuccess() {

	}

	@Override
	public void retAvrcp14SearchResult(int i, long l) {

	}

	@Override
	public void retAvrcp14AddToNowPlayingSuccess() {

	}

	@Override
	public void retAvrcp14SetAbsoluteVolumeSuccess(byte b) {

	}

	@Override
	public void onAvrcpErrorResponse(int i, int i1, byte b) {

	}

	@Override
	public void retAvrcpUpdateSongStatus(String artist, String album,
                                         String title) {
		Log.i(TAG, "retAvrcpUpdateSongStatus");
		if(mMusicChangeListener == null){
			return;
		}
		mMusicChangeListener.retAvrcpUpdateSongStatus(artist, album, title);
	}

	@Override
	public void onHfpCallChanged(String address, NfHfpClientCall call) {
		Log.i(TAG, "onHfpCallChanged"+call.toString());
		if(getCallingActivity() != null){
			getCallingActivity().getCallList();
		}

		if(getWindowDialog() != null){
			windowChange(call);
		}

		if(mPhoneChangeListener == null){
			return;
		}
		if(call.getState() == NfHfpClientCall.CALL_STATE_TERMINATED){
			isSetUiCallLogsToDB = false;
		}
		mPhoneChangeListener.onHfpCallChanged(address, call);
	}

	@Override
	public void onHfpCallingTimeChanged(String time) {
		Log.i(TAG, "onHfpCallingTimeChanged");
		if(getCallingActivity() != null){
			Log.d(TAG, "onHfpCallingTimeChanged: time==" + time);
			Message message = getCallingActivity().myHandler.obtainMessage();
			message.what = 0x05;
			Bundle bundle = new Bundle();
			bundle.putString("time", time);
			message.setData(bundle);
			getCallingActivity().myHandler.sendMessage(message);
		}

		if(getWindowDialog() != null){
			Log.d(TAG, "onHfpCallingTimeChanged: time"+time );
			Message message = getWindowDialog().mHandler.obtainMessage();
			message.what = 0x02;
			Bundle bundle = new Bundle();
			bundle.putString("time",time);
			message.setData(bundle);
			getWindowDialog().mHandler.sendMessage(message);
		}

		if(mPhoneChangeListener == null){
			return;
		}
		mPhoneChangeListener.onHfpCallingTimeChanged(time);
	}

	@Override
	public void onPbapStateChanged(int sycnState) {
		Log.i(TAG, "onPbapStateChanged sycnState---"+sycnState);
		if(mPhoneChangeListener == null){
			return;
		}
		mPhoneChangeListener.onPbapStateChanged(sycnState);
	}

	@Override
	public void onAdapterStateChanged(int i, int i1) {

	}

	@Override
	public void onEnableChanged(boolean isEnable) {
		Log.i(TAG, "onEnableChanged");
		if(mSettingChangeListener == null){
			return;
		}
//		StaticDefConf.defBtEnable = isEnable;
		mSettingChangeListener.onEnableChanged(isEnable);
	}

	@Override
	public void onConnectedChanged(String address, int connectState) {
		Log.i(TAG, "onConnectedChanged");
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.onConnectedChanged(address, connectState);
	}

	@Override
	public void onHfpStateChanged(String address, int connectState) {
		Log.i(TAG, "onHfpStateChanged");
		if(getCallingActivity() != null){
			if (connectState == NforeBtBaseJar.CONNECT_SUCCESSED)   //连接状态
			{
				Log.i(TAG, "-----------------onHfpStateChanged-------------isConnected--------" + true);

			} else if (connectState == NforeBtBaseJar.CONNECT_DISCONNECT) {
				Log.i(TAG, "-----------------onHfpStateChanged--------------isConnected-------" + false);
				getCallingActivity().finish();
			}

		}
		if(getWindowDialog() != null){
			if(connectState == NforeBtBaseJar.CONNECT_DISCONNECT){
				Log.d(TAG, "onHfpStateChanged: 蓝牙断开");
				getWindowDialog().initInstance();

			}
		}

		if(mSettingChangeListener == null){
			return;
		}
//		if(!isConnected){
////			StaticDefConf.isDownContact = false;
////			StaticDefConf.isDownCallLogs = false;
//		}
		mSettingChangeListener.onHfpStateChanged(address, connectState);
	}

	@Override
	public void onHfpAudioStateChanged(String address, int prevState,
                                       int newState) {
		Log.i(TAG, "onHfpAudioStateChanged");
		if(getCallingActivity() != null){
			Message msg = new Message();
			msg.what = 0;
			msg.arg1 = newState;
			getCallingActivity().myHandler.sendMessage(msg);
		}
		if(getWindowDialog() != null){
			Message msg = new Message();
			msg.what = 0;
			msg.arg1 = newState;
			getWindowDialog().mHandler.sendMessage(msg);
		}
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.onHfpAudioStateChanged(address, prevState, newState);
	}
	
	@Override
	public void onAvrcpStateChanged(String address, int connectState) {
		Log.i(TAG, "onAvrcpStateChanged");
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.onAvrcpStateChanged(address, connectState);
	}

	@Override
	public void onA2dpStateChanged(String address, int connectState) {
		Log.i(TAG, "onA2dpStateChanged");
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.onA2dpStateChanged(address, connectState);
	}

	@Override
	public void onAdapterDiscoveryStarted() {
		Log.i(TAG, "onAdapterDiscoveryStarted");
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.onAdapterDiscoveryStarted();
	}

	@Override
	public void onAdapterDiscoveryFinished() {
		Log.i(TAG, "onAdapterDiscoveryFinished");
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.onAdapterDiscoveryFinished();
	}

	@Override
	public void retPairedDevices(int elements, String[] address, String[] name,
                                 int[] supportProfile) {
		Log.i(TAG, "retPairedDevices");
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.retPairedDevices(elements, address, name, supportProfile);
	}

	@Override
	public void onDeviceFound(String address, String name) {
		Log.i(TAG, "onDeviceFound");
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.onDeviceFound(address, name);
	}

	@Override
	public void onDeviceBondStateChanged(String address, String name,
                                         int newState) {
		Log.i(TAG, "onDeviceBondStateChanged");
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.onDeviceBondStateChanged(address, name, newState);
	}

	@Override
	public void onLocalAdapterNameChanged(String name) {
		Log.i(TAG, "onLocalAdapterNameChanged");
		if(mSettingChangeListener == null){
			return;
		}
		mSettingChangeListener.onLocalAdapterNameChanged(name);
	}

	@Override
	public void onPairStateChanged(String s, String s1, int i, int i1) {

	}

	@Override
	public void onMainDevicesChanged(String s, String s1) {

	}

	@Override
	public void onServiceConnectedChanged(boolean isConnected) {
		Log.i(TAG, "onServiceConnectedChanged");
		if(mServiceConnectedListener == null){
			return;
		}
		if(isConnected){
//			try {
//				StaticDefConf.defBtEnable = NforeBtBaseJar.isBtEnabled();
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		mServiceConnectedListener.onServiceConnectedChanged(isConnected);
	}

	@Override
	public void onSppServiceReady() {
		Log.i(TAG, "onSppServiceReady");
		if(mSppChangeListener == null){
			return;
		}
		mSppChangeListener.onSppServiceReady();
	}

	@Override
	public void onSppStateChanged(String address, String deviceName,
                                  int prevState, int newState) {
		Log.i(TAG, "onSppStateChanged");
		if(mSppChangeListener == null){
			return;
		}
		mSppChangeListener.onSppStateChanged(address, deviceName, prevState, newState);		
	}

	@Override
	public void onSppErrorResponse(String address, int errorCode) {
		Log.i(TAG, "onSppErrorResponse");
		if(mSppChangeListener == null){
			return;
		}
		mSppChangeListener.onSppErrorResponse(address, errorCode);		
	}

	@Override
	public void retSppConnectedDeviceAddressList(int totalNum,
                                                 String[] addressList, String[] nameList) {
		Log.i(TAG, "retSppConnectedDeviceAddressList");
		if(mSppChangeListener == null){
			return;
		}
		mSppChangeListener.retSppConnectedDeviceAddressList(totalNum, addressList, nameList);		
	}

	@Override
	public void onSppDataReceived(String address, byte[] receivedData) {
		Log.i(TAG, "onSppDataReceived");
		if(mSppChangeListener == null){
			return;
		}
		mSppChangeListener.onSppDataReceived(address, receivedData);		
	}

	@Override
	public void onSppSendData(String address, int length) {
		Log.i(TAG, "onSppSendData");
		if(mSppChangeListener == null){
			return;
		}
		mSppChangeListener.onSppSendData(address, length);		
	}

	@Override
	public void onSppAppleIapAuthenticationRequest(String address) {
		Log.i(TAG, "onSppAppleIapAuthenticationRequest");
		if(mSppChangeListener == null){
			return;
		}
		mSppChangeListener.onSppAppleIapAuthenticationRequest(address);		
	}

	private void windowChange(NfHfpClientCall call){
		WindowDialog windowDialog = getWindowDialog();
		if (call.isOutgoing() == false) {
//来电
			if (call.getState() == NfHfpClientCall.CALL_STATE_INCOMING) {   //来电
				Log.d(TAG, "------------CALL_STATE_INCOMING-----来电---------------");
				windowDialog.mHandler.sendEmptyMessage(0x03);
			} else if (call.getState() == NfHfpClientCall.CALL_STATE_ACTIVE) {  //通话中
				Log.d(TAG, "onHfpCallChanged:通话中: ");
				windowDialog.mHandler.sendEmptyMessage(0x03);
			} else if (call.getState() == NfHfpClientCall.CALL_STATE_TERMINATED) {  //结束通话
				Log.d(TAG, "onHfpCallChanged:结束通话: ");
				List<NfHfpClientCall> hfpCallList = null;
				try {
					hfpCallList = getHfpCallList();
					if (hfpCallList.size() == 0) {
						Log.d(TAG, "onHfpCallChanged:dismiss() " );
//                        dismiss();
//                        instance = null;
						windowDialog.initInstance();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}


			} else if (call.getState() == NfHfpClientCall.CALL_STATE_DIALING) {     //去电
				Log.d(TAG, "onHfpCallChanged:去电电话1: ");
			}
		} else {
			//去电
			if (call.getState() == NfHfpClientCall.CALL_STATE_DIALING ||
					call.getState() == NfHfpClientCall.CALL_STATE_ALERTING) {
				Log.d(TAG, "onHfpCallChanged:去电电话2: ");
			} else if (call.getState() == NfHfpClientCall.CALL_STATE_ACTIVE) {  //通话中
				Log.d(TAG, "onHfpCallChanged:通话中: ");
				windowDialog.mHandler.sendEmptyMessage(0x03);
			} else if (call.getState() == NfHfpClientCall.CALL_STATE_TERMINATED) {
				Log.d(TAG, "onHfpCallChanged:  结束通话: ");
				List<NfHfpClientCall> hfpCallList = null;
				try {
					hfpCallList = getHfpCallList();
					if (hfpCallList.size() == 0) {
						Log.d(TAG, "onHfpCallChanged:dismiss() " );
//                        dismiss();
//                        instance = null;
						windowDialog.initInstance();

					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}
