//package cn.huangdayu.almanac.utils;
//
//import android.appwidget.AppWidgetManager;
//import android.content.ComponentName;
//import android.widget.RemoteViews;
//import cn.huangdayu.almanac.R;
//import cn.huangdayu.almanac.widget.ListWidgetProvider;
//
///**
// * @author huangdayu create at 2021/2/21 15:51
// */
//public class RemoteViewUtil {
//    public static void updateAlmanacListWidgetDate() {
//        RemoteViews remoteViews = new RemoteViews(ApplicationContextUtil.getContext().getPackageName(), R.layout.widget_almanac_list);
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ApplicationContextUtil.getContext());
//        ComponentName componentName = new ComponentName(ApplicationContextUtil.getContext(), ListWidgetProvider.class);
//        appWidgetManager.updateAppWidget(componentName, remoteViews);
//
//    }
//}
