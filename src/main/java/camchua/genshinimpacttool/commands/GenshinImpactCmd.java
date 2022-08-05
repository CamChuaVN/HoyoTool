package camchua.genshinimpacttool.commands;

import camchua.discordbot.DiscordBot;
import camchua.discordbot.data.UserData;
import camchua.discordbot.discord.DiscordCommandExecutor;
import camchua.discordbot.discord.DiscordSender;
import camchua.genshinimpactapi.GenshinImpact;
import camchua.genshinimpactapi.data.user.model.Avatar;
import camchua.genshinimpactapi.data.user.model.Player;
import camchua.genshinimpactapi.data.user.model.item.Reliquaries;
import camchua.genshinimpactapi.data.user.model.item.Weapon;
import camchua.genshinimpacttool.GenshinImpactTool;
import camchua.genshinimpacttool.dailycheckin.model.CheckInUser;
import camchua.genshinimpacttool.utils.Messages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenshinImpactCmd extends DiscordCommandExecutor {

    @Override
    public final String getDescription() {
        return "Genshin Impact Tool";
    }


    private GenshinImpactTool tool;

    public GenshinImpactCmd(GenshinImpactTool tool) {
        this.tool = tool;
    }

    @Override
    public void execute(DiscordSender author, MessageChannel channel, SlashCommandInteractionEvent event, String[] args) {
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
                    event.reply(Messages.get("no_cookie", user_id, null)).queue();
                    return;
                }

                CheckInUser user;
                try {
                    user = tool.getDailyCheckIn().getUser(ltuid, ltoken);
                } catch(Exception e) {
                    event.reply("Error Exception: " + e.getMessage()).queue();
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

                event.replyEmbeds(eb.build()).addActionRow(buttons).queue();
                return;
            }

            case "mycharacter", "mychar", "mc" -> {
                if(ltuid.isEmpty() && ltoken.isEmpty()) {
                    event.reply(Messages.get("no_cookie", user_id, null)).queue();
                    return;
                }
                if(uid.isEmpty()) {
                    event.reply(Messages.get("no_uid", user_id, null)).queue();
                    return;
                }
                if(args.length == 1) {
                    event.reply(Messages.get("mycharacter_require_input", user_id, null)).queue();
                    return;
                }

                GenshinImpact.inst().setCookie(ltoken, ltuid);
                Player genshinPlayer = GenshinImpact.getAPI().getPlayer(uid, false);
                GenshinImpact.inst().resetCookie();

                StringBuilder sb = new StringBuilder();
                for(int i = 1; i < args.length; i++) {
                    sb.append(args[i]);
                    if(i < args.length - 1) sb.append(" ");
                }

                String input = sb.toString().toLowerCase();
                for(Avatar avatar : genshinPlayer.getAvatars()) {
                    if(avatar.getName().toLowerCase().contains(input)) {
                        HashMap<String, String> replace = new HashMap<>();
                        replace.put("%name%", avatar.getName());
                        replace.put("%element%", avatar.getElement().getKey());
                        replace.put("%rarity%", String.valueOf(avatar.getRarity()));
                        replace.put("%level%", String.valueOf(avatar.getLevel()));
                        replace.put("%fetter_level%", String.valueOf(avatar.getFetter()));
                        replace.put("%constellations%", String.valueOf(avatar.getConstellations()));

                        Weapon weapon = avatar.getWeapon();
                        replace.put("%weapon_name%", weapon.getName());
                        replace.put("%weapon_level%", String.valueOf(weapon.getLevel()));
                        replace.put("%weapon_rarity%", String.valueOf(weapon.getRarity()));
                        replace.put("%weapon_refine%", String.valueOf(weapon.getAffixLevel()));

                        Reliquaries reliquaries = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.FLOWER_OF_LIFE);
                        replace.put("%r1_name%", reliquaries == null ? "No Equip" : reliquaries.getName());
                        replace.put("%r1_level%", reliquaries == null ? "0": String.valueOf(reliquaries.getLevel()));

                        reliquaries = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.PLUME_OF_DEATH);
                        replace.put("%r2_name%", reliquaries == null ? "No Equip" : reliquaries.getName());
                        replace.put("%r2_level%", reliquaries == null ? "0": String.valueOf(reliquaries.getLevel()));

                        reliquaries = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.SANDS_OF_EON);
                        replace.put("%r3_name%", reliquaries == null ? "No Equip" : reliquaries.getName());
                        replace.put("%r3_level%", reliquaries == null ? "0": String.valueOf(reliquaries.getLevel()));

                        reliquaries = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.GOBLET_OF_EONOTHEM);
                        replace.put("%r4_name%", reliquaries == null ? "No Equip" : reliquaries.getName());
                        replace.put("%r4_level%", reliquaries == null ? "0": String.valueOf(reliquaries.getLevel()));

                        reliquaries = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.CIRCLET_OF_LOGOS);
                        replace.put("%r5_name%", reliquaries == null ? "No Equip" : reliquaries.getName());
                        replace.put("%r5_level%", reliquaries == null ? "0": String.valueOf(reliquaries.getLevel()));

                        String title = Messages.get("mycharacter_title", user_id, null);
                        String description = Messages.get("mycharacter_description", user_id, replace);

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(DiscordBot.getColor());
                        eb.setTitle(title);
                        eb.setDescription(description);
                        eb.setFooter(author.getUser().getAsTag(), author.getUser().getAvatarUrl());
                        eb.setThumbnail(avatar.getIcon());

                        event.replyEmbeds(eb.build()).queue();
                        return;
                    }
                }

                event.reply(Messages.get("mycharacter_no_char", user_id, null)).queue();
                return;
            }


            default -> {
                channel.sendMessage("!<cmd>").queue();
                return;
            }

        }
    }

}
