package cn.huangdayu.almanac.activity;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import cn.huangdayu.almanac.R;
import cn.huangdayu.almanac.dto.AlmanacDTO;
import cn.huangdayu.almanac.dto.MoonPhaseDTO;
import cn.huangdayu.almanac.dto.SolarTermDTO;
import cn.huangdayu.almanac.dto.TimeZoneDTO;
import cn.huangdayu.almanac.impl.SolartermsBackgroundImpl;
import cn.huangdayu.almanac.utils.AlmanacUtils;
import cn.huangdayu.almanac.utils.ConstantsUtils;
import cn.huangdayu.almanac.utils.DateTimeUtils;
import cn.huangdayu.almanac.view.CustomizeListView;
import com.necer.calendar.BaseCalendar;
import com.necer.calendar.Miui10Calendar;
import com.necer.enumeration.CalendarState;
import com.necer.enumeration.CheckModel;
import com.necer.enumeration.DateChangeBehavior;
import com.necer.listener.OnCalendarChangedListener;
import com.necer.listener.OnCalendarMultipleChangedListener;
import com.necer.listener.OnClickDisableDateListener;
import com.necer.painter.CalendarBackground;
import com.necer.painter.InnerPainter;
import com.necer.painter.NumBackground;
import org.joda.time.LocalDate;

import java.util.*;

public class MainActivity extends BaseActivity {

    private final List<Map<String, Object>> arrayList = new ArrayList<>();

    /**
     * 获取剪贴板管理器
     */
    private ClipboardManager clipboardManager;
    private Miui10Calendar miui10Calendar;
    private InnerPainter innerPainter;
    private CustomizeListView listView;
    private SharedPreferences sharedPreferences;
    private Context context;
    private CalendarBackground numberBackground;
    private SolartermsBackgroundImpl solartermsBackground;
    private TimeZoneDTO timeZoneDTO;
    private AlmanacDTO almanacDTO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        sharedPreferences = getSharedPreferences("AlmanacSetting", Context.MODE_PRIVATE);
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        initListView();
        initCalendar();

        Map<String, String> strMap = new HashMap<>();
        strMap.put("1995-08-12", "生日快乐");
        strMap.put("2021-02-18", "摘辣椒");
        strMap.put("2021-08-24", "生日快乐");

        // 设置详细显示
        innerPainter.setStretchStrMap(strMap);

