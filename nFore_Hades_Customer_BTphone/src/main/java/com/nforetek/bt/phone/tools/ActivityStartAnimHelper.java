package com.nforetek.bt.phone.tools;

import android.app.ActivityOptions;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.nforetek.bt.phone.R;

import java.util.HashMap;

public class ActivityStartAnimHelper {
    public static final String KEY_LOCATIONX = "anim_locationX";
    public static final String KEY_LOCATIONY = "anim_locationY";
    public static final String KEY_INDEX_IN_LINOS = "anim_index";
    public static final String KEY_VIEW_WIDTH = "anim_width";
    public static final String KEY_VIEW_HEIGHT = "anim_height";
    public static final String KEY_NEED_FINISH_SCALE_ANIM= "anim_need_finish_scale";
    public static final String KEY_NEED_FINISH_TRANS_ANIM= "anim_need_finish_trans";


    /**
     * Linos和SystemUI启动app调用此接口设置启动和finish时的动画参数
     * @param view 启动scale动画的位置
     * @param position View 在Lions 中的gridview中的position，用于修正缩放动画的piovt点
     * @param params SourceInfo中的map参数
     * */
    public static Bundle addScaleAnimParam(View view, int position , HashMap<String,String> params){
        if (view == null || params == null){
            return null;
        }
        int width = view.getWidth();
        int height = view.getHeight();
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        params.put(KEY_LOCATIONX,String.valueOf(location[0]));
        params.put(KEY_LOCATIONY,String.valueOf(location[1]));
        params.put(KEY_INDEX_IN_LINOS,String.valueOf(position));
        params.put(KEY_VIEW_WIDTH,String.valueOf(width));
        params.put(KEY_VIEW_HEIGHT,String.valueOf(height));
        params.put(KEY_NEED_FINISH_SCALE_ANIM,String.valueOf(true));

        return ActivityOptions.makeScaleUpAnimation(view,width/2,height/2,width,height).toBundle();
    }

    /**
     * 语音调用切源启动app时调用此接口设置启动和finish的动画参数
     * @param context
     * @param enterId
     * @param params
     * @return
     */
    public static Bundle addTransAnimParam(Context context, int enterId, HashMap<String,String> params){
        if (params == null){
            return null;
        }
        params.put(KEY_NEED_FINISH_TRANS_ANIM,String.valueOf(true));
        return ActivityOptions.makeCustomAnimation(context,enterId,R.anim.enter_anim).toBundle();
    }

}
