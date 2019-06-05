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
import com.nforetek.bt.phone.tools.CallInterfaceManagement;
import com.nforetek.bt.phone.tools.GetInfoFormContacts;
import com.nforetek.bt.phone.tools.WindowDialog;
import com.nforetek.bt.res.NfDef;

import java.util.List;


public class IncomingActivity extends Activity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(Color.WHITE);
        //设置状态栏图标为深色
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_incoming);
//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        params.y = 60;
//        params.gravity = Gravity.LEFT;
//        params.width = 1920;
//        params.height = 1020;
//        getWindow().setAttributes(params);
        mBPresenter = MyApplication.mBPresenter;
        bTphoneCallActivity = this;
        init();
//        autoAnswer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("IncomingActivity", "onNewIntent: ");
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null) {
            Log.d("IncomingActivity", "onNewIntent: +++++++++++++++++++++++++++++++");
        }
    }
    private void autoAnswer() {
        try {
            if(mBPresenter.getBtAutoAnswerState()){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                            if(mBPresenter != null && mBPresenter.isHfpConnected()){
                                if(callNumber!=null){
                                    mBPresenter.reqHfpAnswerCall(NfDef.CALL_ACCEPT_NONE);
//                                    Intent intent1 = new Intent();
//                                    intent1.setClass(IncomingActivity.this,CallingActivity.class);
//                                    intent1.putExtra("name",callName);
//                                    intent1.putExtra("number",callNumber);
//                                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(intent1);
                                    finish();
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (RemoteException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bTphoneCallActivity = null;
    }

    private void init() {
        incoming_answer = findViewById(R.id.incoming_answer);
        RelativeLayout incoming_showDialog = findViewById(R.id.incoming_showDialog);
        incoming_hangup = findViewById(R.id.incoming_hangup);
        incoming_name = findViewById(R.id.incoming_name);
        incoming_num = findViewById(R.id.incoming_num);
        calling_firName = findViewById(R.id.incoming_firName);
        calling_img_bg = findViewById(R.id.incoming_img_bg);
        List<NfHfpClientCall> hfpCallList = null;
        try {
            hfpCallList = mBPresenter.getHfpCallList();
            if(!hfpCallList.isEmpty()){
                NfHfpClientCall call = hfpCallList.get(0);
                callNumber = call.getNumber();
                callName = GetInfoFormContacts.getNameFromContacts(callNumber);
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


        incoming_num.setText(callNumber);

        incoming_showDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                moveTaskToBack(true);
//                WindowDialog instance = WindowDialog.getInstance(getApplication());
//                instance.show();
                CallInterfaceManagement management = CallInterfaceManagement.getCallInterfaceManagementInstance();
                management.showCallInterface(IncomingActivity.this,CallInterfaceManagement.SHOW_TYPE_DIALOG);
            }
        });
        incoming_hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(mBPresenter != null && mBPresenter.isHfpConnected()){
                        mBPresenter.reqHfpTerminateCurrentCall();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
        incoming_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(mBPresenter != null && mBPresenter.isHfpConnected()){
                        if(callNumber!=null){
                            mBPresenter.reqHfpAnswerCall(NfDef.CALL_ACCEPT_NONE);
//                            Intent intent1 = new Intent();
//                            intent1.setClass(IncomingActivity.this,CallingActivity.class);
//                            intent1.putExtra("name",callName);
//                            intent1.putExtra("number",callNumber);
//                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent1);
//                            finish();
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }






}