        // 设置标记
        innerPainter.setPointList(new ArrayList<>(strMap.keySet()));

    }

    private void initCalendar() {
        miui10Calendar = findViewById(R.id.miui10Calendar);
        miui10Calendar.setCheckMode(CheckModel.SINGLE_DEFAULT_CHECKED);
        miui10Calendar.setCalendarState(CalendarState.MONTH);
        miui10Calendar.setStretchCalendarEnable(true);
        innerPainter = (InnerPainter) miui10Calendar.getCalendarPainter();
        numberBackground = new NumBackground(miui10Calendar.getAttrs().numberBackgroundTextSize, miui10Calendar.getAttrs().numberBackgroundTextColor, miui10Calendar.getAttrs().numberBackgroundAlphaColor);
        solartermsBackground = new SolartermsBackgroundImpl(context, miui10Calendar);
        miui10Calendar.setOnClickDisableDateListener(new OnClickDisableDateListener() {
            @Override
            public void onClickDisableDate(LocalDate localDate) {
                Log.e(TAG, "不可用日期：" + localDate);
            }
        });

        final boolean[] cleanBackground = {true};

        /**
         * 单选
         */
        miui10Calendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarChange(BaseCalendar baseCalendar, int year, int month, LocalDate localDate, DateChangeBehavior dateChangeBehavior) {
                if (timeZoneDTO == null) {
                    timeZoneDTO = getTimeZoneDTO(true);
                } else {
                    Log.d(TAG, "选中日期: " + localDate);
                    timeZoneDTO.setYear(localDate.getYear());
                    timeZoneDTO.setMonth(localDate.getMonthOfYear());
                    timeZoneDTO.setDay(localDate.getDayOfMonth());
                    timeZoneDTO = new TimeZoneDTO(timeZoneDTO);
                }
                almanacDTO = AlmanacUtils.ofDay(timeZoneDTO);
                refreshAdapter();
                if (almanacDTO != null && almanacDTO.getSolarTermDTO() != null && almanacDTO.getSolarTermDTO().getAfterDay() != null && almanacDTO.getSolarTermDTO().getAfterDay() == 0) {
                    solartermsBackground.setIndex(almanacDTO.getSolarTermDTO().getIndex());
                    miui10Calendar.setMonthCalendarBackground(solartermsBackground);
                } else {
                    miui10Calendar.setMonthCalendarBackground(numberBackground);
                }
//                CalendarDate calendarDate = CalendarUtil.getCalendarDate(localDate);
//                Lunar lunar = calendarDate.lunar;
            }
        });
        /**
         * 多选
         */
        miui10Calendar.setOnCalendarMultipleChangedListener(new OnCalendarMultipleChangedListener() {
            @Override
            public void onCalendarChange(BaseCalendar baseCalendar, int year, int month, List<LocalDate> currPagerCheckedList, List<LocalDate> totalCheckedList, DateChangeBehavior dateChangeBehavior) {
                Log.d(TAG, "选中日期：" + currPagerCheckedList);
                Log.d(TAG, "全部选中：：" + totalCheckedList);
            }
        });
    }

    /**
     * 添加listview监听
     */
    private void initListView() {
        listView = findViewById(R.id.almanacListView);

        //点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> itemMap = (HashMap<String, String>) listView.getItemAtPosition(i);
                String title = itemMap.get("title").replaceAll(":", "").replaceAll(" ", "");
                String text = itemMap.get("text");
                String desc = ConstantsUtils.getDesc(title);
                if ("节气".equals(title)) {
                    desc = ConstantsUtils.getDesc(text.split(" ")[0]);
                }
                internetDialog(title, text + (isBlank(desc) ? "" : "\n" + desc));
            }
        });
        // 长按事件
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            HashMap<String, String> itemMap = (HashMap<String, String>) listView.getItemAtPosition(i);
            String title = itemMap.get("title").replaceAll(":", "").replaceAll(" ", "");
            String text = itemMap.get("text");
            setting(title, text);
            return true;
        });
        // 活动事件
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE: // 刷新完毕
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING: // 挑划产生刷新
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL: // 拖动产生刷新
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    View firstView = listView.getChildAt(0);
                    if (firstView != null && firstView.getTop() == 0) {
                        // 已经滚动到顶部了
                    } else {
                    }
                }

                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    View lastView = listView.getChildAt(listView.getChildCount() - 1);
                    if (lastView != null && lastView.getBottom() == listView.getHeight()) {
                        // 已经滚动到最底部了
                    } else {
                    }
                }
            }
        });
    }

    private void initAlmanac(boolean now) {
        timeZoneDTO = getTimeZoneDTO(now);
        almanacDTO = AlmanacUtils.ofDay(timeZoneDTO);
        refreshAdapter();
    }

    private TimeZoneDTO getTimeZoneDTO(boolean now) {
        try {
            Date date = new Date();
            String province = "广东省", area = "徐闻县";
            String westernCalendar = sharedPreferences.getString("AlmanacWesternCalendar", "");//默认值是当前时间
            String position = sharedPreferences.getString("AlmanacPosition", "");
            if (!isBlank(westernCalendar) && !now) {
                date = DateTimeUtils.toDate(westernCalendar);
            }
            if (!isBlank(position)) {
                province = position.split(" ")[0];
                area = position.split(" ")[1];
            }
            return new TimeZoneDTO(province, area, date);
        } catch (Exception e) {
            showToast("日期时间参数异常！");
        }
        return new TimeZoneDTO("广东省", "徐闻县", new Date());
    }

    /***
     * 刷新适配器
     */
    private void refreshAdapter() {
        arrayList.clear();
        almanacDTO.toMap().forEach((k, v) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("title", " " + k + " : ");
            item.put("text", v);
            arrayList.add(item);
        });
