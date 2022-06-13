package camchua.genshinimpacttool.buttonlistener;

import camchua.discordbot.data.UserData;
import camchua.genshinimpacttool.GenshinImpactTool;
import camchua.genshinimpacttool.dailycheckin.model.CheckIn;
import camchua.genshinimpacttool.dailycheckin.model.CheckInUser;
import camchua.genshinimpacttool.utils.Messages;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;

public class DailyCheckinListener extends ListenerAdapter {

    private GenshinImpactTool tool;

    public DailyCheckinListener(GenshinImpactTool tool) {
        this.tool = tool;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String user_id = event.getUser().getId();

        String ltuid = UserData.getString(user_id, tool.getName() + "\\data", "ltuid");
        String ltoken = UserData.getString(user_id, tool.getName() + "\\data", "ltoken");
        String uid = UserData.getString(user_id, tool.getName() + "\\data", "uid");
        String authKey = UserData.getString(user_id, tool.getName() + "\\data", "auth_key");

        String component = event.getComponentId().split(":")[0];
        String author_id = event.getComponentId().split(":")[1];

        if(!user_id.equals(author_id)) {
            String msg = Messages.get("daily_checkin_not_action", user_id, null);
            event.reply(msg).queue();
            return;
        }

        switch(component) {
            case "dailycheckin_claim" -> {
                try {
                    CheckInUser user = tool.getDailyCheckIn().getUser(ltuid, ltoken);
                    CheckIn check = user.checkIn();

                    if(!check.isSuccess()) {
                        String msg = Messages.get("daily_checkin_error", user_id, null);
                        event.reply(msg.replace("%error%", check.getMessage())).queue();
                        return;
                    }

                    HashMap<String, String> replace = new HashMap<>();
                    replace.put("%name%", check.getName());
                    replace.put("%amount%", String.valueOf(check.getAmount()));
                    String msg = Messages.get("daily_checkin_claim", user_id, replace);

                    event.reply(msg).queue();
                    return;
                } catch(Exception e) {
                    String msg = Messages.get("daily_checkin_error", user_id, null);
                    event.reply(msg.replace("%error%", e.getMessage())).queue();
                    return;
                }
            }

            case "dailycheckin_autoclaim" -> {
                boolean auto = UserData.get(user_id, tool.getName() + "\\daily_checkin").getBoolean("auto", false);

                if(auto) {
                    UserData.set(user_id, tool.getName() + "\\daily_checkin", "auto", false);
                    String msg = Messages.get("daily_checkin_turn_off", user_id, null);
                    event.reply(msg).queue();
                } else {
                    UserData.set(user_id, tool.getName() + "\\daily_checkin", "auto", true);
                    String msg = Messages.get("daily_checkin_turn_on", user_id, null);
                    event.reply(msg).queue();
                }

                try {
                    CheckInUser user = tool.getDailyCheckIn().getUser(ltuid, ltoken);
                    CheckIn check = user.checkIn();

                    if(check.isSuccess()) {
                        HashMap<String, String> replace = new HashMap<>();
                        replace.put("%name%", check.getName());
                        replace.put("%amount%", String.valueOf(check.getAmount()));
                        String msg = Messages.get("daily_checkin_claim", user_id, replace);

                        event.reply(msg).queue();
                    }
                } catch(Exception e) {

                }

                return;
            }
        }
    }

}
