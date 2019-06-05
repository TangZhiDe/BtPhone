package com.nforetek.bt.phone.tools;


import com.nforetek.bt.bean.Contacts;

import java.util.Comparator;

public class PinyinComparator implements Comparator<Contacts> {
	private String TAG="PinyinComparator";



	public int compare(Contacts o1, Contacts o2) {
		//这里主要是用来对ListView里面的数据根据ABCDEFG...来排序
		if (o2.getPinyin().equals("#")) {
			return -1;
		} else if (o1.getPinyin().equals("#")) {
			return 1;
		} else {
			return o1.getPinyin().compareTo(o2.getPinyin());
		}
	}

}
