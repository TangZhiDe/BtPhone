package com.nforetek.bt.phone.tools;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.nforetek.bt.bean.Contacts;
import com.nforetek.bt.phone.MyApplication;

import java.util.List;

/**
 * Created by tzd on 2018/12/29.
 */

public class GetInfoFormContacts {
    public static String getNameFromContacts(String num){
        String name = "";
        Log.e("GetInfoFormContacts", "getNameFromContacts: " );
        if(MyApplication.contactList != null && MyApplication.contactList.size()>0){
            for (int i = 0; i < MyApplication.contactList.size(); i++) {
                Contacts contacts = MyApplication.contactList.get(i);
                String numberJSON = contacts.getNumberJSON();
                Log.e("GetInfoFormContacts", "numberJSON: "+numberJSON );
                List<String> list = JSON.parseArray(numberJSON, String.class);
//                List<String> list = (List<String>) JSON.parseObject(numberJSON);
                if(list != null && list.size()>0){
                    Log.e("GetInfoFormContacts", "getNameFromContacts: "+list.get(0) );
                    if(list.get(0).equals(num)){
                        name = contacts.getName();
                    }
                }else {
                    Log.e("GetInfoFormContacts", "getNameFromContacts: list为null--numberJSON="+numberJSON );
                }

            }
        }
        return name;
    }
}
