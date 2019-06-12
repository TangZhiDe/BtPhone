package com.nforetek.bt.phone;

import static com.adayo.app.base.BaseConstant.FRAG_FIRST_PARAM;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.adayo.app.base.BaseFragment;
import com.adayo.app.utils.LogUtils;
import com.alibaba.fastjson.JSON;
import com.nforetek.bt.bean.Contacts;
import com.nforetek.bt.phone.adapter.SortAdapter;
import com.nforetek.bt.phone.presenter.BtPresenter;
import com.nforetek.bt.phone.tools.MyLinearLayoutManager;
import com.nforetek.bt.phone.tools.PinyinComparator;
import com.nforetek.bt.phone.tools.RecyclerViewForEmpty;
import com.nforetek.bt.phone.tools.WaveSideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author tzd
 *
 */
public class ContactFragment extends BaseFragment<BtPresenter>{

	private static final String TAG = ContactFragment.class.getCanonicalName();
	private BtPresenter mBPresenter;
    private WaveSideBar sideBar1;
    private RecyclerViewForEmpty mRecyclerView;
    private MyLinearLayoutManager manager;
    private SortAdapter adapter;
    private LinearLayout contact_refresh;
    private boolean isHidden = true;//fragment是否显示
    private boolean needUpdate = false;//是否需要刷新适配器

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
        isHidden = hidden;
        Log.d(TAG, "onHiddenChanged: isHidden===="+isHidden );
//        if(!isHidden && needUpdate){
//            Log.d(TAG, "onHiddenChanged: 联系人碎片显示 刷新适配器" );
//            getList();
//        }

        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");

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
    public static ContactFragment newInstance(String param) {
    	ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putString(FRAG_FIRST_PARAM, param);
        fragment.setArguments(args);
        return fragment;
    }
    
    /**
     *	获取该 Fragment 布局控件 ID。 
     */
	@Override
	public int getLayout() {
		LogUtils.iL(TAG, "getLayout");
		return R.layout.fragment_page1;
	}

	/**
     *	初始化该 Fragment 布局内的 UI 控件。 
     */
	@Override
	public void initView() {
		LogUtils.iL(TAG, "initView");
        init();
	}

    private void init() {

        sideBar1 =  getContentView().findViewById(R.id.sidebar);
        contact_refresh = getContentView().findViewById(R.id.contact_refresh);
        mRecyclerView = getContentView().findViewById(R.id.recyclerview);
        sideBar1.setOnTouchingLetterChangedListener(new WaveSideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if (adapter != null) {
                    if (adapter.getItemCount() > 0) {
                        int position = adapter.getPositionForSection(s.charAt(0));
                        if (position != -1) {
                            Log.d(TAG, "onTouchingLetterChanged: 滑动" );
                            manager.scrollToPositionWithOffset(position, 0);
                        }
                    }
                }
            }
        });
//        sideBar1.setOnSelectIndexItemListener(new WaveSideBar1.OnSelectIndexItemListener() {
//            @Override
//            public void onSelectIndexItem(String index) {
//                if (adapter != null) {
//                    if (adapter.getItemCount() > 0) {
//                        int position = adapter.getPositionForSection(index.charAt(0));
//                        if (position != -1) {
//                            manager.scrollToPositionWithOffset(position, 0);
//                        }
//                    }
//                }
//            }
//        });
        manager = new MyLinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
