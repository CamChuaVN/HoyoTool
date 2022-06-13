package camchua.genshinimpacttool.dailycheckin.model;

import camchua.genshinimpactapi.GenshinImpact;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class CheckReward {

    private static HashMap<Integer, CheckReward> reward = new HashMap<>();

    public static CheckReward get(int index) {
        return reward.get(index);
    }


    public static void init() {
        JSONObject r = new JSONObject(GenshinImpact.getAPI().getDailyRewardHome(false));

        reward.clear();
        JSONArray array = r.getJSONObject("data").getJSONArray("awards");
        for(int i = 0; i < array.length(); i++) {
            String name = array.getJSONObject(i).getString("name");
            int amount = array.getJSONObject(i).getInt("cnt");
            String icon = array.getJSONObject(i).getString("icon");
            reward.put(i, new CheckReward(name, amount, icon));
        }
    }


    private String name;
    private int amount;
    private String icon;

    private CheckReward(String name, int amount, String icon) {
        this.name = name;
        this.amount = amount;
        this.icon = icon;
    }

    public String getName() {
        return this.name;
    }

    public int getAmount() {
        return this.amount;
    }

    public String getIconUrl() {
        return this.icon;
    }

}
