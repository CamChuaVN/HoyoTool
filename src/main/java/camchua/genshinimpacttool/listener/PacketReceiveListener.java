package camchua.genshinimpacttool.listener;

import camchua.discordbot.data.UserData;
import camchua.discordbot.discord.DiscordServer;
import camchua.discordbot.event.DiscordEvent;
import camchua.discordbot.event.EventHandler;
import camchua.discordbot.event.HandlerPriority;
import camchua.discordbot.event.packet.PacketReceiveEvent;
import camchua.discordbot.utils.DiscordUtils;
import camchua.discordbot.utils.Utils;
import camchua.genshinimpacttool.GenshinImpactTool;
import net.dv8tion.jda.api.exceptions.ContextException;

import java.util.Locale;

public class PacketReceiveListener extends DiscordEvent {

    private GenshinImpactTool tool;

    public PacketReceiveListener(GenshinImpactTool tool) {
        this.tool = tool;
    }

    public void registerEvent() {
        EventHandler handler = new EventHandler<>(PacketReceiveEvent.class);
        handler.priority(HandlerPriority.NORMAL);
        handler.listener(event -> onEvent((PacketReceiveEvent) event));
        DiscordEvent.register(handler);
    }

    private String split = "</>";

    public void onEvent(PacketReceiveEvent e) {
        Utils.info("Receive: " + e.getPacket());
        String action = e.getPacket().split(split)[0];
        String user_id = e.getPacket().split(split)[1];
        switch(action.toLowerCase()) {
            case "set_ltuid": {
                String value = e.getPacket().split(split)[2];
                UserData.set(user_id, tool.getName() + "\\data", "ltuid", value);
                return;
            }
            case "set_ltoken": {
                String value = e.getPacket().split(split)[2];
                UserData.set(user_id, tool.getName() + "\\data", "ltoken", value);
                return;
            }
            case "set_autoclaim": {
                boolean value = Boolean.parseBoolean(e.getPacket().split(split)[2]);
                UserData.set(user_id, tool.getName() + "\\daily_checkin", "auto", value);
                return;
            }
            case "get_autoclaim": {
                boolean auto = UserData.get(user_id, tool.getName() + "\\daily_checkin").getBoolean("auto", false);
                DiscordServer.sendPacket(e.getSender(), "Response" + split + "get_autoclaim" + split + auto);
                return;
            }
            case "sendmessage": {
                String msg = e.getPacket().split(split)[2];
                try {
                    DiscordUtils.sendPrivateMessage(user_id, msg);
                } catch(Exception ex) {
                    DiscordServer.sendPacket(e.getSender(), "Response" + split + "sendmessage" + split + "Unknown Discord User ID");
                }
                return;
            }
        }
    }

}
