package camchua.genshinimpacttool.utils;

import camchua.discordbot.configuration.file.FileConfiguration;
import camchua.discordbot.discord.DiscordMessages;
import camchua.genshinimpacttool.manager.FileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Messages {

    public static String get(String message_id, String user_id, HashMap<String, String> replace) {
        String locale = DiscordMessages.getUserLocale(user_id);
        FileConfiguration data = FileManager.getFileConfig(FileManager.Files.CONFIG);
        String msg = data.getString("messages." + locale + "." + message_id, message_id + " " + locale);
        if(replace != null) {
            List<String> list = new ArrayList<>(replace.keySet());
            for(String req : list) {
                String res = replace.get(req);
                msg = msg.replace(req, res);
            }
        }
        msg = msg.replace("%tag%", "<@" + user_id + ">");
        return msg;
    }

}
