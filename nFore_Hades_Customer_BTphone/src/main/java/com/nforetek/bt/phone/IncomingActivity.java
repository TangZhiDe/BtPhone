package com.nforetek.bt.phone;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.phone.tools.BtUtils;
import com.nforetek.bt.phone.tools.CallInterfaceManagement;
import com.nforetek.bt.phone.tools.GetInfoFormContacts;
import com.nforetek.bt.phone.tools.WindowDialog;
import com.nforetek.bt.res.NfDef;

import java.util.List;


public class IncomingActivity extends Activity {
    private static String TAG = "IncomingActivity"+MyApplication.Verson;
    private String callName;
    public static IncomingActivity bTphoneCallActivity;
    private String callNumber;
    private BtPresenter mBPresenter;
    private ImageView incoming_answer;
    private ImageView incoming_hangup;
    private TextView incoming_name;
    private TextView incoming_num;
    private TextView calling_firName;
    private ImageView calling_img_bg;
    private boolean isMove = false;//是否需要将蓝牙电话移动到后台


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        getWindow().setStatusBarColor(Color.WHITE);
//        //设置状态栏图标为深色
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_incoming);
        mBPresenter = MyApplication.mBPresenter;
        bTphoneCallActivity = this;
        init();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        isMove = false;
        Log.d(TAG, "onStart: ");
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
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        bTphoneCallActivity = null;
//        if(isMove){
//            moveTaskToBack(true);
//        }
    }

    private void init() {
        incoming_answer = findViewById(R.id.incoming_answer);
        RelativeLayout incoming_showDialog = findViewById(R.id.incoming_showDialog);
        incoming_hangup = findViewById(R.id.incoming_hangup);
        incoming_name = findViewById(R.id.incoming_name);
        incoming_num = findViewById(R.id.incoming_num);
        calling_firName = findViewById(R.id.incoming_firName);
        calling_img_bg = findViewById(R.id.incoming_img_bg);
        if(mBPresenter != null){
            List<NfHfpClientCall> hfpCallList = null;
            try {
                hfpCallList = mBPresenter.getHfpCallList();
                if(!hfpCallList.isEmpty()){
                    NfHfpClientCall call = hfpCallList.get(0);
                    callNumber = call.getNumber();
                    callName = mBPresenter.getCallName(callNumber);
                    if(callName != null && callName != ""){
                        calling_img_bg.setImageDrawable(getResources().getDrawable(R.drawable.icon_contact_image));
                        calling_firName.setVisibility(View.VISIBLE);
                        calling_firName.setText(callName.substring(0));
                    }else {
                        calling_img_bg.setImageDrawable(getResources().getDrawable(R.drawable.icon_contact_image_bg));
                        calling_firName.setVisibility(View.GONE);
                    }
                    incoming_name.setText(callName);
//                if("".equals(callName1)){
//                    callName1 = callNumber;
//                }

                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }



        incoming_num.setText(callNumber);
        incoming_showDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                moveTaskToBack(true);
//                WindowDialog instance = WindowDialog.getInstance(getApplication());
//                instance.show();
                isMove = true;
//                BtUtils.finish(IncomingActivity.this);
                CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                management.showCallInterface(CallInterfaceManagement.SHOW_TYPE_DIALOG);
            }
        });
        incoming_hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(mBPresenter != null && mBPresenter.isHfpConnected()){
                        mBPresenter.reqHfpRejectIncomingCall();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                BtUtils.finish(IncomingActivity.this);
            }
        });
        incoming_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(mBPresenter != null && mBPresenter.isHfpConnected()){
                        if(callNumber!=null){
                            mBPresenter.reqHfpAnswerCall(NfDef.CALL_ACCEPT_NONE);
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }






}
