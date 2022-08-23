package camchua.hoyotool.commands;

import camchua.discordbot.DiscordBot;
import camchua.discordbot.bot.user.DiscordUser;
import camchua.discordbot.command.Command;
import camchua.discordbot.command.CommandHandler;
import camchua.discordbot.manager.DataBaseManager;
import camchua.discordbot.utils.DiscordUtils;
import camchua.hoyoapi.HoyoAPI;
import camchua.hoyoapi.api.genshin.data.cdkey.CdkeyRedeem;
import camchua.hoyoapi.api.genshin.data.user.Player;
import camchua.hoyoapi.api.genshin.data.user.model.Avatar;
import camchua.hoyoapi.api.genshin.data.user.model.item.Reliquaries;
import camchua.hoyoapi.api.genshin.data.user.model.item.Weapon;
import camchua.hoyotool.HoyoTool;
import camchua.hoyotool.dailycheckin.model.CheckInUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Command(command = "genshinimpact", aliases = {"gitool"}, description = "Tool for Genshin Impact game")
public class GenshinImpactCmd implements CommandHandler {


    private HoyoTool tool;

    public GenshinImpactCmd(HoyoTool tool) {
        this.tool = tool;
    }

    @Override
    public void execute(DiscordUser sender, List<String> args, SlashCommandInteractionEvent event) {
        if(args.size() == 0) {
            def(sender, event);
            return;
        }

        String ltuid = "", ltoken = "", cookie_token = "", uid = "", authKey = "";

        String userId = sender.getAsUser().getId();
        DataBaseManager dataBase = DiscordBot.getDataBaseManager();
        dataBase.createTable("GITool_HoyoLab_data", "userId CHAR(64), ltuid CHAR(64), ltoken CHAR(64), cookie_token CHAR(64)");
        dataBase.createTable("GITool_GenshinImpact_data", "userId CHAR(64), uid CHAR(64), authkey VARCHAR(255)");
        String execute = "SELECT * FROM GITool_HoyoLab_data WHERE userId='" + userId + "';";
        ResultSet result = dataBase.runStatementQuery(execute);
        try {
            while(result.next()) {
                ltuid = result.getString("ltuid"); if(ltuid == null) ltuid = "";
                ltoken = result.getString("ltoken"); if(ltoken == null) ltoken = "";
                cookie_token = result.getString("cookie_token"); if(cookie_token == null) cookie_token = "";
                break;
            }
        } catch(Exception e) {
            CommandHandler.sendMessage(event, sender, "Fetch database failed. Exception: " + e.getMessage());
            return;
        }
        execute = "SELECT * FROM GITool_GenshinImpact_data WHERE userId='" + userId + "';";
        result = dataBase.runStatementQuery(execute);
        try {
            while(result.next()) {
                uid = result.getString("uid"); if(uid == null) uid = "";
                authKey = result.getString("authkey"); if(authKey == null) authKey = "";
                break;
            }
        } catch(Exception e) {
            CommandHandler.sendMessage(event, sender, "Fetch database failed. Exception: " + e.getMessage());
            return;
        }

        switch(args.get(0).toLowerCase()) {
            case "uid" -> {
                if(args.size() <= 1) {
                    CommandHandler.sendMessage(event, sender, "Usage: /gitool uid <uid> - Update Genshin UID");
                    return;
                }
                String id = args.get(1);

                uid(sender, event, userId, id, authKey);
            }

            case "authkey" -> {
                if(args.size() <= 1) {
                    CommandHandler.sendMessage(event, sender, "Usage: /gitool authkey <authkey> - Update Game Authkey");
                    return;
                }
                String key = args.get(1);

                authkey(sender, event, userId, uid, key);
            }

            case "daily_checkin", "dailycheckin", "dc" -> dailycheckin(sender, event, userId, ltuid, ltoken, cookie_token);

            case "mycharacter", "mychar", "mc" -> {
                if(args.size() <= 1) {
                    CommandHandler.sendMessage(event, sender, "Usage: /gitool mychar <character> - Show character info");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for(int i = 1; i < args.size(); i++) {
                    sb.append(args.get(i));
                    if(i < args.size() - 1) sb.append(" ");
                }
                String character = sb.toString();
                mycharacter(sender, event, userId, ltuid, ltoken, cookie_token, uid, character);
            }

            case "redeem_code", "code" -> {
                if(args.size() <= 1) {
                    CommandHandler.sendMessage(event, sender, "Usage: /gitool code <code> - Redeem giftcode");
                    return;
                }

                String code = args.get(1);
                redeemcode(sender, event, userId, ltuid, ltoken, cookie_token, uid, code);
            }

            default -> def(sender, event);
        }
    }


    private void def(DiscordUser sender, SlashCommandInteractionEvent event) {
        String message =
                "/gitool uid <uid> - Update Genshin UID\n" +
                "/gitool authkey <authkey> - Update Game Authkey\n" +
                "/gitool dailycheckin - Genshin daily check-in\n" +
                "/gitool mychar <character> - Show character info\n" +
                "/gitool code <code> - Redeem giftcode";

        CommandHandler.sendMessageEmbed(event, sender, "Genshin Tool Command Help", message);
    }

    private void uid(DiscordUser sender, SlashCommandInteractionEvent event, String userId, String uid, String authkey) {
        DataBaseManager dataBase = DiscordBot.getDataBaseManager();
        dataBase.runStatement("DELETE FROM GITool_GenshinImpact_data WHERE userId='" + userId + "';");
        dataBase.runStatement("INSERT INTO GITool_GenshinImpact_data(userId, uid, authkey) VALUES('" + userId + "', '" + uid + "', '" + authkey + "');");
        DiscordUtils.sendPrivateMessage(userId, "Your Genshin UID has been updated.");
    }

    private void authkey(DiscordUser sender, SlashCommandInteractionEvent event, String userId, String uid, String authkey) {
        DataBaseManager dataBase = DiscordBot.getDataBaseManager();
        dataBase.runStatement("DELETE FROM GITool_GenshinImpact_data WHERE userId='" + userId + "';");
        dataBase.runStatement("INSERT INTO GITool_GenshinImpact_data(userId, ltuid, ltoken, uid, authkey) VALUES('" + userId + "', '" + uid + "', '" + authkey + "');");
        DiscordUtils.sendPrivateMessage(userId, "Your Game Authkey has been updated.");
    }

    private void dailycheckin(DiscordUser sender, SlashCommandInteractionEvent event, String userId, String ltuid, String ltoken, String cookie_token) {
        if(ltuid.isEmpty() && ltoken.isEmpty() && cookie_token.isEmpty()) {
            CommandHandler.sendMessage(event, sender, "You don't have HoyoLab cookie. Try /hoyotool cookie");
            return;
        }

        CheckInUser user;
        try {
            user = tool.getDailyCheckIn().getUser(ltuid, ltoken, cookie_token);
        } catch(Exception e) {
            event.reply("Error Exception: " + e.getMessage()).queue();
            return;
        }

        boolean auto = false;
        DataBaseManager dataBase = DiscordBot.getDataBaseManager();
        dataBase.createTable("GITool_dailycheckin", "userId CHAR(64), auto BOOL");
        String execute = "SELECT * FROM GITool_dailycheckin WHERE userId='" + userId + "';";
        ResultSet result = dataBase.runStatementQuery(execute);
        try {
            while(result.next()) {
                auto = result.getBoolean("auto");
                break;
            }
        } catch(Exception e) {
            CommandHandler.sendMessage(event, sender, "Fetch database failed. Exception: " + e.getMessage());
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int month = calendar.getTime().getMonth() + 1;
        int year = calendar.getTime().getYear() + 1900;
        String message =
                "**Daily Reward statistics (" + month + "/" + year + ")**\n" +
                        " • Total sign day: " + user.getTotalSignDay() + "\n" +
                        " • Today claim: " + (user.isSign() ? "yes" : "no") + "\n" +
                        " • Server: " + user.getRegion() + "\n" +
                        "\n" +
                        "**Reward Info**\n" +
                        " • Today reward: " + user.getTodayCheckIn().getName() + " x" + user.getTodayCheckIn().getAmount() + "\n" +
                        " • Auto claim: " + (auto ? "on" : "off");
        User u = sender.getAsUser();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(DiscordBot.getSettings().getColor());
        eb.setTitle("Genshin Impact Daily check-in");
        eb.setDescription(message);
        eb.setFooter(u.getAsTag(), u.getAvatarUrl());
        eb.setThumbnail(user.getTodayCheckIn().getIconUrl());

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("dailycheckin_claim:" + userId, "Claim"));
        buttons.add(Button.primary("dailycheckin_autoclaim:" + userId, "Auto Claim"));

        event.replyEmbeds(eb.build()).addActionRow(buttons).queue();
    }

    private void mycharacter(DiscordUser sender, SlashCommandInteractionEvent event, String userId, String ltuid, String ltoken, String cookie_token, String uid, String character) {
        if(ltuid.isEmpty() && ltoken.isEmpty() && cookie_token.isEmpty()) {
            CommandHandler.sendMessage(event, sender, "You don't have HoyoLab cookie. Try /hoyotool cookie");
            return;
        }

        if(uid.isEmpty()) {
            CommandHandler.sendMessage(event, sender, "You don't have Genshin UID. Try /gitool uid");
            return;
        }

        HoyoAPI.inst().setCookie(ltoken, ltuid, cookie_token);
        Player genshinPlayer = HoyoAPI.inst().genshin().getPlayer(uid);
        HoyoAPI.inst().resetCookie();

        String input = character.toLowerCase();
        for(Avatar avatar : genshinPlayer.getAvatars()) {
            if(avatar.getName().toLowerCase().contains(input)) {
                Weapon weapon = avatar.getWeapon();

                Reliquaries reliquaries1 = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.FLOWER_OF_LIFE);
                Reliquaries reliquaries2 = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.PLUME_OF_DEATH);
                Reliquaries reliquaries3 = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.SANDS_OF_EON);
                Reliquaries reliquaries4 = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.GOBLET_OF_EONOTHEM);
                Reliquaries reliquaries5 = avatar.getReliquaries().get(Reliquaries.ReliquariesPosition.CIRCLET_OF_LOGOS);

