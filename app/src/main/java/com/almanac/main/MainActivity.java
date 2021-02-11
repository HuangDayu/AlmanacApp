package com.almanac.main;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import cn.huangdayu.almanac.dto.AlmanacDTO;
import cn.huangdayu.almanac.dto.TimeZoneDTO;
import cn.huangdayu.almanac.utils.AlmanacUtils;
import cn.huangdayu.almanac.utils.ConstantsUtils;
import cn.huangdayu.almanac.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ListActivity {
    private ListView mListView = null;
    private ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
    private AlmanacDTO almanacDTO = null;
    private SharedPreferences sharedPreferences = null;
    private ClipboardManager clipboardManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mListView = super.getListView();
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("AlmanacSetting", Context.MODE_PRIVATE);
        //获取剪贴板管理器：
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        adapter(AlmanacUtils.dayCalendar(instantTimeBean()));
        //点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> itemMap = (HashMap<String, String>) mListView.getItemAtPosition(i);
                String title = itemMap.get("title").replace(":", "");
                String text = itemMap.get("text");
                internetDialog(title, text + "\n" + ConstantsUtils.getDesc(text));
            }
        });
        // 长按事件
        mListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            HashMap<String, String> itemMap = (HashMap<String, String>) mListView.getItemAtPosition(i);
            String title = itemMap.get("title").replace(":", "");
            String text = itemMap.get("text");
            // 将ClipData内容放到系统剪贴板里。
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Label", text));
            if (0 == i || 1 == i) {
                setDate(i, title, text);
            } else {
                Toast.makeText(this, "只有地区和西历可以长按修改！", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        super.onCreate(savedInstanceState);
    }

    private TimeZoneDTO instantTimeBean() {
        TimeZoneDTO timeZoneDTO = null;
        Date date = null;
        String province = null, area = null, westernCalendar = null, position = null;
        westernCalendar = sharedPreferences.getString("AlmanacWesternCalendar", "");//默认值是当前时间
        position = sharedPreferences.getString("AlmanacPosition", "");
        if (isAnyBlank(westernCalendar)) {
            date = new Date();
        } else {
            try {
                date = DateTimeUtils.toDate(westernCalendar);
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
            timeZoneDTO = new TimeZoneDTO(province, date);
        } catch (Exception e) {
            showToast("地区参数异常！");
            timeZoneDTO = new TimeZoneDTO("广东", "徐闻", new Date());
        }
        return timeZoneDTO;
    }

    /***
     * 适配器
     * @param almanacDTO
     */
    private void adapter(AlmanacDTO almanacDTO) {
        arrayList.clear();
        this.almanacDTO = almanacDTO;
        almanacDTO.toMap().forEach((K, V) -> {
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
                if (0 == i) {
                    editor.putString("AlmanacPosition", inValue);
                } else if (1 == i) {
                    editor.putString("AlmanacWesternCalendar", inValue);
                }
                //完成提交
                editor.commit();
                adapter(AlmanacUtils.dayCalendar(instantTimeBean()));
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
                editor.putString("AlmanacWesternCalendar", DateTimeUtils.getFormatDate("yyyy-MM-dd HH:mm:ss.SSS"));
                editor.putString("AlmanacPosition", "广东省 徐闻县");
                editor.commit();
                adapter(AlmanacUtils.dayCalendar(instantTimeBean()));
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

