package com.nforetek.bt.phone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.phone.tools.CallInterfaceManagement;
import com.nforetek.bt.phone.tools.GetInfoFormContacts;
import com.nforetek.bt.res.NfDef;

import java.lang.reflect.Method;
import java.util.List;


public class CallingActivity extends Activity implements View.OnClickListener, View.OnLongClickListener{
    //,
    //        BtPresenter.UiBluetoothSettingChangeListerer, BtPresenter.UiBluetoothPhoneChangeListerer
    private static String TAG = CallingActivity.class.getCanonicalName();
    private BtPresenter mBPresenter;
    private RelativeLayout one, two, three, four, five, six, seven, eight, nine, zero, asterisk, pound;
    private EditText phone_num;
    private String input_number = "";
    private ImageView calling_keyboard_btn;
    private ImageView calling_hangup;
    private ImageView calling_decice;
    private ImageView calling_yuying;
    private View calling_keyboard;
    private LinearLayout calling_info;
    private TextView calling_state;
    private LinearLayout calling_thz;

    private TextView calling_name;
    private RelativeLayout calling_showDialog;
    private TextView calling_num;
    private TextView calling_firName;
    private ImageView calling_img_bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(Color.WHITE);
        //设置状态栏图标为深色
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_calling);
        MyApplication.callingActivity = this;

        mBPresenter = MyApplication.mBPresenter;
        if(mBPresenter != null){
            mBPresenter.setCallingActivity(this);
        }
        Log.d(TAG, "onCreate: " );
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: ");
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null) {
            Log.d(TAG, "onNewIntent: +++++++++++++++++++++++++++++++");
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mBPresenter != null) {
            if (MyApplication.isKeyboardShow) {
                calling_info.setVisibility(View.GONE);
                calling_keyboard.setVisibility(View.VISIBLE);
            } else {
                calling_info.setVisibility(View.VISIBLE);
                calling_keyboard.setVisibility(View.GONE);
            }
            getCallList();
            try {
//                if (mBPresenter.getHfpAudioConnectionState() != NfDef.STATE_CONNECTED) {
//                    calling_decice.setImageDrawable(getDrawable(R.drawable.select_hands_free));
//                } else {
//                    calling_decice.setImageDrawable(getDrawable(R.drawable.select_private));
//                }
                if(mBPresenter.getHfpAudioConnectionState() == NfDef.STATE_READY){
                    calling_decice.setImageDrawable(getDrawable(R.drawable.select_private));
                }else if(mBPresenter.getHfpAudioConnectionState() == NfDef.STATE_CONNECTED){
                    calling_decice.setImageDrawable(getDrawable(R.drawable.select_hands_free));
                }
                Log.d(TAG, "onStart: isHfpMicMute()="+mBPresenter.isHfpMicMute()+"----getHfpAudioConnectionState="+mBPresenter.getHfpAudioConnectionState());
                if (mBPresenter.isHfpMicMute()) {
                    calling_yuying.setImageDrawable(getDrawable(R.drawable.select_mute));
                } else {
                    //mic非静音
                    calling_yuying.setImageDrawable(getDrawable(R.drawable.select_voice));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void init() {
        calling_keyboard_btn = findViewById(R.id.calling_keyboard_btn);
        calling_hangup = findViewById(R.id.calling_hangup);
        calling_decice = findViewById(R.id.calling_decice);
        calling_name = findViewById(R.id.calling_name);
        calling_showDialog = findViewById(R.id.calling_showDialog);
        calling_num = findViewById(R.id.calling_num);
        calling_firName = findViewById(R.id.calling_firName);
        calling_img_bg = findViewById(R.id.calling_img_bg);
        calling_showDialog.setOnClickListener(this);
        calling_hangup.setOnClickListener(this);
        calling_keyboard_btn.setOnClickListener(this);
        calling_yuying = findViewById(R.id.calling_yuying);
        calling_keyboard = findViewById(R.id.calling_keyboard);
        calling_info = findViewById(R.id.calling_info);
        calling_thz = findViewById(R.id.calling_thz);
        calling_state = findViewById(R.id.calling_state);
        calling_yuying.setOnClickListener(this);
        calling_decice.setOnClickListener(this);
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
        phone_num = findViewById(R.id.editnum_phone1);
        disableShowInput(phone_num);
//        phone_num.setInputType(InputType.TYPE_NULL);
        phone_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        getCallList();
    }


    private long time1 = 0;
    public Handler myHandler = new Handler(new android.os.Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    int arg1 = msg.arg1;
                    if(arg1 == NfDef.STATE_READY){
                        calling_decice.setImageDrawable(getDrawable(R.drawable.select_private));
                    }else if(arg1 == NfDef.STATE_CONNECTED){
                        calling_decice.setImageDrawable(getDrawable(R.drawable.select_hands_free));
                    }
                    break;
                case 0x05:
                    Bundle data = msg.getData();
                    String time = data.getString("time");
                    Log.d(TAG, "handleMessage: time" + time);
                    if ("00:00".equals(time)) {

                    } else {
                        calling_state.setText(time);
                    }
                    break;

            }

            return false;
        }
    });

    //处理时间格式
    private String getTimeByCount(long time) {
        String minute;
        if (time < 60) {
            if (time < 10) {
                return "00:0" + time;
            } else {
                return "00:" + time;
            }

        } else {
            if ((time / 60) > 99) {
                minute = (time / 60) + "";
            } else {
                minute = (time / 60) > 9 ? time / 60 + "" : "0" + (time / 60);
            }
            long sec = time % 60;
            String secStr = "";
            if (sec < 10) {
                secStr = "0" + sec;
            } else {
                secStr = "" + sec;
            }
            return minute + ":" + secStr;
        }
    }




    public void getCallList() {
        Log.i(TAG, "-----------------------getCallList-----------------------");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "-----------------------getCallList------------runOnUiThread-----------");
                if (mBPresenter == null) {
                    Log.d(TAG, "--------MainApplication.app.mCommand---is------nulll-----");
                    return;
                } else {
                    try {
                        List<NfHfpClientCall> callList = mBPresenter.getHfpCallList();
                        if (callList != null) {

                            switch (callList.size()) {
                                case 1: {
                                    Log.i(TAG, "------callList.size()-------" + callList.size());
                                    NfHfpClientCall call = callList.get(0);
                                    String number = call.getNumber();
                                    Log.i(TAG, "------number-------" + number);
                                    calling_num.setText(number);
                                    String callName = GetInfoFormContacts.getNameFromContacts(number);
                                    if(callName != null && callName != ""){
                                        calling_img_bg.setImageDrawable(getResources().getDrawable(R.drawable.icon_contact_image));
                                        calling_firName.setVisibility(View.VISIBLE);
                                        calling_firName.setText(callName.substring(0));
                                    }else {
                                        calling_img_bg.setImageDrawable(getResources().getDrawable(R.drawable.icon_contact_image_bg));
                                        calling_firName.setVisibility(View.GONE);
                                    }
                                    calling_name.setText(callName);
                                    getCallState(call);
                                }
                                break;
                                case 0: {//如果通话结束
                                    Log.d(TAG, "run: ==================list为0 finish==================");
                                    finish();
                                }

                            }

                        } else {
                            Log.d(TAG, "----callList is null----");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }




    private void getCallState(NfHfpClientCall call) {
        Log.i(TAG, "------getCallState()----state---" + call.getState());
        String callStateStr = "";
        if (call.getState() == NfHfpClientCall.CALL_STATE_HELD) {
            Log.d(TAG, "===============保留...===============");
            callStateStr = "保留...";

        } else if (call.getState() == NfHfpClientCall.CALL_STATE_DIALING) {
            Log.d(TAG, "============正在拨号...===================");
            calling_thz.setVisibility(View.GONE);
            callStateStr = getResources().getString(R.string.string15);
            calling_state.setText(callStateStr);
        } else if (call.getState() == NfHfpClientCall.CALL_STATE_INCOMING) {
            Log.d(TAG, "===============来电...=====================");
            callStateStr = getResources().getString(R.string.string12);
            calling_state.setText(callStateStr);
        } else if (call.getState() == NfHfpClientCall.CALL_STATE_ACTIVE)        //通话中
        {
            Log.d(TAG, "===========通话中开始计时================");
            calling_thz.setVisibility(View.VISIBLE);
//            myHandler.sendEmptyMessage(0x05);
        } else if (call.getState() == NfHfpClientCall.CALL_STATE_WAITING) {
            callStateStr = "来电...";
        } else if (call.getState() == NfHfpClientCall.CALL_STATE_TERMINATED)        //挂断
        {
            Log.d(TAG, "---------------通话状态-----挂断--------------");
            callStateStr = getResources().getString(R.string.string14);
            calling_state.setText(callStateStr);

        } else {
            Log.d(TAG, "---------------通话状态其它-------------------");
        }

    }

    private void onNumberClick(String input) {
        try {
            if(mBPresenter != null){
                mBPresenter.reqHfpSendDtmf(input);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        input_number += input;
        phone_num.setText(input_number);
        phone_num.setSelection(phone_num.length());

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

    private boolean isKeyboardShow = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            case R.id.keyboard_delete:
                if (input_number.length() != 0) {
                    input_number = input_number.substring(0, input_number.length() - 1);
                    phone_num.setText(input_number);
                    phone_num.setSelection(phone_num.length());
                }
                break;
            case R.id.calling_keyboard_btn:
//                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) calling_show.getLayoutParams();
                if (!MyApplication.isKeyboardShow) {
                    calling_info.setVisibility(View.GONE);
                    calling_keyboard.setVisibility(View.VISIBLE);
                } else {
                    calling_info.setVisibility(View.VISIBLE);
                    calling_keyboard.setVisibility(View.GONE);
                }
                MyApplication.isKeyboardShow = !MyApplication.isKeyboardShow;
                isKeyboardShow = !isKeyboardShow;
                break;
            case R.id.calling_decice:
                if (mBPresenter != null) {
                    try {
                        if (mBPresenter.getHfpAudioConnectionState() == NfDef.STATE_CONNECTED) {
                            mBPresenter.reqHfpAudioTransferToPhone();
//                            calling_decice.setImageDrawable(getDrawable(R.drawable.select_private));
                        } else {
                            //NfDef.STATE_READY
                            mBPresenter.reqHfpAudioTransferToCarkit();
//                            calling_decice.setImageDrawable(getDrawable(R.drawable.select_hands_free));
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.calling_yuying:
                if (mBPresenter != null) {
                    try {
                        Log.d(TAG, " isHfpMicMute()="+mBPresenter.isHfpMicMute());
                        if (mBPresenter.isHfpMicMute()) {
                            mBPresenter.muteHfpMic(false);
                            calling_yuying.setImageDrawable(getDrawable(R.drawable.select_voice));
                            Log.d(TAG, "muteHfpMic(false)  isHfpMicMute()="+mBPresenter.isHfpMicMute());
                        } else {
                            //mic非静音
                            mBPresenter.muteHfpMic(true);//设置mic静音
                            calling_yuying.setImageDrawable(getDrawable(R.drawable.select_mute));
                            Log.d(TAG, "muteHfpMic(true)  isHfpMicMute()="+mBPresenter.isHfpMicMute());
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.calling_hangup:
                try {

                    if (mBPresenter != null && mBPresenter.isHfpConnected()) {
                        mBPresenter.reqHfpTerminateCurrentCall();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onClick:calling_hangup ");
//                finish();
                break;
            case R.id.calling_showDialog:
                CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                management.showCallInterface(this,CallInterfaceManagement.SHOW_TYPE_DIALOG);
                break;
            default:
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
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if(mBPresenter != null){
            mBPresenter.setCallingActivity(null);
        }
        MyApplication.callingActivity = null;

    }



}
