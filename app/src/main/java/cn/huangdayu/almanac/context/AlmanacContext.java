package cn.huangdayu.almanac.context;

import cn.huangdayu.almanac.dto.AlmanacDTO;
import cn.huangdayu.almanac.dto.TimeZoneDTO;
import cn.huangdayu.almanac.utils.AlmanacUtils;

/**
 * @author huangdayu create at 2021/2/22 11:27
 */
public class AlmanacContext {

    private static TimeZoneDTO timeZoneDTO;
    private static AlmanacDTO almanacDTO;


    public static TimeZoneDTO getTimeZoneDTO() {
        return timeZoneDTO;
    }

    public static void setTimeZoneDTO(TimeZoneDTO timeZoneDTO) {
        AlmanacContext.timeZoneDTO = timeZoneDTO;
        AlmanacContext.almanacDTO = AlmanacUtils.ofDay(timeZoneDTO);
    }

    public static AlmanacDTO getAlmanacDTO() {
        return almanacDTO;
    }


}
