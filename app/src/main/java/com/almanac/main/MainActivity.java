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
import com.almanac.lunar.TimeBean;
import com.almanac.lunar.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> itemMap = (HashMap<String, String>) mListView.getItemAtPosition(i);
                String title = itemMap.get("title").replace(":", "");
                String text = itemMap.get("text");
                if (0 == i || 1 == i || 2 == i) {
                    setDate(i, title, text);
                } else if (36 == i) {
                    String s = text.split(" ")[2];
                    internetDialog(title, text + "，" + almanac.getSolarTermDoc(s));
                } else {
                    internetDialog(title, text);
                }
            }
        });
        super.onCreate(savedInstanceState);
    }

    private TimeBean instantTimeBean() {
        TimeBean timeBean = null;
        String province = null, area = null, dateFormer = null, timeFormer = null;
        String date = sharedPreferences.getString("AlmanacDate", "");//默认值是空的
        String time = sharedPreferences.getString("AlmanacTime", "");
        String position = sharedPreferences.getString("AlmanacPosition", "");
        if (null != almanac) {
            province = almanac.getPosition().split(" ")[0];
            area = almanac.getPosition().split(" ")[1];
            dateFormer = almanac.getDateFormer();
            timeFormer = almanac.getTimeFormer();
        } else {
            province = "广东省";
            area = "徐闻县";
            dateFormer = TimeUtil.getFormatDate("yyyy-MM-dd");
            timeFormer = TimeUtil.getFormatDate("HH:mm:ss");
        }
        String code = null;
        try {
            if (!date.equals("") && !time.equals("") && !position.equals("")) {//111=7
                code = "111";
                timeBean = new TimeBean(position, date + " " + time);
            } else if (date.equals("") && !time.equals("") && !position.equals("")) {//011=6
                code = "011";
                timeBean = new TimeBean(position, dateFormer + " " + time);
            } else if (!date.equals("") && time.equals("") && !position.equals("")) {//101=5
                code = "101";
                timeBean = new TimeBean(position, date + " " + timeFormer);
            } else if (date.equals("") && time.equals("") && !position.equals("")) {//001=4
                code = "001";
                timeBean = new TimeBean(position, Calendar.getInstance());
            } else if (!date.equals("") && !time.equals("") && position.equals("")) {//110=3
                code = "110";
                timeBean = new TimeBean(province, area, date, time);
            } else if (date.equals("") && !time.equals("") && position.equals("")) {//010=2
                code = "010";
                timeBean = new TimeBean(province, area, dateFormer, time);
            } else if (!date.equals("") && time.equals("") && position.equals("")) {//100=1
                code = "100";
                timeBean = new TimeBean(province, area, date, timeFormer);
            } else {//000=0
                code = "000";
                timeBean = new TimeBean(province, area, Calendar.getInstance());
            }
        } catch (Exception e) {
            showToast("参数异常,code:" + code);
            timeBean = new TimeBean(province, area, Calendar.getInstance());
        }
        return timeBean;
    }

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
        map.put("日期", almanac.getDate());
        map.put("时间", almanac.getTime());
        map.put("地点", almanac.getPosition());
        map.put("星期", almanac.getWeek());
        map.put("年号", almanac.getYearNumber());
        map.put("农历", almanac.getLunar());
        map.put("时辰", almanac.getLunarTime());
        map.put("黄历", almanac.getHuangLi());
        map.put("天干", almanac.getTianGan());
        map.put("地支", almanac.getDiZhi());
        map.put("八字", almanac.getBaZi());
        map.put("回历", almanac.getIslamic());
        map.put("儒略日", almanac.getJulianDay());
        map.put("黄帝纪年", almanac.getChronology());
        map.put("生肖", almanac.getZodiac());
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
        map.put("月天数", almanac.getLunarDays());
        map.put("大月否", almanac.isLunarBigMonth());
        map.put("闰月否", almanac.isLeapMonth());
        map.put("闰年否", almanac.isLeapYear());
        map.put("星座", almanac.getConstellation());
        map.put("节气", almanac.getNextSolarTerm());
        //map.put("指定节气", almanac.getSolarTerm("秋分"));
        //map.put("24节气", almanac.getAllSolarTerm()[23]);
        return map;
    }


    public void setDate(int i, String title, String text) {
        final Context context = this;
        EditText editText = new EditText(context);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        String date = sharedPreferences.getString("AlmanacDate", "");//默认值是空的
        String time = sharedPreferences.getString("AlmanacTime", "");
        String position = sharedPreferences.getString("AlmanacPosition", "");

        if (0 == i) {
            alertDialog.setTitle("修改日期（年-月-日）");
            editText.setText(!date.equals("") ? date : almanac.getDateFormer());
        } else if (1 == i) {
            alertDialog.setTitle("修改时间（时:分:秒.毫秒）");
            editText.setText(!time.equals("") ? time : almanac.getTimeFormer());
        } else {
            alertDialog.setTitle("修改地点（省&&市/区/县）");
            editText.setText(!position.equals("") ? position : text);
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
                    editor.putString("AlmanacDate", inValue);
                } else if (1 == i) {
                    editor.putString("AlmanacTime", inValue);
                } else if (2 == i) {
                    editor.putString("AlmanacPosition", inValue);
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
        alertDialog.setNeutralButton("清空", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editText.setText("");
                if (0 == i) {
                    removeSetting("AlmanacDate");
                } else if (1 == i) {
                    removeSetting("AlmanacTime");
                } else if (2 == i) {
                    removeSetting("AlmanacPosition");
                }
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

}

