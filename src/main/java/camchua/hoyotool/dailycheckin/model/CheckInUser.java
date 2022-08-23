package camchua.hoyotool.dailycheckin.model;

import camchua.hoyoapi.HoyoAPI;
import org.json.JSONObject;

import java.lang.reflect.Constructor;

public class CheckInUser {
    private int totalSignDay;
    private boolean sign;
    private String region;
    private boolean sub;
    private boolean firstBind;
    private String ltuid = "";
    private String ltoken = "";
    private String cookie_token = "";

    private CheckInUser(int totalSignDay, boolean sign, String region, boolean sub, boolean firstBind, String ltuid, String ltoken, String cookie_token) {
        this.totalSignDay = totalSignDay;
        this.sign = sign;
        this.region = region;
        this.sub = sub;
        this.firstBind = firstBind;
        this.ltuid = ltuid;
        this.ltoken = ltoken;
        this.cookie_token = cookie_token;
    }

    public int getTotalSignDay() {
        return this.totalSignDay;
    }

    public boolean isSign() {
        return this.sign;
    }

    public String getRegion() {
        return this.region;
    }

    public boolean isSub() {
        return this.sub;
    }

    public boolean isFirstBind() {
        return this.firstBind;
    }

    public String getLtuid() {
        return this.ltuid;
    }

    public String getLtoken() {
        return this.ltoken;
    }


    private String checkInClassName = "camchua.hoyotool.dailycheckin.model.CheckIn";

    public CheckIn checkIn() throws Exception {
        JSONObject checkIn = new JSONObject(HoyoAPI.inst().genshin().getDailyRewardSign(ltuid, ltoken, cookie_token, false));
        if(checkIn.getInt("retcode") != 0) {
            int date = sign ? totalSignDay : totalSignDay + 1;
            CheckReward reward = this.getTodayCheckIn();

            Class<?> clazz = Class.forName(this.checkInClassName);
            Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, int.class, String.class, boolean.class, String.class, int.class);
            ctor.setAccessible(true);
            Object checkClass = ctor.newInstance(reward.getName(), reward.getAmount(), reward.getIconUrl(), false, checkIn.getString("message"), checkIn.getInt("retcode"));
            ctor.setAccessible(false);
            return (CheckIn) checkClass;
        } else {
            sign = true;
            this.totalSignDay += 1;
            int date = sign ? totalSignDay : totalSignDay + 1;
            CheckReward reward = this.getTodayCheckIn();

            Class<?> clazz = Class.forName(this.checkInClassName);
            Constructor<?> ctor = clazz.getDeclaredConstructor(String.class, int.class, String.class, boolean.class, String.class, int.class);
            ctor.setAccessible(true);
            Object checkClass = ctor.newInstance(reward.getName(), reward.getAmount(), reward.getIconUrl(), true, "", 0);
            ctor.setAccessible(false);
            return (CheckIn) checkClass;
        }
    }

    public CheckReward getTodayCheckIn() {
        int date = sign ? totalSignDay : totalSignDay + 1;
        return CheckReward.get(date - 1);
    }

}
