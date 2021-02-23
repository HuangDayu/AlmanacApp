package cn.huangdayu.almanac.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import cn.huangdayu.almanac.R;
import cn.huangdayu.almanac.context.AlmanacContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangdayu create at 2021/2/22 10:59
 * @sample <a>https://github.com/Tamicer/ListWidget</a>
 */
public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;

    public WidgetFactory(Context context, Intent intent) {
        this.context = context;
    }

    /**
     * MyRemoteViewsFactory调用时执行，这个方法执行时间超过20秒回报错。
     * 如果耗时长的任务应该在onDataSetChanged或者getViewAt中处理
     */
    @Override
    public void onCreate() {

    }

    /**
     * 当调用notifyAppWidgetViewDataChanged方法时，触发这个方法
     * 例如：MyRemoteViewsFactory.notifyAppWidgetViewDataChanged();
     */
    @Override
    public void onDataSetChanged() {

    }

    /**
     * 这个方法不用多说了把，这里写清理资源，释放内存的操作
     */
    @Override
    public void onDestroy() {
        AlmanacContext.getAdapters().clear();
    }

    /**
     * 返回集合数量
     */
    @Override
    public int getCount() {
        return AlmanacContext.getAdapters().size();
    }

    /**
     * 创建并且填充，在指定索引位置显示的View，这个和BaseAdapter的getView类似
     */
    @Override
    public RemoteViews getViewAt(int position) {
        if (position < 0 || position >= AlmanacContext.getAdapters().size()) {
            return null;
        }
        Map<String, String> content = AlmanacContext.getAdapters().get(position);
        // 创建在当前索引位置要显示的View
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.activity_almanac_item);

        // 设置要显示的内容
        remoteViews.setTextViewText(R.id.almanac_item_title, content.get("title"));
        remoteViews.setTextViewText(R.id.almanac_item_text, content.get("text"));

        return remoteViews;
    }

    /**
     * 显示一个"加载"View。返回null的时候将使用默认的View
     */
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    /**
     * 不同View定义的数量。默认为1
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     * 返回当前索引的。
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 如果每个项提供的ID是稳定的，即她们不会在运行时改变，就返回true
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }
}
