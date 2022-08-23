package camchua.hoyotool.commands;

import camchua.discordbot.DiscordBot;
import camchua.discordbot.bot.user.DiscordUser;
import camchua.discordbot.command.Command;
import camchua.discordbot.command.CommandHandler;
import camchua.discordbot.manager.DataBaseManager;
import camchua.discordbot.utils.DiscordUtils;
import camchua.hoyotool.HoyoTool;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

@Command(command = "hoyolab", aliases = {"hoyotool"}, description = "Tool for Hoyo Lab")
public class HoyoLabCmd implements CommandHandler {

    private HoyoTool tool;

    public HoyoLabCmd(HoyoTool tool) {
        this.tool = tool;
    }

    @Override
    public void execute(DiscordUser sender, List<String> args, SlashCommandInteractionEvent event) {
        if(args.size() == 0) {
            def(sender, event);
            return;
        }

        String userId = sender.getAsUser().getId();
        DataBaseManager dataBase = DiscordBot.getDataBaseManager();
        dataBase.createTable("GITool_HoyoLab_data", "userId CHAR(64), ltuid CHAR(64), ltoken CHAR(64), cookie_token CHAR(64)");

        switch(args.get(0).toLowerCase()) {
            case "cookie" -> {
                if(args.size() <= 3) {
                    CommandHandler.sendMessage(event, sender, "Usage: /hoyotool cookie <ltuid> <ltoken> <cookie_token> - Update HoyoLab cookie");
                    return;
                }
                String u = args.get(1);
                String t = args.get(2);
                String c = args.get(3);

                cookie(sender, event, userId, u, t, c);
            }
        }
    }


    private void def(DiscordUser sender, SlashCommandInteractionEvent event) {
        String message =
                "/hoyotool cookie <ltuid> <ltoken> <cookie_token> - Update HoyoLab cookie";

        CommandHandler.sendMessageEmbed(event, sender, "Hoyo Tool Command Help", message);
    }

    private void cookie(DiscordUser sender, SlashCommandInteractionEvent event, String userId, String ltuid, String ltoken, String cookieToken) {
        DataBaseManager dataBase = DiscordBot.getDataBaseManager();
        dataBase.runStatement("DELETE FROM GITool_HoyoLab_data WHERE userId='" + userId + "';");
        dataBase.runStatement("INSERT INTO GITool_HoyoLab_data(userId, ltuid, ltoken, cookie_token) VALUES('" + userId + "', '" + ltuid + "', '" + ltoken + "', '" + cookieToken + "');");
        DiscordUtils.sendPrivateMessage(userId, "Your HoyoLab cookie has been updated.");
    }

}
