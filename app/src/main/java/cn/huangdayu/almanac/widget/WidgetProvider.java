package cn.huangdayu.almanac.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;
import cn.huangdayu.almanac.R;
import cn.huangdayu.almanac.activity.MainActivity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by lyl on 2017/8/23.
 */

public class WidgetProvider extends AppWidgetProvider {

    // 更新 widget 的广播对应的action
    private final String ACTION_UPDATE_ALL = "cn.huangdayu.almanac.widget.UPDATE_ALL";
    // 保存 widget 的id的HashSet，每新建一个 widget 都会为该 widget 分配一个 id。
    private static Set idsSet = new HashSet();

    public static int mIndex;

    String clickAction = "cn.huangdayu.almanac.widget.ON_CLICK";
    int i = 0;

    /**
     * 接收窗口小部件点击时发送的广播
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();

        if (ACTION_UPDATE_ALL.equals(action)) {
            // “更新”广播
            updateAllAppWidgets(context, AppWidgetManager.getInstance(context), idsSet);
        } else if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
            // “按钮点击”广播
            mIndex = 0;
            updateAllAppWidgets(context, AppWidgetManager.getInstance(context), idsSet);
        }
    }

    // 更新所有的 widget
    private void updateAllAppWidgets(Context context, AppWidgetManager appWidgetManager, Set set) {
        // widget 的id
        int appID;
        // 迭代器，用于遍历所有保存的widget的id
        Iterator it = set.iterator();

        // 要显示的那个数字，每更新一次 + 1
        mIndex++; // TODO:可以在这里做更多的逻辑操作，比如：数据处理、网络请求等。然后去显示数据

        while (it.hasNext()) {
            appID = ((Integer) it.next()).intValue();

            // 获取 example_appwidget.xml 对应的RemoteViews
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_listview);

            // 设置显示数字
            remoteView.setTextViewText(R.id.widget_txt, String.valueOf(mIndex));

            Intent svcIntent = new Intent(context, RemoteViewsService.class);

            //passing app widget id to that RemoteViews Service
            svcIntent.putExtra("Id", 1);
            Log.d("fff", "receiver:" + 1);
            svcIntent.putExtra("appname", "almanacApp");
            //setting a unique Uri to the intent
            //don't know its purpose to me right now
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            //setting adapter to listview of the widget

            remoteView.setRemoteAdapter(R.id.almanac_widget_listview, svcIntent);

            // 设置点击按钮对应的PendingIntent：即点击按钮时，发送广播。
            remoteView.setOnClickPendingIntent(R.id.widget_btn_before, getResetPendingIntent(context));
            remoteView.setOnClickPendingIntent(R.id.widget_btn_after, getOpenPendingIntent(context));

            // 更新 widget
            appWidgetManager.updateAppWidget(appID, remoteView);
        }
    }

    /**
     * 获取 重置数字的广播
     */
    private PendingIntent getResetPendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, WidgetProvider.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }

    /**
     * 获取 打开 MainActivity 的 PendingIntent
     */
    private PendingIntent getOpenPendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.putExtra("main", "这句话是我从桌面点开传过去的。");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return pendingIntent;
    }

    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法，可添加多次但只第一次调用
     */
    @Override
    public void onEnabled(Context context) {
        // 在第一个 widget 被创建时，开启服务
        Intent intent = new Intent(context, WidgetService.class);
        context.startService(intent);
        Toast.makeText(context, "开始计数", Toast.LENGTH_SHORT).show();
        super.onEnabled(context);
    }

    // 当 widget 被初次添加 或者 当 widget 的大小被改变时，被调用
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle
            newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * 当小部件从备份恢复时调用该方法
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    /**
     * 每次窗口小部件被点击更新都调用一次该方法
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // 每次 widget 被创建时，对应的将widget的id添加到set中
        for (int appWidgetId : appWidgetIds) {
            idsSet.add(Integer.valueOf(appWidgetId));
        }

        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);

        // 创建一个RemoteView
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_listview);

        // 把这个Widget绑定到RemoteViewsService
        Intent intent = new Intent(context, WidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);

        // 设置适配器
        remoteViews.setRemoteAdapter(R.id.almanac_widget_listview, intent);

        // 设置当显示的widget_list为空显示的View
        remoteViews.setEmptyView(R.id.almanac_widget_listview, R.layout.widget_nono_data);

        // 点击列表触发事件
        Intent clickIntent = new Intent(context, WidgetProvider.class);
        // 设置Action，方便在onReceive中区别点击事件
        clickIntent.setAction(clickAction);
        clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));

        PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setPendingIntentTemplate(R.id.almanac_widget_listview, pendingIntentTemplate);

        // 刷新按钮
        final Intent refreshIntent = new Intent(context, WidgetProvider.class);
        refreshIntent.setAction("refresh");
        final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_txt, refreshPendingIntent);

        // 更新Wdiget
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    /**
     * 每删除一次窗口小部件就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // 当 widget 被删除时，对应的删除set中保存的widget的id
        for (int appWidgetId : appWidgetIds) {
            idsSet.remove(Integer.valueOf(appWidgetId));
        }
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 当最后一个该窗口小部件删除时调用该方法，注意是最后一个
     */
    @Override
    public void onDisabled(Context context) {
        // 在最后一个 widget 被删除时，终止服务
        Intent intent = new Intent(context, WidgetService.class);
        context.stopService(intent);
        super.onDisabled(context);
    }
}
