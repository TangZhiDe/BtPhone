package com.nforetek.bt.phone;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adayo.adayosource.AdayoSource;
import com.adayo.app.base.BaseActivity;
import com.adayo.app.utils.LogUtils;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.proxy.sourcemngproxy.Control.SrcMngSwitchProxy;
import com.adayo.proxy.sourcemngproxy.ISourceActionCallBack;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.base.jar.NforeBtBaseJar;
import com.nforetek.bt.bean.Contacts;
import com.nforetek.bt.phone.adapter.SortAdapter;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.phone.pvcontract.IBtContract;
import com.nforetek.bt.phone.service_boardcast.CallService;
import com.nforetek.bt.phone.tools.BtUtils;
import com.nforetek.bt.phone.tools.CallInterfaceManagement;
import com.nforetek.bt.phone.tools.ConfirmDialog;
import com.nforetek.bt.phone.tools.MyLinearLayoutManager;
import com.nforetek.bt.phone.tools.WindowDialog;
import com.nforetek.bt.res.NfDef;


/**
 * @author tzd
 */
public class BtPhoneMainActivity extends BaseActivity implements View.OnClickListener, View.OnLongClickListener,
        BtPresenter.UiBluetoothServiceConnectedListener,
        BtPresenter.UiBluetoothPhoneChangeListerer,
        BtPresenter.UiBluetoothSettingChangeListerer, IBtContract {

    private static final String TAG = BtPhoneMainActivity.class.getCanonicalName();
    private BtPresenter mBPresenter;
    private ContactFragment contactFragment;
    private RecordsFragment recordsFragment;
    private RelativeLayout one, two, three, four, five, six, seven, eight, nine, zero, asterisk, pound;
    private ImageView delete;
    private Button keyboard_call;
    private EditText phone_num;
    private boolean phoneTextCursor = false;
    private String input_number = "";
    private ImageView main_disconnect;
    private LinearLayout main_fragment;
    private LinearLayout main_fit;
    private RecyclerView recycleview;
    private SortAdapter callRecyclerAdapter;
    private LinearLayout main_bt_connected;
    private RelativeLayout main_bt_disconnect;
    private TextView main_disconnect_toconnect;
    private TextView main_device_name;
    private String btRemoteDeviceName = "";
    private SrcMngSwitchProxy mProxy;
    private FrameLayout main_content;
    private RelativeLayout empty_loading;
    private ImageView ico_loading;
    private int mProperty = NfDef.PBAP_PROPERTY_MASK_FN |
            NfDef.PBAP_PROPERTY_MASK_N |
            NfDef.PBAP_PROPERTY_MASK_TEL |
            NfDef.PBAP_PROPERTY_MASK_VERSION |
            NfDef.PBAP_PROPERTY_MASK_ADR |
            NfDef.PBAP_PROPERTY_MASK_EMAIL |
            NfDef.PBAP_PROPERTY_MASK_PHOTO |
            NfDef.PBAP_PROPERTY_MASK_ORG;
    private RadioGroup main_tabs;
    private RadioButton tab_records;
    private RadioButton tab_contact;

    private boolean isPageShow = false;//搜索匹配联系人页面是否显示

    /**
     * 获取该 Activity 布局控件 ID。
     */
    @Override
    public int getLayout() {
        LogUtils.iL(TAG, "getLayout");
        return R.layout.activity_btphone_main;
    }

    /**
     * 初始化 Fragment,在此方法内,需要实例化所有装载在该 Activity 内的 Fragment,
     * 并装载在 BaseAcitivty 的 Fragment 容器内,装载 Fragment 的容器的 key 为 Fragment 的唯一
     * 识别码(int 类型数值),为开发人员自行定义,value 为 Fragment 实例。
     * 且需要注册Activity 与各 Fragment 的通讯接口 ICommunication.
     */
    @Override
    public void initView() {
        LogUtils.iL(TAG, "initView");
        contactFragment = ContactFragment.newInstance(null);
        recordsFragment = RecordsFragment.newInstance(null);
        setCommunications();
        loadFragmentSA();
        init();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: ");
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null) {
            Log.d(TAG, "onNewIntent: +++++++++++++++++++++++++++++++");
            turnPage(intent);
        }
    }

    /**
     * 跳转到页面
     *
     * @param intent
     */
    private void turnPage(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Map map = (HashMap) bundle.get("map");
            if (map != null) {
                String phoneValue = (String) map.get("phone");
                //1 : 通讯录界面; 2 : 通话记录界面
                if (!TextUtils.isEmpty(phoneValue)) {
                    Log.d(TAG, "phoneValue =" + phoneValue);
                    if ("1".equals(phoneValue)) {
                        tab_contact.setChecked(true);
                        showPage(2);
                    } else {
                        tab_records.setChecked(true);
                        showPage(1);
                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        disableShowInput(phone_num);
//		btConnect();
        isopen();
//        setCallView();
    }

    private void setCallView() {
        Log.d(TAG, "setCallView: 用户点击电话");
        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
        management.showCallInterface(BtPhoneMainActivity.this,CallInterfaceManagement.SHOW_TYPE_Activity);
    }

    //	private boolean isBTConnect = false;
    private void btConnect(boolean isBTConnect) {
        if (isBTConnect) {
            main_bt_connected.setVisibility(View.VISIBLE);
            main_bt_disconnect.setVisibility(View.GONE);
        } else {
            main_bt_connected.setVisibility(View.GONE);
            main_bt_disconnect.setVisibility(View.VISIBLE);
        }
    }


    private void init() {
//        Intent service = new Intent(this, CallService.class);
//        service.setPackage("com.nforetek.bt.phone");
//        startService(service);
        main_bt_connected = findViewById(R.id.main_bt_connected);
        main_bt_disconnect = findViewById(R.id.main_bt_disconnect);
        main_device_name = findViewById(R.id.main_device_name);
        main_disconnect_toconnect = findViewById(R.id.main_disconnect_toconnect);
        main_disconnect_toconnect.setOnClickListener(this);
        one = findViewById(R.id.number01);
        one.setOnClickListener(this);
        two = findViewById(R.id.number02);
        two.setOnClickListener(this);
        three = findViewById(R.id.number03);
        three.setOnClickListener(this);
        four = findViewById(R.id.number04);
        four.setOnClickListener(this);
        five = findViewById(R.id.number05);
        five.setOnClickListener(this);
        six = findViewById(R.id.number06);
        six.setOnClickListener(this);
        seven = findViewById(R.id.number07);
        seven.setOnClickListener(this);
        eight = findViewById(R.id.number08);
        eight.setOnClickListener(this);
        nine = findViewById(R.id.number09);
        nine.setOnClickListener(this);
        zero = findViewById(R.id.number00);
        zero.setOnClickListener(this);
        asterisk = findViewById(R.id.numberxinghao);
        asterisk.setOnClickListener(this);
        pound = findViewById(R.id.numberjinhao);
        pound.setOnClickListener(this);
        delete = findViewById(R.id.keyboard_delete);
        keyboard_call = findViewById(R.id.keyboard_call);
        delete.setOnClickListener(this);
        keyboard_call.setOnClickListener(this);
        delete.setOnLongClickListener(this);
        zero.setOnLongClickListener(this);
        asterisk.setOnLongClickListener(this);
        pound.setOnLongClickListener(this);
        main_disconnect = findViewById(R.id.main_disconnect);
        main_disconnect.setOnClickListener(this);
        phone_num = findViewById(R.id.editnum_phone);
        disableShowInput(phone_num);
        phone_num.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone_num.setCursorVisible(true);
                phoneTextCursor = true;

            }
        });
        phone_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (MyApplication.contactList.size() > 0) {
                    indexofNum(s.toString().trim());
                }
                if (s != null && s.length() > 0) {
                    if (!isPageShow) {
                        //输入第一个数字时显示页面
                        main_fragment.setVisibility(View.GONE);
                        main_fit.setVisibility(View.VISIBLE);
                        main_fit.setAnimation(BtUtils.makeInAnimation(BtPhoneMainActivity.this, false));
                        isPageShow = true;
                    }

                } else {
                    main_fragment.setVisibility(View.VISIBLE);
                    main_fit.setVisibility(View.GONE);
                    main_fit.setAnimation(BtUtils.makeOutAnimation(BtPhoneMainActivity.this, false));
                    phone_num.setCursorVisible(false);
                    isPageShow = false;
                    phoneTextCursor = false;
                }
            }
        });
        main_content = findViewById(R.id.main_content);
        empty_loading = findViewById(R.id.empty_loading);
        ico_loading = findViewById(R.id.empty_refresh_icon);
        main_fragment = findViewById(R.id.main_fragment);
        main_fit = findViewById(R.id.main_fit);
        recycleview = findViewById(R.id.main_recycler);

        MyLinearLayoutManager myLinearLayoutManager = new MyLinearLayoutManager(getMContext(), LinearLayoutManager.VERTICAL, false);
        recycleview.setLayoutManager(myLinearLayoutManager);
        recycleview.addItemDecoration(new SpaceItemDecoration(10));
        callRecyclerAdapter = new SortAdapter(this, callList);
        recycleview.setAdapter(callRecyclerAdapter);
        callRecyclerAdapter.setOnItemClickListener(new SortAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Contacts contacts = callList.get(position);
                try {
                    if (mBPresenter != null && mBPresenter.isHfpConnected()) {
                        String numberJSON = contacts.getNumberJSON();
                        List<String> list = JSON.parseArray(numberJSON, String.class);
                        if (list != null && list.size() > 0) {
                            Log.d(TAG, "onItemClick: num===" + list.get(0) + "--size==" + list.size());
                            mBPresenter.reqHfpDialCall(list.get(0));
                        }
//						Intent intent1 = new Intent();
//						intent1.setClass(getMContext(),CallingActivity.class);
//						intent1.putExtra("number",contacts.getNumber());
//						intent1.putExtra("name",contacts.getName());
//						intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						startActivity(intent1);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private List<Contacts> callList = new ArrayList<>();

    private void indexofNum(String num) {
        callList.clear();
        if (MyApplication.contactList.size() > 0) {
            for (int i = 0; i < MyApplication.contactList.size(); i++) {
                String numberJSON = MyApplication.contactList.get(i).getNumberJSON();
                List<String> list = JSON.parseArray(numberJSON, String.class);
                if (list != null && list.size() > 0) {
                    if (list.get(0).contains(num)) {
                        callList.add(MyApplication.contactList.get(i));
                    }
                }

            }
        }
        callRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void refresh() {
        if (mBPresenter == null) {
            Log.d(TAG, "onClick: mBPresenter===null");
            return;
        }
        //开始下载通话记录
        try {
            if (MyApplication.connectAddress == "") {
                MyApplication.connectAddress = mBPresenter.getHfpConnectedAddress();
            }
            Log.d(TAG, "onClick: 开始下载通讯录" + MyApplication.connectAddress);
            mBPresenter.reqPbapDownload(MyApplication.connectAddress, NfDef.PBAP_STORAGE_PHONE_MEMORY, mProperty);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0x00:
                    Bundle data = message.getData();
                    boolean isConnected = data.getBoolean("isConnected");
                    MyApplication.connectAddress = data.getString("address");
                    String name = data.getString("name");
                    Log.d(TAG, "handleMessage: btConnect==" + isConnected);
                    btConnect(isConnected);
                    if (isConnected) {
                        if (name.isEmpty())
                            name = "名称未设置";
                        main_device_name.setText(name);
                    }
                    break;
                case 0x01:
                    if (mBPresenter.getContactFragment() != null) {
                        mBPresenter.getContactFragment().getList();
                    }
                    if (mBPresenter.getRecordsFragment() != null) {
                        mBPresenter.getRecordsFragment().getList();
                    }
                    main_content.setVisibility(View.VISIBLE);
                    empty_loading.setVisibility(View.GONE);
                    stopRotate();
                    break;
                case 0x02:
                    MyApplication.recordsList.clear();
                    MyApplication.contactList.clear();
                    startRotate();//开启动画
                    Log.d(TAG, "handleMessage: 开始下载，开启动画");
                    break;
            }
            return false;
        }
    });

    /**
     * 开启动画
     */
    public void startRotate() {
        main_content.setVisibility(View.GONE);
        empty_loading.setVisibility(View.VISIBLE);
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.version_image_rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (operatingAnim != null) {
            ico_loading.startAnimation(operatingAnim);
        }
    }

    /**
     * 关闭动画
     */
    public void stopRotate() {
        main_content.setVisibility(View.VISIBLE);
        empty_loading.setVisibility(View.GONE);
        ico_loading.clearAnimation();
    }

    @Override
    public void onHfpCallChanged(String address, NfHfpClientCall call) {

    }

    @Override
    public void onHfpCallingTimeChanged(String time) {

    }

    @Override
    public void onPbapStateChanged(int sycnState) {
        Log.d(TAG, "onPbapStateChanged: sycnState===" + sycnState);
        switch (sycnState) {
            case NforeBtBaseJar.BT_SYNC_CONTACT:
                //开始同步
                myHandler.sendEmptyMessage(0x02);
                break;
            case NforeBtBaseJar.BT_SYNC_COMPLETE_CALLLOGS:
                //通话记录下载完成
                Log.d(TAG, "onPbapStateChanged: 下载完成");
                myHandler.sendEmptyMessage(0x01);
                break;
            case NforeBtBaseJar.BT_SYNC_COMPLETE_CONTACT:
                //通讯录下载完成
                try {
                    if (MyApplication.connectAddress == "") {
                        MyApplication.connectAddress = mBPresenter.getHfpConnectedAddress();
                    }

                    mBPresenter.reqPbapDownload(MyApplication.connectAddress, NfDef.PBAP_STORAGE_CALL_LOGS, mProperty);
                    Log.d(TAG, "onPbapStateChanged: 开始下载通话记录" + MyApplication.connectAddress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case NforeBtBaseJar.BT_SYNC_INTERRUPTED:
                //同步失败
                Log.d(TAG, "onPbapStateChanged: 同步失败");
                myHandler.sendEmptyMessage(0x01);
                break;
        }
    }

    private static class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //int childAdapterPosition = parent.getChildAdapterPosition(view);
            outRect.top = space;
//            outRect.left = space;
//            outRect.right = space;
        }
    }

    /**
     * 初始化所有 BusinessPresenter(业务逻辑),并赋值到对应 Fragment 中。
     */
    @Override
    public void initPresenters() {
        Log.d(TAG, "initPresenters: ");
        mBPresenter = MyApplication.mBPresenter;
        if (mBPresenter != null) {
            mBPresenter.registerUiBluetoothServiceConnectedListener(this);
            mBPresenter.registerUiBluetoothSettingChangeListerer(this);
            mBPresenter.registerUiBluetoothPhoneChangeListerer(this);
            mBPresenter.setContactFragment(contactFragment);
            mBPresenter.setRecordsFragment(recordsFragment);
            mBPresenter.registerView(this);
            contactFragment.setBPresenter(mBPresenter);
            recordsFragment.setBPresenter(mBPresenter);
        } else {
            Log.d(TAG, "initPresenters: MyApplication.mBPresenter === null");
        }
        mProxy = SrcMngSwitchProxy.getInstance();
        registerSourceOff();

    }

    private void unRegisterSourceOff() {
        mProxy.unRegisteSourceActionCallBackFunc(AdayoSource.ADAYO_SOURCE_BT_PHONE);
        Log.d(TAG, "unRegisterSourceAction SourceOff");
    }


    private void registerSourceOff() {
        Log.d(TAG, "registerSourceAction SourceOff");
        mProxy.registeSourceActionCallBackFunc(AdayoSource.ADAYO_SOURCE_BT_PHONE, new ISourceActionCallBack.Stub() {
            @Override
            public void SourceOff() throws RemoteException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        Log.d(TAG, "finish");
                    }
                });
            }
        });
    }

    /**
     * 获取首页 Fragment 的 ID 集合,ID 为 Fragment 唯一识别码。
     */
    @Override
    public List<Integer> getHostFragmentID() {
        LogUtils.iL(TAG, "getHostFragmentID");
        List<Integer> list = new ArrayList<>();
        list.add(1);
        return list;
    }

    /**
     * 将所有fragment信息装载到mFragmentSA容器内
     */
    private void loadFragmentSA() {
        LogUtils.iL(TAG, "loadFragmentSA");
        createFragmentInfo(R.id.main_content, 2, contactFragment);
        createFragmentInfo(R.id.main_content, 1, recordsFragment);
    }

    /**
     * 向所有fragment设置与activity进行通讯的通讯类
     */
    private void setCommunications() {
        LogUtils.iL(TAG, "setCommunications");
        contactFragment.setCommunication(this);
        recordsFragment.setCommunication(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(Color.WHITE);
        //设置状态栏图标为深色
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LogUtils.iL(TAG, "onCreate");
//		WindowManager.LayoutParams params = getWindow().getAttributes();
//		params.y = 60;
//		params.gravity = Gravity.LEFT;
//		params.width = 1920;
//		params.height = 1020;
//		getWindow().setAttributes(params);
        btPhoneMainActivity = this;
        main_tabs = findViewById(R.id.main_tabs);
//        RadioButton childAt = (RadioButton) main_tabs.getChildAt(0);
        tab_records = findViewById(R.id.tab_records);
        tab_contact = findViewById(R.id.tab_contact);
        main_tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "onCheckedChanged: " + group.getChildCount());
                for (int i = 0; i < group.getChildCount(); i++) {
                    if (checkedId == group.getChildAt(i).getId()) {
                        if (i == 0) {
                            Log.d(TAG, "onCheckedChanged: showpage1");
                            showPage(1);
                        }
                        if (i == 2) {
                            Log.d(TAG, "onCheckedChanged: showpage2");
                            showPage(2);
                        }
                    }
                }
            }
        });


    }

    public static BtPhoneMainActivity btPhoneMainActivity;

    @Override
    protected void onResume() {
        super.onResume();
        SrcMngSwitchProxy.getInstance().notifyServiceUIChange(AdayoSource.ADAYO_SOURCE_BT_PHONE);
        if (mBPresenter != null) {
            mBPresenter.registerUiBluetoothServiceConnectedListener(this);
            mBPresenter.registerUiBluetoothSettingChangeListerer(this);
        } else {
            Log.d(TAG, "onResume: mBPresenter == null");
        }
        LogUtils.iL(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.iL(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        phone_num.clearFocus();
        Log.d(TAG, "onStop");
//        CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
//        management.showCallInterface(BtPhoneMainActivity.this,CallInterfaceManagement.SHOW_TYPE_DIALOG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterSourceOff();
        LogUtils.iL(TAG, "onDestroy");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.number01:
                Log.v(TAG, "button_num1 onClicked");
                onNumberClick("1");
                break;
            case R.id.number02:
                Log.v(TAG, "button_num2 onClicked");

                onNumberClick("2");
                break;
            case R.id.number03:
                Log.v(TAG, "button_num3 onClicked");

                onNumberClick("3");
                break;
            case R.id.number04:
                Log.v(TAG, "button_num4 onClicked");

                onNumberClick("4");
                break;
            case R.id.number05:
                Log.v(TAG, "button_num5 onClicked");

                onNumberClick("5");
                break;
            case R.id.number06:
                Log.v(TAG, "button_num6 onClicked");

                onNumberClick("6");
                break;
            case R.id.number07:
                Log.v(TAG, "button_num7 onClicked");

                onNumberClick("7");
                break;
            case R.id.number08:
                Log.v(TAG, "button_num8 onClicked");

                onNumberClick("8");
                break;
            case R.id.number09:
                Log.v(TAG, "button_num9 onClicked");

                onNumberClick("9");
                break;
            case R.id.number00:
                Log.v(TAG, "button_num0 onClicked");

                onNumberClick("0");
                break;

            //  *  号键
            case R.id.numberxinghao:
                Log.v(TAG, "button_num* onClicked");
                onNumberClick("*");
                break;

            //  #  号建
            case R.id.numberjinhao:
                Log.v(TAG, "button_num# onClicked");
                onNumberClick("#");
                break;
            case R.id.keyboard_call:
                try {
                    if (input_number.length() != 0 && mBPresenter != null && mBPresenter.isHfpConnected()) {
                        mBPresenter.reqHfpDialCall(input_number);
                        Log.d(TAG, "onClick: 进入通话界面1");
//						Intent intent1 = new Intent();
//						intent1.setClass(this,CallingActivity.class);
//						intent1.putExtra("number",input_number);
//						intent1.putExtra("name","");
//						intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						startActivity(intent1);
                        input_number = input_number.substring(0, 0);
                        phone_num.setText(input_number);

                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.keyboard_delete:
//                if (input_number.length() != 0) {
//                    input_number = input_number.substring(0, input_number.length() - 1);
//                    phone_num.setText(input_number);
//                    phone_num.setSelection(phone_num.length());
//                }
                StringBuffer sb = new StringBuffer(phone_num.getText().toString().trim());
                int index = 0;
                if (phoneTextCursor == true) {
                    index = phone_num.getSelectionStart();
                    if (index > 0) {
                        sb = sb.delete(index - 1, index);
                    }
                } else {
                    index = phone_num.length();
                    if (index > 0) {
                        sb = sb.delete(index - 1, index);
                    }
                }
                input_number = sb.toString();
                phone_num.setText(sb.toString());
                if (index > 0) {
                    Selection.setSelection(phone_num.getText(), index - 1);
                }
                if (phone_num.getText().toString().trim().length() <= 0) {
//                    phone_num.setCursorVisible(false);
                    phoneTextCursor = false;
                }
                break;
            case R.id.main_disconnect:
                final ConfirmDialog confirmDialog = new ConfirmDialog(this, btRemoteDeviceName + "?");
                confirmDialog.show();
                confirmDialog.setClicklistener(new ConfirmDialog.ClickListenerInterface() {
                    @Override
                    public void doConfirm() {
                        if (mBPresenter == null) {
                            Log.d(TAG, "doConfirm: mBPresenter==" + null);
                            btConnect(false);
                            return;
                        }
                        try {
                            Log.d(TAG, "doConfirm: 断开蓝牙");
                            mBPresenter.reqBtDisconnectAll();//断开连接
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        confirmDialog.dismiss();
                    }

                    @Override
                    public void doCancel() {
                        confirmDialog.dismiss();
                    }
                });

                break;
            case R.id.main_disconnect_toconnect:
                //跳转到设置界面连接
//                Intent newAct = new Intent();
//                newAct.putExtra("flag",1);
//                newAct.setComponent(new ComponentName("com.adayo.app.settings", "com.adayo.app.settings.ui.activity.SettingsMainActivity"));
//                startActivity(newAct);
                SrcMngSwitchProxy srcMngSwitchProxy = SrcMngSwitchProxy.getInstance();
                Map<String, String> map = new HashMap<>();
                map.put("bt", "ConnectBt");
                SourceInfo info = new SourceInfo(AdayoSource.ADAYO_SOURCE_SETTING, map,
                        AppConfigType.SourceSwitch.APP_ON.getValue(), AppConfigType.SourceType.UI.getValue());
                srcMngSwitchProxy.onRequest(info);
                Log.d(TAG, "onClick: 通过源管理跳转到setting蓝牙界面");
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.keyboard_delete:
                input_number = input_number.substring(0, 0);
                phone_num.setText(input_number);
                break;
            case R.id.numberxinghao:
                onNumberClick(",");
                break;
            case R.id.number00:
                onNumberClick("+");
                break;
            case R.id.numberjinhao:
                onNumberClick(";");
                break;
        }
        return false;
    }

    private void onNumberClick(String input) {
//        if (input_number.length() <= 19) {
//            input_number += input;
//            phone_num.setText(input_number);
//            phone_num.setSelection(phone_num.length());
//        }

        StringBuffer sb = new StringBuffer(phone_num.getText().toString().trim());
        phone_num.setCursorVisible(true);
        phoneTextCursor = true;
        if (sb.length() > 19) {
            return;
        }
        if (phoneTextCursor == true) {
            //获得光标的位置
            int index = phone_num.getSelectionStart();
            //将字符串转换为StringBuffer

            //将字符插入光标所在的位置
            sb = sb.insert(index, input);
            input_number = sb.toString();
            phone_num.setText(sb.toString());
            //设置光标的位置保持不变
            Selection.setSelection(phone_num.getText(), index + 1);
        } else {
            Log.d(TAG, "onNumberClick: 没有光标输入");
            input_number += input;
            phone_num.setText(phone_num.getText().toString().trim() + input);
        }
    }

    public void disableShowInput(EditText editText) {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
            }
            try {
                method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
            }
        }
    }


    @Override
    public void onServiceConnectedChanged(boolean isConnected) {
        Log.d(TAG, "onServiceConnectedChanged: isConnected" + isConnected);
        if (isConnected) {
            isopen();
        }
    }

    @Override
    public void onEnableChanged(boolean isEnable) {

    }

    @Override
    public void onConnectedChanged(String address, int connectState) {


    }

    @Override
    public void onHfpStateChanged(String address, int connectState) {
        Log.d(TAG, "onHfpStateChanged: onHfpStateChanged" + connectState);
        if (mBPresenter == null)
            return;
        Message message = myHandler.obtainMessage(0x00);
        try {
            String btRemoteDeviceName1 = mBPresenter.getBtRemoteDeviceName(address);
            Bundle bundle = new Bundle();
            boolean isConnected = false;
            if (connectState == NforeBtBaseJar.CONNECT_DISCONNECT) {
                bundle.putString("name", "");
                bundle.putString("address", "");
                isConnected = false;
                //蓝牙断开关闭动画
//				main_content.setVisibility(View.VISIBLE);
//				empty_loading.setVisibility(View.GONE);
                stopRotate();
            } else if (connectState == NforeBtBaseJar.CONNECT_SUCCESSED) {
                bundle.putString("name", btRemoteDeviceName1);
                bundle.putString("address", address);
                btRemoteDeviceName = btRemoteDeviceName1;
                isConnected = true;
            }
            bundle.putBoolean("isConnected", isConnected);
            message.setData(bundle);
            myHandler.sendMessage(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHfpAudioStateChanged(String address, int prevState, int newState) {

    }

    @Override
    public void onAvrcpStateChanged(String address, int connectState) {

    }

    @Override
    public void onA2dpStateChanged(String address, int connectState) {

    }

    @Override
    public void onAdapterDiscoveryStarted() {

    }

    @Override
    public void onAdapterDiscoveryFinished() {

    }

    @Override
    public void retPairedDevices(int elements, String[] address, String[] name, int[] supportProfile) {

    }

    @Override
    public void onDeviceFound(String address, String name) {

    }

    @Override
    public void onDeviceBondStateChanged(String address, String name, int newState) {

    }

    @Override
    public void onLocalAdapterNameChanged(String name) {

    }

    private void isopen() {
        try {
            if (mBPresenter == null) {

            } else {
                Log.d(TAG, "============!mCommand.isHfpConnected()=============mBPresenter.isBtEnabled() ===== " + mBPresenter.isBtEnabled() + "========mBPresenter.isHfpConnected()==+" + mBPresenter.isHfpConnected());
                if (!mBPresenter.isBtEnabled() || !mBPresenter.isHfpConnected()) {

                    btConnect(false);
                    Log.d(TAG, "============!mCommand.isHfpConnected()================== ");
                } else if (mBPresenter.isHfpConnected()) {
                    Message message = myHandler.obtainMessage(0x00);
                    try {
                        String address = mBPresenter.getHfpConnectedAddress();
                        btRemoteDeviceName = mBPresenter.getBtRemoteDeviceName(address);
                        Bundle bundle = new Bundle();
                        bundle.putString("name", btRemoteDeviceName);
                        bundle.putString("address", address);
                        bundle.putBoolean("isConnected", true);
                        message.setData(bundle);
                        myHandler.sendMessage(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