//        manager.setRecycleChildrenOnDetach(true);
//        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
//        mRecyclerView.addItemDecoration(new SpaceItemDecoration(10));
        adapter = new SortAdapter(getActivity(), MyApplication.contactList);
        View contacts_empty = getContentView().findViewById(R.id.contacts_empty);
        mRecyclerView.setEmptyView(contacts_empty);
        mRecyclerView.setAdapter(adapter);
        ImageView refresh = getContentView().findViewById(R.id.contacts_empty_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: size=="+MyApplication.contactList.size()+" canRefresh=="+mRecyclerView.canRefresh);
                if(MyApplication.contactList.size()>0 && !mRecyclerView.canRefresh){
                    return;
                }
                mBPresenter.refresh();
            }
        });
        //滑动监听
        adapter.setOnItemClickListener(new SortAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Contacts contacts = MyApplication.contactList.get(position);
                try {
                    if (mBPresenter != null && mBPresenter.isHfpConnected()) {
                        String numberJSON = contacts.getNumberJSON();
                        List<String> list = JSON.parseArray(numberJSON, String.class);
                        if(list != null && list.size()>0){
                            Log.d(TAG, "onItemClick: num==="+list.get(0)+"--size=="+list.size() );
                            mBPresenter.reqHfpDialCall(list.get(0));
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        contact_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: size=="+MyApplication.contactList.size()+" canRefresh=="+mRecyclerView.canRefresh);
                if(MyApplication.contactList.size()>0 && !mRecyclerView.canRefresh){
                    return;
                }
                Log.d(TAG, "onClick: 刷新" );
                mBPresenter.refresh();
            }
        });
        getList();
    }
    public void notifyDataSetChanged(){
        MyApplication.contactList.clear();
        adapter.notifyDataSetChanged();
    }
    public  void getList() {
        MyApplication.contactList.clear();
        Log.d(TAG, "handleMessage: contactsListForDB 刷新列表");
        List<Contacts> contactsListForDB = null;
        List<Contacts> temp = new ArrayList<>();
        try {
            contactsListForDB = mBPresenter.getContactsListForDB();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(contactsListForDB != null) {
            Collections.sort(contactsListForDB, new PinyinComparator());
            MyApplication.contactList.addAll(contactsListForDB);
        }
        adapter.notifyDataSetChanged();
        //!isHidden
//        if(!isHidden){
//            Log.d(TAG, "getList: isHidden==false开始刷新 needUpdate==false" );
//            needUpdate = false;
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    MyApplication.contactList.clear();
//                    Log.d(TAG, "handleMessage: contactsListForDB 刷新列表");
//                    List<Contacts> contactsListForDB = null;
//                    List<Contacts> temp = new ArrayList<>();
//                    try {
//                        contactsListForDB = mBPresenter.getContactsListForDB();
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                    if(contactsListForDB != null) {
//                        Collections.sort(contactsListForDB, new PinyinComparator());
//                            MyApplication.contactList.addAll(contactsListForDB);
//                            myHandler.sendEmptyMessage(0x00);
//
//                    }
//                }
//            }).start();
//        }else {
//            Log.d(TAG, "getList: isHidden==true   needUpdate==true" );
//            needUpdate = true;
//        }


    }

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 0x00:
                    adapter.notifyDataSetChanged();
                    break;
                case 0x01:
                    Bundle data = msg.getData();
                    int position = data.getInt("position");
                    int count = data.getInt("count");
                    Log.d(TAG, "myHandler: position==="+position+" ===count==="+count );
                    adapter.notifyItemRangeChanged(position,count);
                    break;
            }
            return false;
        }
    });

    List<String> arrays = new ArrayList<String>(){
        @Override
        public boolean add(String e) {
            for(String str:this){
                if(str.equals(e)){
                    return false;
                }else{
                }
            }
            return super.add(e);
        }
    };

    /**
     *	初始化数据,主要对 BusinessPresenter 的注册和初始化,以及一些其他相关工具的初始化。
     */
	@Override
	public void initData() {
		LogUtils.iL(TAG, "initData");

	}

    @Override
    public void setBPresenter(BtPresenter btPresenter) {
        this.mBPresenter = btPresenter;
    }

    private static class SpaceItemDecoration extends RecyclerView.ItemDecoration{
        private int space;
        public SpaceItemDecoration(int space){
            this.space =space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //int childAdapterPosition = parent.getChildAdapterPosition(view);
            outRect.top = space;
//            outRect.left = space;
//            outRect.right = space;
        }
    }

}
