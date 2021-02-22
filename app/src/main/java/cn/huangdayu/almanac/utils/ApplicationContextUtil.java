package cn.huangdayu.almanac.utils;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * @author huangdayu create at 2021/2/21 15:59
 */
public class ApplicationContextUtil {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        ApplicationContextUtil.context = context;
    }
}
