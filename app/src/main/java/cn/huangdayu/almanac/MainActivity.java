package cn.huangdayu.almanac;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.*;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.*;

import cn.huangdayu.almanac.dto.AlmanacDTO;
import cn.huangdayu.almanac.dto.MoonPhaseDTO;
import cn.huangdayu.almanac.dto.SolarTermDTO;
import cn.huangdayu.almanac.dto.TimeZoneDTO;
import cn.huangdayu.almanac.utils.AlmanacUtils;
import cn.huangdayu.almanac.utils.ConstantsUtils;
import cn.huangdayu.almanac.utils.DateTimeUtils;
import cn.huangdayu.almanac.R;

import java.util.*;

public class MainActivity extends ListActivity {
    private ListView mListView = null;
    private final ArrayList<Map<String, Object>> arrayList = new ArrayList<>();
    private AlmanacDTO[] almanacDTOS = null;
    private SharedPreferences sharedPreferences = null;
    private ClipboardManager clipboardManager = null;
    private boolean top = false;
    private int dayIndex = 0;
    private AlmanacDTO almanacDTO = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mListView = super.getListView();
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("AlmanacSetting", Context.MODE_PRIVATE);
        //获取剪贴板管理器：
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        update(true);
        //点击事件
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                HashMap<String, String> itemMap = (HashMap<String, String>) mListView.getItemAtPosition(i);
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
        mListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            HashMap<String, String> itemMap = (HashMap<String, String>) mListView.getItemAtPosition(i);
            String title = itemMap.get("title").replaceAll(":", "").replaceAll(" ", "");
            String text = itemMap.get("text");
            // 将ClipData内容放到系统剪贴板里。
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Label", text));
            setting(title, text);
            Toast.makeText(this, title + " 已复制到粘贴板！", Toast.LENGTH_SHORT).show();
            return true;
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int before = 0, after = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE: // 刷新完毕
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING: // 挑划产生刷新
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL: // 拖动产生刷新
                        if (top) {
                            before++;
                            after = 0;
                            if (before >= 2) {
                                before = 0;
                                showToast("前一天！");
                                dayIndex--;
                                almanacDTO = almanacDTOS[dayIndex];
                                if (almanacDTO != null) {
                                    refreshAdapter();
                                }
                            }
                        } else {
                            after++;
                            before = 0;
                            if (after >= 2) {
                                after = 0;
                                showToast("后一天！");
                                dayIndex++;
                                almanacDTO = almanacDTOS[dayIndex];
                                if (almanacDTO != null) {
                                    refreshAdapter();
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    View firstView = mListView.getChildAt(0);
                    if (firstView != null && firstView.getTop() == 0) {
                        // 已经滚动到顶部了
                        top = true;
                    } else {
                        before = 0;
                        after = 0;
                    }
                }

                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    View lastView = mListView.getChildAt(mListView.getChildCount() - 1);
                    if (lastView != null && lastView.getBottom() == mListView.getHeight()) {
                        // 已经滚动到最底部了
                        top = false;
                    } else {
                        before = 0;
                        after = 0;
                    }
                }
            }
        });
        super.onCreate(savedInstanceState);
    }

    private void update(boolean now) {
        TimeZoneDTO timeZoneDTO = getTimeZoneDTO(now);
        almanacDTOS = AlmanacUtils.monthCalendar(timeZoneDTO);
        dayIndex = timeZoneDTO.getCalendar().get(Calendar.DATE) - 1;
        almanacDTO = almanacDTOS[dayIndex];
        refreshAdapter();
    }

    private TimeZoneDTO getTimeZoneDTO(boolean now) {
        Date date = new Date();
        String province = "广东省", area = "徐闻县";
        String westernCalendar = sharedPreferences.getString("AlmanacWesternCalendar", "");//默认值是当前时间
        String position = sharedPreferences.getString("AlmanacPosition", "");
        if (!isBlank(westernCalendar) && !now) {
            try {
                date = DateTimeUtils.toDate(westernCalendar);
            } catch (Exception e) {
                showToast("日期时间参数异常！");
            }
        }
        if (!isBlank(position)) {
            try {
                province = position.split(" ")[0];
                area = position.split(" ")[1];
            } catch (Exception e) {
                showToast("地区参数异常！");
            }
        }
        return new TimeZoneDTO(province, area, date);
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
        SimpleAdapter adapter = new SimpleAdapter(this, arrayList,
                R.layout.activity_main, new String[]{"title", "text"}, new int[]{
                R.id.title, R.id.text});
        super.setListAdapter(adapter);
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
                }
                //完成提交
                editor.commit();
                update(false);
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
                update(false);
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
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
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

