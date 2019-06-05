package com.nforetek.bt.phone;

import static com.adayo.app.base.BaseConstant.FRAG_FIRST_PARAM;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.adayo.app.base.BaseFragment;
import com.adayo.app.utils.LogUtils;
import com.nforetek.bt.bean.CallLogs;
import com.nforetek.bt.phone.adapter.RecordsRecyclerAdapter;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.phone.tools.MyLinearLayoutManager;
import com.nforetek.bt.phone.tools.RecyclerViewForEmpty;
import com.nforetek.bt.res.NfDef;

import java.util.List;

/**
 * @author tzd
 */
public class RecordsFragment extends BaseFragment<BtPresenter> {

    private static final String TAG = RecordsFragment.class.getCanonicalName();
    private BtPresenter mBPresenter;
    private RecyclerViewForEmpty recycle;
    private RecordsRecyclerAdapter adapter;
    private int mProperty = NfDef.PBAP_PROPERTY_MASK_FN |
            NfDef.PBAP_PROPERTY_MASK_N |
            NfDef.PBAP_PROPERTY_MASK_TEL |
            NfDef.PBAP_PROPERTY_MASK_VERSION |
            NfDef.PBAP_PROPERTY_MASK_ADR |
            NfDef.PBAP_PROPERTY_MASK_EMAIL |
            NfDef.PBAP_PROPERTY_MASK_PHOTO |
            NfDef.PBAP_PROPERTY_MASK_ORG;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtils.iL(TAG, "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LogUtils.iL(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        LogUtils.iL(TAG, "onHiddenChanged");
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        LogUtils.iL(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        LogUtils.iL(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        LogUtils.iL(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LogUtils.iL(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public View getContentView() {
        LogUtils.iL(TAG, "getContentView");
        return super.getContentView();
    }

    @Override
    public Context getMContext() {
        LogUtils.iL(TAG, "getMContext");
        return super.getMContext();
    }

    /**
     * Fragment实例化方法，用于向自身传送数据
     */
    public static RecordsFragment newInstance(String param) {
        RecordsFragment fragment = new RecordsFragment();
        Bundle args = new Bundle();
        args.putString(FRAG_FIRST_PARAM, param);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 获取该 Fragment 布局控件 ID。
     */
    @Override
    public int getLayout() {
        LogUtils.iL(TAG, "getLayout");
        return R.layout.fragment_page2;
    }


    /**
     * 初始化该 Fragment 布局内的 UI 控件。
     */
    @Override
    public void initView() {
        recycle = getContentView().findViewById(R.id.records_recycle);
        MyLinearLayoutManager myLinearLayoutManager = new MyLinearLayoutManager(getMContext(), LinearLayoutManager.VERTICAL, false);
        recycle.setLayoutManager(myLinearLayoutManager);
        adapter = new RecordsRecyclerAdapter(getMContext(), MyApplication.recordsList);
        View records_empty = getContentView().findViewById(R.id.records_empty);
        recycle.setEmptyView(records_empty);
        recycle.setAdapter(adapter);
        adapter.setOnItemClickLenster(new RecordsRecyclerAdapter.OnItemClickLenster() {
            @Override
            public void itemClick(int position, View view) {
                CallLogs callLogs = MyApplication.recordsList.get(position);
                //TODO 去电
                try {
                    if (mBPresenter != null && mBPresenter.isHfpConnected()) {
                        mBPresenter.reqHfpDialCall(callLogs.getNumber());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        ImageView refresh = getContentView().findViewById(R.id.empty_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             mBPresenter.refresh();
            }
        });
        getList();
    }

    public void getList() {
        MyApplication.recordsList.clear();
        Log.d(TAG, "getList: 刷新列表");
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CallLogs> listForDB = null;
                try {
                    listForDB = mBPresenter.getCallLogsListForDB(0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if(listForDB!=null){
                    Log.d(TAG, "handleMessage: listForDB.size=="+listForDB.size());
                    MyApplication.recordsList.addAll(listForDB);
                }else {
                    Log.d(TAG, "handleMessage: listForDB==null");
                }
                myHandler.sendEmptyMessage(0x00);
            }
        }).start();


    }

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0x00:
                    adapter.notifyDataSetChanged();
                    break;

            }
            return false;
        }
    });

    public void notifyDataSetChanged(){
        MyApplication.recordsList.clear();
        adapter.notifyDataSetChanged();
    }

    /**
     * 初始化数据,主要对 BusinessPresenter 的注册和初始化,以及一些其他相关工具的初始化。
     */
    @Override
    public void initData() {
        LogUtils.iL(TAG, "initData");

    }

    @Override
    public void setBPresenter(BtPresenter btPresenter) {
        this.mBPresenter = btPresenter;
    }




}
