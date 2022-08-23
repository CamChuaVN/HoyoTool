package camchua.hoyotool.dailycheckin;

import camchua.hoyoapi.HoyoAPI;
import camchua.hoyotool.dailycheckin.model.CheckInUser;
import org.json.JSONObject;

import java.lang.reflect.Constructor;

public class DailyCheckIn {

    private DailyCheckIn() {

    }

    private String checkInUserClassName = "camchua.hoyotool.dailycheckin.model.CheckInUser";

    public CheckInUser getUser(String ltuid, String ltoken, String cookie_token) throws Exception {
        JSONObject info = new JSONObject(HoyoAPI.inst().genshin().getDailyRewardInfo(ltuid, ltoken, cookie_token, false));

        if(info.getInt("retcode") != 0) {
            throw new Exception("Error: " + info.getString("message"));
        }

        JSONObject data = info.getJSONObject("data");
        int totalSignDay = data.getInt("total_sign_day");
        boolean sign = data.getBoolean("is_sign");
        String region = data.getString("region");
        boolean sub = data.getBoolean("is_sub");
        boolean firstBind = data.getBoolean("first_bind");

        Class<?> clazz = Class.forName(this.checkInUserClassName);
        Constructor<?> ctor = clazz.getDeclaredConstructor(int.class, boolean.class, String.class, boolean.class, boolean.class, String.class, String.class, String.class);
        ctor.setAccessible(true);
        Object checkInUserClass = ctor.newInstance(totalSignDay, sign, region, sub, firstBind, ltuid, ltoken, cookie_token);
        ctor.setAccessible(false);

        return (CheckInUser) checkInUserClass;
    }

}
