package cn.huangdayu.almanac;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * @author huangdayu create at 2021/2/16 20:03
 */
public class CustomizeListView extends ListView {

    public CustomizeListView(Context context) {
        super(context);
    }

    public CustomizeListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量的大小由一个32位的数字表示，前两位表示测量模式，后30位表示大小，这里需要右移两位才能拿到测量的大小
        int heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightSpec);
    }
}
