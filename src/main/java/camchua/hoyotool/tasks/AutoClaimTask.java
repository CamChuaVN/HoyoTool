package camchua.hoyotool.tasks;

import camchua.discordbot.DiscordBot;
import camchua.discordbot.manager.DataBaseManager;
import camchua.discordbot.utils.DiscordUtils;
import camchua.hoyotool.HoyoTool;
import camchua.hoyotool.dailycheckin.model.CheckIn;
import camchua.hoyotool.dailycheckin.model.CheckInUser;
import net.dv8tion.jda.api.EmbedBuilder;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

public class AutoClaimTask extends TimerTask {

    private HoyoTool tool;

    public AutoClaimTask(HoyoTool tool) {
        this.tool = tool;
    }

    private int[] check_time = {3, 9, 15, 21};

    @Override
    public void run() {
        Date d = Calendar.getInstance().getTime();
        for(int hour : check_time) {
            if(d.getHours() == hour && d.getMinutes() == 0 && d.getSeconds() == 0) {
                DataBaseManager dataBase = DiscordBot.getDataBaseManager();
                dataBase.createTable("GITool_dailycheckin", "userId CHAR(64), auto BOOL");
                String execute = "SELECT * FROM GITool_dailycheckin";
                ResultSet result = dataBase.runStatementQuery(execute);
                try {
                    while(result.next()) {
                        String userId = result.getString("userId");
                        boolean auto = result.getBoolean("auto");
                        if(!auto) continue;

                        String ltuid = "", ltoken = "", cookie_token = "";

                        dataBase.createTable("GITool_HoyoLab_data", "userId CHAR(64), ltuid CHAR(64), ltoken CHAR(64), cookie_token CHAR(64)");
                        execute = "SELECT * FROM GITool_HoyoLab_data WHERE userId='" + userId + "';";
                        ResultSet result2 = dataBase.runStatementQuery(execute);
                        try {
                            while(result2.next()) {
                                ltuid = result2.getString("ltuid"); if(ltuid == null) ltuid = "";
                                ltoken = result2.getString("ltoken"); if(ltoken == null) ltoken = "";
                                cookie_token = result2.getString("cookie_token"); if(cookie_token == null) cookie_token = "";
                                break;
                            }
                        } catch(Exception e) { e.printStackTrace(); continue; }
                        if(ltuid.isEmpty() && ltoken.isEmpty() && cookie_token.isEmpty()) continue;

                        try {
                            CheckInUser user = tool.getDailyCheckIn().getUser(ltuid, ltoken, cookie_token);
                            CheckIn check = user.checkIn();

                            if(!check.isSuccess()) {
                                if(check.getCode() != -5003) {
                                    String msg = "Daily check-in error: " + check.getMessage();
                                    DiscordUtils.sendPrivateMessage(userId, msg);
                                }
                                continue;
                            }

                            String message =
                                    "**Daily Check-in Reward**\n" +
                                    " • Item: " + check.getName() + "\n" +
                                    " • Amount: " + check.getAmount();

                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setColor(DiscordBot.getSettings().getColor());
                            eb.setTitle(DiscordBot.getSettings().getBotName());
                            eb.setDescription(message);
                            eb.setThumbnail(check.getIconUrl());

                            DiscordUtils.sendPrivateMessage(userId, eb.build());
                        } catch(Exception e) {
                            String msg = "Daily check-in error: " + e.getMessage();
                            DiscordUtils.sendPrivateMessage(userId, msg);
                        }
                    }
                } catch(Exception e) { e.printStackTrace(); }
            }
        }
    }

}