//        ArrayAdapter<Map<String, Object>> adapter = new ArrayAdapter<>(this,
//                R.layout.activity_almanac_item,
//                arrayList);
        SimpleAdapter adapter = new SimpleAdapter(this, arrayList,
                R.layout.activity_almanac_item, new String[]{"title", "text"}, new int[]{
                R.id.almanac_item_title, R.id.almanac_item_text});
        listView.setAdapter(adapter);
    }


    /**
     * 设置框弹窗
     *
     * @param title
     * @param text
     */
    public boolean setting(String title, String text) {
        List<String> keys = Arrays.asList("地点", "西历");
        if (!keys.contains(title)) {
            if ("节气".equals(title)) {
                StringBuilder solarTermText = new StringBuilder();
                for (SolarTermDTO solarTermDTO : almanacDTO.getSolarTermDTO().getNext()) {
                    solarTermText.append(solarTermDTO.getDetails()).append("\n");
                }
                internetDialog("往后节气", solarTermText.toString());
            } else if ("月相".equals(title)) {
                StringBuilder moonPhaseText = new StringBuilder();
                for (MoonPhaseDTO moonPhaseDTO : almanacDTO.getMoonPhaseDTO().getNext()) {
                    moonPhaseText.append(moonPhaseDTO.getDetails()).append("\n");
                }
                internetDialog("往后月相", moonPhaseText.toString());
            } else {
                String desc = ConstantsUtils.getDesc(title);
                internetDialog(title, text + (isBlank(desc) ? "" : "\n" + desc));
            }
            return false;
        }
        final Context context = this;
        EditText editText = new EditText(context);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        String westernCalendar = sharedPreferences.getString("AlmanacWesternCalendar", "");//默认值是空的
        String position = sharedPreferences.getString("AlmanacPosition", "");

        if ("地点".equals(title)) {
            alertDialog.setTitle("修改地点（省&&市/区/县）");
            editText.setText(!isAnyBlank(position) ? position : text);
        } else if ("西历".equals(title)) {
            alertDialog.setTitle("修改西历（年-月-日 时:分:秒.毫秒）");
            editText.setText(!isAnyBlank(westernCalendar) ? westernCalendar : DateTimeUtils.getFormatDate("yyyy-MM-dd HH:mm:ss.SSS"));
        }
        alertDialog.setCancelable(true);    //设置按钮是否可以按返回键取消,false则不可以取消
        //alertDialog.setCanceledOnTouchOutside(true); //设置弹出框失去焦点是否隐藏,即点击屏蔽其它地方是否隐藏
        alertDialog.setView(editText);
        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inValue = editText.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if ("地点".equals(title)) {
                    editor.putString("AlmanacPosition", inValue);
                } else if ("西历".equals(title)) {
                    editor.putString("AlmanacWesternCalendar", inValue);
                    miui10Calendar.jumpDate(inValue.split(" ")[0]);
                }
                //完成提交
                editor.commit();
                initAlmanac(false);
            }
        });
        alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setNeutralButton("重置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String inValue = DateTimeUtils.getFormatDate("yyyy-MM-dd HH:mm:ss.SSS");
                editor.putString("AlmanacWesternCalendar", inValue);
                editor.putString("AlmanacPosition", "广东省 徐闻县");
                editor.commit();
                miui10Calendar.jumpDate(inValue.split(" ")[0]);
                initAlmanac(false);
            }
        });
        alertDialog.show();
        return true;
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public void internetDialog(String title, String text) {
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setNegativeButton("复制", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 将ClipData内容放到系统剪贴板里。
                clipboardManager.setPrimaryClip(ClipData.newPlainText("Label", text));
                Toast.makeText(context, title + " 已复制到粘贴板！", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    //清除指定数据
    private void removeSetting(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    //清空数据
    private void clearSetting() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    /***
     * 多个字符串判空，一真则真
     * @param strings
     * @return
     */
    public static boolean isAnyBlank(String... strings) {
        for (String string : strings) {
            if (isBlank(string)) {
                return true;
            }
        }
        return false;
    }

    /***
     * org.apache.commons.lang3.StringUtils
     * @param cs
     * @return
     */
    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}