                String message =
                        "**Information**\n" +
                        " • Name: " + avatar.getName() + "\n" +
                        " • Element: " + avatar.getElement().getKey() + "\n" +
                        " • Rarity: " + avatar.getRarity() + "★\n" +
                        " • Level: " + avatar.getLevel() + "\n" +
                        " • Fetter level: " + avatar.getFetter() + "\n" +
                        " • Constellations: " + avatar.getConstellations() + "\n" +
                        "\n" +
                        "**Weapon Equipment**\n" +
                        " • Name: " + weapon.getName() + "\n" +
                        " • Level: " + weapon.getLevel() + "\n" +
                        " • Rarity: " + weapon.getRarity() + "★\n" +
                        " • Refine: " + weapon.getAffixLevel() + "\n" +
                        "\n" +
                        "**Reliquaries Equipment**\n" +
                        " • " + ((reliquaries1 == null) ? "No Equip" : reliquaries1.getName()) + " Lv." + ((reliquaries1 == null) ? "0" : reliquaries1.getLevel()) + "\n" +
                        " • " + ((reliquaries2 == null) ? "No Equip" : reliquaries2.getName()) + " Lv." + ((reliquaries2 == null) ? "0" : reliquaries2.getLevel()) + "\n" +
                        " • " + ((reliquaries3 == null) ? "No Equip" : reliquaries3.getName()) + " Lv." + ((reliquaries3 == null) ? "0" : reliquaries3.getLevel()) + "\n" +
                        " • " + ((reliquaries4 == null) ? "No Equip" : reliquaries4.getName()) + " Lv." + ((reliquaries4 == null) ? "0" : reliquaries4.getLevel()) + "\n" +
                        " • " + ((reliquaries5 == null) ? "No Equip" : reliquaries5.getName()) + " Lv." + ((reliquaries5 == null) ? "0" : reliquaries5.getLevel());
                User u = sender.getAsUser();

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(DiscordBot.getSettings().getColor());
                eb.setTitle("Genshin Impact Character Info");
                eb.setDescription(message);
                eb.setFooter(u.getAsTag(), u.getAvatarUrl());
                eb.setThumbnail(avatar.getIcon());

