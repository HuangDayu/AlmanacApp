package cn.huangdayu.almanac.impl;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import cn.huangdayu.almanac.R;
import com.necer.calendar.Miui10Calendar;
import com.necer.drawable.TextDrawable;
import com.necer.painter.CalendarBackground;
import com.necer.painter.NumBackground;
import org.joda.time.LocalDate;

/**
 * @author huangdayu create at 2021/2/18 19:27
 */
public class SolartermsBackgroundImpl extends NumBackground implements CalendarBackground {

    private int index = -1;
    private Context context;
    private Miui10Calendar miui10Calendar;

    public SolartermsBackgroundImpl(Context context, Miui10Calendar miui10Calendar) {
        super(miui10Calendar.getAttrs().numberBackgroundTextSize, miui10Calendar.getAttrs().numberBackgroundTextColor, miui10Calendar.getAttrs().numberBackgroundAlphaColor);
        this.context = context;
        this.miui10Calendar = miui10Calendar;
    }

    @Override
    public Drawable getBackgroundDrawable(LocalDate localDate, int currentDistance, int totalDistance) {
        Drawable drawable = null;
        if (index >= 0 && index <= 23) {
            ApplicationInfo appInfo = context.getApplicationInfo();
            int resId = context.getResources().getIdentifier("solarterms_" + index, "drawable", appInfo.packageName);
            drawable = ContextCompat.getDrawable(context, resId);
            drawable.setBounds(100, 100, 100, 100);
        }
        if (drawable == null) {
            return super.getBackgroundDrawable(localDate, currentDistance, totalDistance);
        }
        return drawable;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
