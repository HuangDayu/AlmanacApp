package cn.huangdayu.almanac.context;

import cn.huangdayu.almanac.dto.AlmanacDTO;
import cn.huangdayu.almanac.dto.TimeZoneDTO;
import cn.huangdayu.almanac.utils.AlmanacUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangdayu create at 2021/2/22 11:27
 */
public class AlmanacContext {

    private static TimeZoneDTO timeZoneDTO;
    private static AlmanacDTO almanacDTO;
    private final static List<Map<String, String>> ADAPTERS = new ArrayList<>();



    public static TimeZoneDTO getTimeZoneDTO() {
        return timeZoneDTO;
    }

    public static void setTimeZoneDTO(TimeZoneDTO timeZoneDTO) {
        AlmanacContext.timeZoneDTO = timeZoneDTO;
        AlmanacContext.almanacDTO = AlmanacUtils.ofDay(timeZoneDTO);
        ADAPTERS.clear();
        AlmanacContext.getAlmanacDTO().toMap().forEach((k, v) -> {
            Map<String, String> item = new HashMap<>();
            item.put("title", " " + k + " : ");
            item.put("text", v);
            ADAPTERS.add(item);
        });
    }

    public static AlmanacDTO getAlmanacDTO() {
        return almanacDTO;
    }

    public static List<Map<String, String>> getAdapters() {
        return ADAPTERS;
    }


}