                event.replyEmbeds(eb.build()).queue();
                return;
            }
        }

        event.reply("Cannot find your character with this name").queue();
    }

    private void redeemcode(DiscordUser sender, SlashCommandInteractionEvent event, String userId, String ltuid, String ltoken, String cookie_token, String uid, String code) {
        if(ltuid.isEmpty() && ltoken.isEmpty() && cookie_token.isEmpty()) {
            CommandHandler.sendMessage(event, sender, "You don't have HoyoLab cookie. Try /hoyotool cookie");
            return;
        }

        if(uid.isEmpty()) {
            CommandHandler.sendMessage(event, sender, "You don't have Genshin UID. Try /gitool uid");
            return;
        }

        HoyoAPI.inst().setCookie(ltoken, ltuid, cookie_token);
        CdkeyRedeem codeRedeem = HoyoAPI.inst().genshin().redeemCode(uid, code);
        HoyoAPI.inst().resetCookie();

        String message =
                " • UID: " + codeRedeem.getUid() + "\n" +
                " • Server: " + codeRedeem.getRegion() + "\n" +
                " • Code: " + codeRedeem.getCode() + "\n" +
                " • Status: " + (codeRedeem.isSuccess() ? "success" : "failed") + "\n" +
                " • Response Message: " + codeRedeem.getMessage();
        User u = sender.getAsUser();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(DiscordBot.getSettings().getColor());
        eb.setTitle("Genshin Impact Redeem Code");
        eb.setDescription(message);
        eb.setFooter(u.getAsTag(), u.getAvatarUrl());

        event.replyEmbeds(eb.build()).queue();
    }

}
