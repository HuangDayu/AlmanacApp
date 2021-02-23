package cn.huangdayu.almanac.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.Toast;
import cn.huangdayu.almanac.R;
import cn.huangdayu.almanac.activity.MainActivity;
import cn.huangdayu.almanac.context.AlmanacContext;
import cn.huangdayu.almanac.dto.TimeZoneDTO;

import java.util.HashSet;
import java.util.Set;

/**
 * @author huangdayu create at 2021/2/22 10:59
 */
public class WidgetProvider extends AppWidgetProvider {

    public static final String ACTION_TODAY = "cn.huangdayu.almanac.widget.TODAY",
            ACTION_AFTER_DAY = "cn.huangdayu.almanac.widget.AFTER_DAY",
            ACTION_BEFORE_DAY = "cn.huangdayu.almanac.widget.BEFORE_DAY",
            ACTION_ITEM_ONCLICK = "cn.huangdayu.almanac.widget.ITEM_ONCLICK";

    private static final Set<Integer> WIDGET_IDS = new HashSet<>();


    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();
        Toast.makeText(context, action, Toast.LENGTH_SHORT).show();
        switch (action) {
            case ACTION_BEFORE_DAY:
                TimeZoneDTO timeZoneDTO = AlmanacContext.getTimeZoneDTO();
                timeZoneDTO.setDay(timeZoneDTO.getDay() - 1);
                AlmanacContext.setTimeZoneDTO(new TimeZoneDTO(timeZoneDTO));
                refreshWidget(context);
                break;
            case ACTION_TODAY:
                break;
            case ACTION_AFTER_DAY:
                TimeZoneDTO timeZoneDTO1 = AlmanacContext.getTimeZoneDTO();
                timeZoneDTO1.setDay(timeZoneDTO1.getDay() + 1);
                AlmanacContext.setTimeZoneDTO(new TimeZoneDTO(timeZoneDTO1));
                refreshWidget(context);
                break;
            default:
                break;
        }
    }

    private void refreshWidget(Context context) {
        // 刷新Widget
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final ComponentName componentName = new ComponentName(context, WidgetProvider.class);

//        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_almanac_layout);
//        remoteViews.setTextViewText(R.id.widget_txt_today, AlmanacContext.getAlmanacDTO().getTimeZoneDTO().getDateInfo());

        // 会调用WidgetFactory中RemoteViewsFactory的onDataSetChanged()方法
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.widget_listview);
    }


    @Override
    public void onEnabled(Context context) {
        // 在第一个 widget 被创建时，开启服务
        Intent intent = new Intent(context, WidgetService.class);
        context.startService(intent);
        super.onEnabled(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle
            newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }


    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // 每次 widget 被创建时，对应的将widget的id添加到set中
        for (int appWidgetId : appWidgetIds) {
            WIDGET_IDS.add(appWidgetId);
        }

        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);

        // 创建一个RemoteView
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_almanac_layout);

        // 把这个Widget绑定到RemoteViewsService
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        // 设置适配器
        remoteViews.setRemoteAdapter(R.id.widget_listview, intent);
        // 设置当显示的widget_list为空显示的View
        remoteViews.setEmptyView(R.id.widget_listview, R.layout.widget_nono_data);

        // listview 点击
        Intent clickIntent = new Intent(context, WidgetProvider.class);
        clickIntent.setAction(ACTION_ITEM_ONCLICK);
        clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.widget_listview, pendingIntentTemplate);

        // 日期textview点击
        remoteViews.setOnClickPendingIntent(R.id.widget_txt_today, getTodayPendingIntent(context));
        // 昨天button点击
        remoteViews.setOnClickPendingIntent(R.id.widget_btn_before, PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_BEFORE_DAY, null, context, WidgetProvider.class), PendingIntent.FLAG_UPDATE_CURRENT));
        // 明天button点击
        remoteViews.setOnClickPendingIntent(R.id.widget_btn_after, PendingIntent.getBroadcast(context, 0,
                new Intent(ACTION_AFTER_DAY, null, context, WidgetProvider.class), PendingIntent.FLAG_UPDATE_CURRENT));

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    private PendingIntent getTodayPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ACTION_TODAY);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.putExtra("dateTime", "2021-02-22");
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            WIDGET_IDS.remove(appWidgetId);
        }
        super.onDeleted(context, appWidgetIds);
    }


    @Override
    public void onDisabled(Context context) {
        // 在最后一个 widget 被删除时，终止服务
        Intent intent = new Intent(context, WidgetService.class);
        context.stopService(intent);
        super.onDisabled(context);
    }
}
