package com.nforetek.bt.phone.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.nforetek.bt.bean.Contacts;
import com.nforetek.bt.phone.R;

import java.util.List;


/**
 * @author tzd
 *
 */

public class SortAdapter extends RecyclerView.Adapter<SortAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<Contacts> mData;
    private Context mContext;
    private int type;
    private String changeStr = "";
    public String schar;
    public SortAdapter(Context context, List<Contacts> data,int type) {
        mInflater = LayoutInflater.from(context);
        mData = data;
        this.type = type;
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_recycle_contact, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        if(type == 0){
            viewHolder.num.setVisibility(View.VISIBLE);
        }else {
            viewHolder.num.setVisibility(View.GONE);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d("TAG", "onBindViewHolder: "+position);

        if (mOnItemClickListener != null) {
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });
        }

        String numberJSON = this.mData.get(position).getNumberJSON();
        List<String> list = JSON.parseArray(numberJSON, String.class);
        if(type == 0){
            if(list != null && list.size()>0){
                if(list.get(0).contains(changeStr)){
                    int index = list.get(0).indexOf(changeStr);
                    int len = changeStr.length();
                    Spanned temp = Html.fromHtml(list.get(0).substring(0, index)
                            + "<font color="+mContext.getResources().getColor(R.color.device_name) + ">"
                            + list.get(0).substring(index, index + len) + "</font>"
                            + list.get(0).substring(index + len, list.get(0).length()));
                    holder.num.setText(temp);
                }else {
                    holder.num.setText(list.get(0));
                }
            }
        }
        if(this.mData.get(position).getName().isEmpty()){
            if(list != null && list.size()>0){
                holder.name.setText(list.get(0));
                holder.firName.setText("");
            }

        } else {
            holder.name.setText(this.mData.get(position).getName());
            holder.firName.setText(this.mData.get(position).getName().substring(0));
        }
    }




    @Override
    public int getItemCount() {
        return mData.size();
    }

    //**********************itemClick************************
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    //这个方法很重要，editText监听文本变化需要用到
    public void changeText(String textStr) {
        this.changeStr = textStr;
//        notifyDataSetChanged();

    }

    public void setContactsList(List<Contacts> mData){
        this.mData = mData;
    }


    //***************************************************************

//*******************************************************
   public interface getStringChar{
       void getStringChar(String p);
}

private getStringChar getStringChar;
    public void setListener( getStringChar listener){
         this.getStringChar = listener;
    }

    //**************************************************************

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView firName;
        private TextView num;
        private View view;
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(com.nforetek.bt.phone.R.id.irc_name);
            firName = itemView.findViewById(com.nforetek.bt.phone.R.id.irc_firName);
            num = itemView.findViewById(com.nforetek.bt.phone.R.id.irc_num);
            view = itemView;
        }
    }



    public Object getItem(int position) {
        return mData.get(position);
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的char ascii值
     */
    public int getSectionForPosition(int position) {
        return mData.get(position).getSzm().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = mData.get(i).getPinyin();
            if (sortStr != null) {
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void getFirstPosition(){
        int FirstPosition;
        for (int i = 0; i < getItemCount(); i++) {
            String firtStr=mData.get(i).getSzm();
            FirstPosition=firtStr.toUpperCase().charAt(0);
        }
    }



//    public  String getFirstChar(String ontA){
//
//        String firstChar=schar;
//
//        return firstChar;
//
//    }

}
