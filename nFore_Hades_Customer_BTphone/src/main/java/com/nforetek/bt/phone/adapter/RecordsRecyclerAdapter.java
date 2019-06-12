package com.nforetek.bt.phone.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nforetek.bt.bean.CallLogs;
import com.nforetek.bt.phone.R;
import com.nforetek.bt.phone.tools.DateFormatter;
import com.nforetek.bt.res.NfDef;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class RecordsRecyclerAdapter extends RecyclerView.Adapter<RecordsRecyclerAdapter.MyViewHolder> {
    private Context context;
    private List<CallLogs> list;

    public RecordsRecyclerAdapter(Context context, List<CallLogs> list){
       this.context = context;
       this.list = list;
   }




    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup viewGroup, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(com.nforetek.bt.phone.R.layout.item_recycle_records, viewGroup, false);
//        if(viewType == 1){
//            inflate.setBackground(context.getDrawable(R.drawable.call_select_icon));
//
//        }else {
//            inflate.setBackground(context.getDrawable(R.color.tran));
//        }

        MyViewHolder myViewHolder = new MyViewHolder(inflate);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder( MyViewHolder myViewHolder, final int i) {
        CallLogs callBean = list.get(i);
        Date date_year = null;
        Date date_time = null;
        if(!callBean.getName().equals("未知号码")){
            myViewHolder.name.setText(callBean.getName());
        }else {
            myViewHolder.name.setText(callBean.getNumber());
        }

        if(myViewHolder.view!=null){
            myViewHolder.view.setBackground(context.getResources().getDrawable(R.drawable.selector_item_pressed));
            myViewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickLenster.itemClick(i,v);
                }
            });
        }
        //f20707
        if(callBean.getType() == NfDef.PBAP_STORAGE_MISSED_CALLS )
        {
            myViewHolder.img.setImageResource(com.nforetek.bt.phone.R.drawable.bt_list_ic_hangup);
            myViewHolder.name.setTextColor(Color.parseColor("#f20707"));

        }else if(callBean.getType() == NfDef.PBAP_STORAGE_RECEIVED_CALLS )
        {
            myViewHolder.img.setImageResource(com.nforetek.bt.phone.R.drawable.bt_list_ic_answer);
            myViewHolder.name.setTextColor(context.getResources().getColor(R.color.btcolor12));
        }else if(callBean.getType() == NfDef.PBAP_STORAGE_DIALED_CALLS )
        {
            myViewHolder.img.setImageResource(com.nforetek.bt.phone.R.drawable.bt_list_ic_call);
            myViewHolder.name.setTextColor(context.getResources().getColor(R.color.btcolor12));
        }
        String str = callBean.getTime();
        Log.d("TAG", "onBindViewHolder: callBean.getTime==="+str);
        String time = "";
        //20180728T154401
        if (!str.isEmpty()) {
            String year = str.substring(0, str.indexOf("T"));
//            if(year.length() == 8){
//                StringBuffer stringBuffer = new StringBuffer(year);
//                stringBuffer.insert(6,"-");
//                stringBuffer.insert(4,"-");
//                year = stringBuffer.toString();
//                Log.d("TAG", "onBindViewHolder:year==== "+year);
//            }
            SimpleDateFormat sf_year = new SimpleDateFormat("yyyyMMdd");
            try {
                date_year = sf_year.parse(year);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (DateFormatter.isToday(date_year)) {
//                int period = Integer.parseInt(DateFormatter.format(date_time, DateFormatter.Template.CHAT_MSG_HOUR));
//                if (period >= 0 && period <= 6) {
//                    time = DateFormatter.format(date_time, DateFormatter.Template.TIME)+" AM";
//                } else if (period > 6 && period <= 12) {
//                    time = DateFormatter.format(date_time, DateFormatter.Template.TIME)+" AM";
//                } else if (period > 12 && period <= 18) {
//                    time = DateFormatter.format(date_time, DateFormatter.Template.TIME)+" PM";
//                } else {
//                    time = DateFormatter.format(date_time, DateFormatter.Template.TIME)+" PM";
//                }
                time = timeFormat(str);
            } else if (DateFormatter.isYesterday(date_year)) {
                time = "昨天";
            }else if (DateFormatter.isWeekDay(date_year)) {
                time = DateFormatter.format(date_year, DateFormatter.Template.WEEK);
            } else {
//                time = DateFormatter.format(date_year, DateFormatter.Template.STRING_DAY_MONTH_YEAR) ;
                time = yearFormat(str);
            }


            myViewHolder.time.setText(time);
        }
    }

    /**
     *	时间格式转换
     */
    public static String yearFormat(String time){
        if(time == null || time == "") return "time is null";
        String year = time.substring(0, 4);
        String month = time.substring(4, 6);
        String day = time.substring(6, 8);
        return year+"-"+month+"-"+day;
    }
    public static String timeFormat(String time){
        if(time == null || time == "") return "time is null";
        String hour = time.substring(9,11);
        String minute = time.substring(11,13);
        String second = time.substring(13,15);
        return hour+":"+minute;
    }
    private OnItemClickLenster onItemClickLenster;
    public interface OnItemClickLenster{
        void itemClick(int position, View view);
    }
    public void setOnItemClickLenster(OnItemClickLenster onItemClickLenster){
       this.onItemClickLenster = onItemClickLenster;
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

       private TextView name;
       private TextView time;
        private ImageView img;
        private View view;
        public MyViewHolder( View itemView) {
            super(itemView);
            name = itemView.findViewById(com.nforetek.bt.phone.R.id.irr_name);
            time = itemView.findViewById(com.nforetek.bt.phone.R.id.irr_time);
            img = itemView.findViewById(com.nforetek.bt.phone.R.id.irr_img);
            view = itemView.findViewById(com.nforetek.bt.phone.R.id.irr_item);
        }
    }
}
