package camchua.genshinimpacttool.tasks;

import camchua.discordbot.DiscordBot;
import camchua.discordbot.data.UserData;
import camchua.discordbot.utils.DiscordUtils;
import camchua.genshinimpacttool.GenshinImpactTool;
import camchua.genshinimpacttool.dailycheckin.model.CheckIn;
import camchua.genshinimpacttool.dailycheckin.model.CheckInUser;
import camchua.genshinimpacttool.utils.Messages;

import java.lang.reflect.GenericArrayType;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimerTask;

public class AutoClaimTask extends TimerTask {

    private GenshinImpactTool tool;

    public AutoClaimTask(GenshinImpactTool tool) {
        this.tool = tool;
    }

    private int[] check_time = {3, 9, 15, 21};

    @Override
    public void run() {
        Date d = Calendar.getInstance().getTime();
        for(int hour : check_time) {
            if(d.getHours() == hour && d.getMinutes() == 0 && d.getSeconds() == 0) {
                for(String user_id : UserData.listId()) {
                    boolean auto = UserData.get(user_id, tool.getName() + "\\daily_checkin").getBoolean("auto", false);
                    if(!auto) continue;

                    String ltuid = UserData.getString(user_id, tool.getName() + "\\data", "ltuid");
                    String ltoken = UserData.getString(user_id, tool.getName() + "\\data", "ltoken");
                    if(ltuid.isEmpty() && ltoken.isEmpty()) continue;

                    try {
                        CheckInUser user = tool.getDailyCheckIn().getUser(ltuid, ltoken);
                        CheckIn check = user.checkIn();

                        if(!check.isSuccess()) {
                            if(check.getCode() != -5003) {
                                String msg = Messages.get("daily_checkin_error", user_id, null);
                                DiscordUtils.sendPrivateMessage(user_id, msg.replace("%error%", check.getMessage()));
                            }
                            continue;
                        }

                        HashMap<String, String> replace = new HashMap<>();
                        replace.put("%name%", check.getName());
                        replace.put("%amount%", String.valueOf(check.getAmount()));
                        String msg = Messages.get("daily_checkin_claim", user_id, replace);

                        DiscordUtils.sendPrivateMessage(user_id, msg);
                    } catch(Exception e) {
                        String msg = Messages.get("daily_checkin_error", user_id, null);
                        DiscordUtils.sendPrivateMessage(user_id, msg.replace("%error%", e.getMessage()));
                    }
                }
            }
        }
    }

}
