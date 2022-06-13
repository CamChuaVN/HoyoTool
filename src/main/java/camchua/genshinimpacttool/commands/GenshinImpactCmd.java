package camchua.genshinimpacttool.commands;

import camchua.discordbot.DiscordBot;
import camchua.discordbot.data.UserData;
import camchua.discordbot.discord.DiscordCommandExecutor;
import camchua.discordbot.discord.DiscordSender;
import camchua.genshinimpacttool.GenshinImpactTool;
import camchua.genshinimpacttool.dailycheckin.model.CheckInUser;
import camchua.genshinimpacttool.utils.Messages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenshinImpactCmd extends DiscordCommandExecutor {

    private GenshinImpactTool tool;

    public GenshinImpactCmd(GenshinImpactTool tool) {
        this.tool = tool;
    }

    @Override
    public void execute(DiscordSender author, MessageChannel channel, MessageReceivedEvent event, String[] args) {
        if(args.length == 0) {
            channel.sendMessage("!<cmd>").queue();
            return;
        }

        String user_id = author.getUser().getId();

        String ltuid = UserData.getString(user_id, tool.getName() + "\\data", "ltuid");
        String ltoken = UserData.getString(user_id, tool.getName() + "\\data", "ltoken");
        String uid = UserData.getString(user_id, tool.getName() + "\\data", "uid");
        String authKey = UserData.getString(user_id, tool.getName() + "\\data", "auth_key");

        switch(args[0].toLowerCase()) {
            case "cookie" -> {
                String u = "";
                String t = "";

                try {
                    u = args[1];
                    t = args[2];
                } catch(Exception e) {
                    channel.sendMessage("Error Exception: " + e.getMessage()).queue();
                    return;
                }

                UserData.set(user_id, tool.getName() + "\\data", "ltuid", u);
                UserData.set(user_id, tool.getName() + "\\data", "ltoken", t);

                channel.sendMessage(Messages.get("cookie_set", user_id, null)).queue();
                return;
            }

            case "uid" -> {
                String id = "";

                try {
                    id = args[1];
                } catch(Exception e) {
                    channel.sendMessage("Error Exception: " + e.getMessage()).queue();
                    return;
                }

                UserData.set(user_id, tool.getName() + "\\data", "uid", id);

                channel.sendMessage(Messages.get("uid_set", user_id, null)).queue();
                return;
            }

            case "authkey" -> {
                String key = "";

                try {
                    key = args[1];
                } catch(Exception e) {
                    channel.sendMessage("Error Exception: " + e.getMessage()).queue();
                    return;
                }

                UserData.set(user_id, tool.getName() + "\\data", "auth_key", key);

                channel.sendMessage(Messages.get("authkey_set", user_id, null)).queue();
                return;
            }


            case "daily_checkin", "dailycheckin", "dc" -> {
                if(ltuid.isEmpty() && ltoken.isEmpty()) {
                    event.getMessage().reply(Messages.get("no_cookie", user_id, null)).queue();
                    return;
                }

                CheckInUser user;
                try {
                    user = tool.getDailyCheckIn().getUser(ltuid, ltoken);
                } catch(Exception e) {
                    event.getMessage().reply("Error Exception: " + e.getMessage()).queue();
                    return;
                }

                boolean auto = UserData.get(user_id, tool.getName() + "\\daily_checkin").getBoolean("auto", false);

                HashMap<String, String> replace = new HashMap<>();
                replace.put("%total_sign_day%", String.valueOf(user.getTotalSignDay()));
                replace.put("%today_claim%", user.isSign() ? "yes" : "no");
                replace.put("%server%", user.getRegion());
                replace.put("%today_name%", user.getTodayCheckIn().getName());
                replace.put("%today_amount%", String.valueOf(user.getTodayCheckIn().getAmount()));
                replace.put("%auto%", auto ? "on" : "off");

                String title = Messages.get("daily_checkin_title", user_id, null);
                String description = Messages.get("daily_checkin_description", user_id, replace);

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(DiscordBot.getColor());
                eb.setTitle(title);
                eb.setDescription(description);
                eb.setFooter(author.getUser().getAsTag(), author.getUser().getAvatarUrl());

                List<Button> buttons = new ArrayList<>();
                buttons.add(Button.primary("dailycheckin_claim:" + user_id, "Claim"));
                buttons.add(Button.primary("dailycheckin_autoclaim:" + user_id, "Auto Claim"));

                event.getMessage().replyEmbeds(eb.build()).setActionRow(buttons).queue();
                return;
            }


            default -> {
                channel.sendMessage("!<cmd>").queue();
                return;
            }

        }
    }

}
