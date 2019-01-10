package com.almanac.main;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.almanac.lunar.Almanac;
import com.almanac.lunar.AlmanacImpl;
import com.almanac.lunar.HistoricalYear;
import com.almanac.lunar.TimeBean;
import com.almanac.lunar.TimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends ListActivity {
    private ListView mListView = null;
    private ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>();
    private Almanac almanac = null;
    private SharedPreferences sharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mListView = super.getListView();
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("AlmanacSetting", Context.MODE_PRIVATE);
        adapter(new AlmanacImpl(instantTimeBean()));
        //点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> itemMap = (HashMap<String, String>) mListView.getItemAtPosition(i);
                String title = itemMap.get("title").replace(":", "");
                String text = itemMap.get("text");
                if (1 == i) {
                    internetDialog(title, almanac.getWesternCalendarCND().replace(" ", "\n"));
                } else if (8 == i) {
                    String s = text.split(" ")[2];
                    internetDialog(title, text + "，" + almanac.getSolarTermDoc(s));
                } else if (4 == i) {
                    internetDialog(title, "即中华历史朝代年号信息。\n" + HistoricalYear.getHY(getNH()));
                } else if (6 == i) {
                    internetDialog(title, "又称伊斯兰历。\n" + text);
                } else if (2 == i) {
                    internetDialog(title, "又称阴历。\n" + text);
                } else if (5 == i) {
                    internetDialog(title, "又称皇帝纪年。\n" + text);
                } else if (9 == i) {
                    internetDialog(title, text.replace(" ", "\n"));
                } else {
                    internetDialog(title, text);
                }
            }
        });
        mListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            HashMap<String, String> itemMap = (HashMap<String, String>) mListView.getItemAtPosition(i);
            String title = itemMap.get("title").replace(":", "");
            String text = itemMap.get("text");
            if (0 == i || 1 == i) {
                setDate(i, title, text);
            } else {
                Toast.makeText(this, "只有地区和西历可以长按修改！", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        super.onCreate(savedInstanceState);
    }

    private int getNH() {
        String s = null;
        s = sharedPreferences.getString("AlmanacWesternCalendar", "");
        if (!isBlank(s) && s.substring(0, 1).equals("-")) {
            s = s.split("-")[1];
        } else {
            s = almanac.getWesternCalendar().split("-")[0];
        }
        return Integer.parseInt(s);
    }

    private TimeBean instantTimeBean() {
        TimeBean timeBean = null;
        Date date = null;
        String province = null, area = null, westernCalendar = null, position = null;
        westernCalendar = sharedPreferences.getString("AlmanacWesternCalendar", "");//默认值是当前时间
        position = sharedPreferences.getString("AlmanacPosition", "");
        if (isAnyBlank(westernCalendar)) {
            date = new Date();
        } else {
            try {
                date = TimeUtil.toDate(westernCalendar);
            } catch (Exception e) {
                showToast("日期时间参数异常！");
                date = new Date();
            }
        }
        if (isAnyBlank(position)) {
            province = "广东省";
            area = "徐闻县";
        } else {
            province = position.split(" ")[0];
            area = position.split(" ")[1];
        }
        try {
            timeBean = new TimeBean(province, area, date);
        } catch (Exception e) {
            showToast("地区参数异常！");
            timeBean = new TimeBean("广东", "徐闻", new Date());
        }
        return timeBean;
    }

    /***
     * 适配器
     * @param almanac1
     */
    private void adapter(Almanac almanac1) {
        if (arrayList.size() > 0) {
            arrayList.clear();
        }
        this.almanac = almanac1;
        Map<String, String> dataMap = pakMap(almanac);
        dataMap.forEach((K, V) -> {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("title", " " + K + " : ");
            item.put("text", V);
            arrayList.add(item);
        });
        SimpleAdapter adapter = new SimpleAdapter(this, arrayList,
                R.layout.activity_main, new String[]{"title", "text"}, new int[]{
                R.id.title, R.id.text});
        super.setListAdapter(adapter);
    }

    private Map<String, String> pakMap(Almanac almanac) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        //map.put("日期", almanac.getDate());
        //map.put("时间", almanac.getTime());
        map.put("地区", almanac.getPosition());
        //map.put("星期", almanac.getWeek());
        map.put("西历", almanac.getWesternCalendarCN());
        map.put("农历", almanac.getLunar() + almanac.getLunarTime());
        //map.put("时辰", almanac.getLunarTime());
        map.put("黄历", almanac.getHuangLi());
        map.put("史历", almanac.getYearNumber());
        map.put("黄帝历", almanac.getChronology());//黄帝纪年
        //map.put("天干", almanac.getTianGan());
        //map.put("地支", almanac.getDiZhi());
        //map.put("八字", almanac.getBaZi());
        map.put("回历", almanac.getIslamic());//伊斯兰历
        map.put("儒略历", almanac.getJulianDay());
        map.put("节气", almanac.getNextSolarTerm());
        //map.put("生肖", almanac.getZodiac());
        map.put("节假日", almanac.getHolidayVacations());
        map.put("经度", almanac.getLongitude());
        map.put("纬度", almanac.getLatitude());
        map.put("时区", almanac.getTimeZone());
        map.put("港口", almanac.getPortName());
        map.put("昼长", almanac.getDiurnalTime());
        map.put("夜长", almanac.getNightTime());
        map.put("天亮", almanac.getDawnTime());
        map.put("日出", almanac.getSunriseTime());
        map.put("中天", almanac.getMidDayTime());
        map.put("日落", almanac.getSunsetTime());
        map.put("天黑", almanac.getDarkTime());
        map.put("月出", almanac.getMoonOutTime());
        map.put("月中", almanac.getMidMoonTime());
        map.put("月落", almanac.getMoonDownTime());
        map.put("月相", almanac.getMoonPhase());
        //map.put("月天数", almanac.getLunarDays());
        map.put("大月", almanac.isLunarBigMonth() + " " + almanac.getLunarDays());
        map.put("闰月", almanac.isLeapMonth());
        map.put("闰年", almanac.isLeapYear());
        map.put("星座", almanac.getConstellation());
        //map.put("指定节气", almanac.getSolarTerm("秋分"));
        //map.put("24节气", almanac.getAllSolarTerm()[23]);
        return map;
    }


    public void setDate(int i, String title, String text) {
        final Context context = this;
        EditText editText = new EditText(context);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        String westernCalendar = sharedPreferences.getString("AlmanacWesternCalendar", "");//默认值是空的
        String position = sharedPreferences.getString("AlmanacPosition", "");

        if (0 == i) {
            alertDialog.setTitle("修改地点（省&&市/区/县）");
            editText.setText(!isAnyBlank(position) ? position : text);
        } else if (1 == i) {
            alertDialog.setTitle("修改西历（年-月-日 时:分:秒.毫秒）");
            editText.setText(!isAnyBlank(westernCalendar) ? westernCalendar : almanac.getWesternCalendar());
        }
        alertDialog.setCancelable(true);    //设置按钮是否可以按返回键取消,false则不可以取消
        //alertDialog.setCanceledOnTouchOutside(true); //设置弹出框失去焦点是否隐藏,即点击屏蔽其它地方是否隐藏
        alertDialog.setView(editText);
        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inValue = editText.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (0 == i) {
                    editor.putString("AlmanacPosition", inValue);
                } else if (1 == i) {
                    editor.putString("AlmanacWesternCalendar", inValue);
                }
                //完成提交
                editor.commit();
                adapter(new AlmanacImpl(instantTimeBean()));
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
                editor.putString("AlmanacWesternCalendar", TimeUtil.getFormatDate("yyyy-MM-dd HH:mm:ss.SSS"));
                editor.putString("AlmanacPosition", "广东省 徐闻县");
                editor.commit();
                adapter(new AlmanacImpl(instantTimeBean()));
            }
        });
        alertDialog.show();
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    public void internetDialog(String title, String text) {
        final Context context = this;
        AlertDialog.Builder rconectDialog = new AlertDialog.Builder(context);
        rconectDialog.setTitle(title);
        rconectDialog.setMessage(text);
        rconectDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        rconectDialog.show();
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

