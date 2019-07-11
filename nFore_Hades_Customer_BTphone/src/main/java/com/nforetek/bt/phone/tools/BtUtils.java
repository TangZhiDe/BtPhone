package com.nforetek.bt.phone.tools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.nforetek.bt.phone.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by tzd on 2019/3/12.
 */

public class BtUtils {
    private static String TAG = BtUtils.class.getCanonicalName();
    //进场动画
    public static Animation makeInAnimation(Context context, boolean fromLeft) {
        Animation a;
        if (fromLeft) {
            a = AnimationUtils.loadAnimation(context, R.anim.in_from_left);
        } else {
            a = AnimationUtils.loadAnimation(context, R.anim.in_from_right);
        }
        return a;
    }

    //出场动画
    public static Animation makeOutAnimation(Context context, boolean fromLeft) {
        Animation a;
        if (fromLeft) {
            a = AnimationUtils.loadAnimation(context, R.anim.out_to_left);
        } else {
            a = AnimationUtils.loadAnimation(context, R.anim.out_to_right);
        }
        return a;
    }

    public static void initAppLanguage(Context mContext) {
        Resources resources = mContext.getApplicationContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();

        Locale locale = config.locale;// getSetLocale方法是获取新设置的语言
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, dm);
    }


    public static void setlanguage(Context mContext) {
        //获取系统当前的语言
        String able = mContext.getApplicationContext().getResources().getConfiguration().locale.getLanguage();
        Resources resources = mContext.getApplicationContext().getResources();//获得res资源对象
        Configuration config = resources.getConfiguration();//获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();
        Log.e("TAG", "setlanguage: able==" + able);
        //根据系统语言进行设置
        if (able.equals("zh")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Log.e("TAG", "setlanguage:11 ");
                config.setLocale(Locale.SIMPLIFIED_CHINESE);
            } else {
                Log.e("TAG", "setlanguage:22 ");
                config.locale = Locale.SIMPLIFIED_CHINESE;
            }
            resources.updateConfiguration(config, dm);

        } else if (able.equals("en")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Log.e("TAG", "setlanguage:33 ");
                config.setLocale(Locale.US);
            } else {
                Log.e("TAG", "setlanguage:44 ");
                config.locale = Locale.US;
            }
            resources.updateConfiguration(config, dm);
        }
    }

    public static void finish(Activity activity){
        //&& !activity.isFinishing()
        if(activity != null && !activity.isFinishing()){
            Log.d(TAG, "finish: activity ="+activity);
            try {
                Method method = Activity.class.getDeclaredMethod("finish", int.class);
                method.setAccessible(true);
                method.invoke(activity, 0);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }


}
