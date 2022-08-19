package camchua.genshinimpacttool.buttonlistener;

import camchua.discordbot.DiscordBot;
import camchua.discordbot.manager.DataBaseManager;
import camchua.genshinimpacttool.GenshinImpactTool;
import camchua.genshinimpacttool.dailycheckin.model.CheckIn;
import camchua.genshinimpacttool.dailycheckin.model.CheckInUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.ResultSet;

public class DailyCheckinListener extends ListenerAdapter {

    private GenshinImpactTool tool;

    public DailyCheckinListener(GenshinImpactTool tool) {
        this.tool = tool;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String userId = event.getUser().getId();

        String component = event.getComponentId().split(":")[0];
        String author_id = event.getComponentId().split(":")[1];

        if(!component.startsWith("dailycheckin_")) return;

        if(!userId.equals(author_id)) {
            String msg = "You cannot perform this action | " + event.getUser().getAsMention();
            event.reply(msg).queue();
            return;
        }

        String ltuid = "", ltoken = "", uid = "", authKey = "";

        DataBaseManager dataBase = DiscordBot.getDataBaseManager();
        dataBase.createTable("GITool_data", "userId CHAR(64), ltuid CHAR(64), ltoken CHAR(64), uid CHAR(64), authkey VARCHAR(255)");
        String execute = "SELECT * FROM GITool_data WHERE userId='" + userId + "';";
        ResultSet result = dataBase.runStatementQuery(execute);
        try {
            while(result.next()) {
                ltuid = result.getString("ltuid"); if(ltuid == null) ltuid = "";
                ltoken = result.getString("ltoken"); if(ltoken == null) ltoken = "";
                uid = result.getString("uid"); if(uid == null) uid = "";
                authKey = result.getString("authkey"); if(authKey == null) authKey = "";
                break;
            }
        } catch(Exception e) {
            String msg = "Fetch database failed. Exception: " + e.getMessage() + " | " + event.getUser().getAsMention();
            event.reply(msg).queue();
            return;
        }

        switch(component) {
            case "dailycheckin_claim" -> {
                try {
                    CheckInUser user = tool.getDailyCheckIn().getUser(ltuid, ltoken);
                    CheckIn check = user.checkIn();

                    if(!check.isSuccess()) {
                        String msg = "Daily check-in error: " + check.getMessage() + " | " + event.getUser().getAsMention();
                        event.reply(msg).queue();
                        return;
                    }

                    String message =
                            "**Daily Check-in Reward**\n" +
                            " • Item: " + check.getName() + "\n" +
                            " • Amount: " + check.getAmount() + "\n" +
                            " • Receiver: " + event.getUser().getAsMention();

                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(DiscordBot.getSettings().getColor());
                    eb.setTitle(DiscordBot.getSettings().getBotName());
                    eb.setDescription(message);
                    eb.setFooter(event.getUser().getAsTag(), event.getUser().getAvatarUrl());
                    eb.setThumbnail(check.getIconUrl());

                    event.replyEmbeds(eb.build()).queue();
                } catch(Exception e) {
                    String msg = "Daily check-in error: " + e.getMessage() + " | " + event.getUser().getAsMention();
                    event.reply(msg).queue();
                }
            }

            case "dailycheckin_autoclaim" -> {
                boolean auto = false;
                dataBase = DiscordBot.getDataBaseManager();
                dataBase.createTable("GITool_dailycheckin", "userId CHAR(64), auto BOOL");
                execute = "SELECT * FROM GITool_dailycheckin WHERE userId='" + userId + "';";
                result = dataBase.runStatementQuery(execute);
                try {
                    while(result.next()) {
                        auto = result.getBoolean("auto");
                        break;
                    }
                } catch(Exception e) {
                    String msg = "Fetch database failed. Exception: " + e.getMessage() + " | " + event.getUser().getAsMention();
                    event.reply(msg).queue();
                    return;
                }

                if(auto) {
                    dataBase.runStatement("DELETE FROM GITool_dailycheckin WHERE userId='" + userId + "';");
                    dataBase.runStatement("INSERT INTO GITool_dailycheckin(userId, auto) VALUES('" + userId + "', 0);");
                    String msg = "Auto check-in turn off | " + event.getUser().getAsMention();
                    event.reply(msg).queue();
                } else {
                    dataBase.runStatement("DELETE FROM GITool_dailycheckin WHERE userId='" + userId + "';");
                    dataBase.runStatement("INSERT INTO GITool_dailycheckin(userId, auto) VALUES('" + userId + "', 1);");
                    String msg = "Auto check-in turn on | " + event.getUser().getAsMention();
                    event.reply(msg).queue();
                }

                try {
                    CheckInUser user = tool.getDailyCheckIn().getUser(ltuid, ltoken);
                    CheckIn check = user.checkIn();

                    if(check.isSuccess()) {
                        String message =
                                "**Daily Check-in Reward**\n" +
                                        " • Item: " + check.getName() + "\n" +
                                        " • Amount: " + check.getAmount() + "\n" +
                                        " • Receiver: " + event.getUser().getAsMention();

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(DiscordBot.getSettings().getColor());
                        eb.setTitle(DiscordBot.getSettings().getBotName());
                        eb.setDescription(message);
                        eb.setFooter(event.getUser().getAsTag(), event.getUser().getAvatarUrl());
                        eb.setThumbnail(check.getIconUrl());

                        event.replyEmbeds(eb.build()).queue();
                    }
                } catch(Exception e) {}
            }
        }
    }

}
