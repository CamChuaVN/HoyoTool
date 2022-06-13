package camchua.genshinimpacttool;

import camchua.discordbot.DiscordBot;
import camchua.discordbot.discord.DiscordCommandExecutor;
import camchua.discordbot.event.DiscordEvent;
import camchua.discordbot.plugin.api.DiscordPlugin;
import camchua.genshinimpacttool.buttonlistener.DailyCheckinListener;
import camchua.genshinimpacttool.commands.GenshinImpactCmd;
import camchua.genshinimpacttool.dailycheckin.DailyCheckIn;
import camchua.genshinimpacttool.dailycheckin.model.CheckReward;
import camchua.genshinimpacttool.listener.PacketReceiveListener;
import camchua.genshinimpacttool.manager.FileManager;
import camchua.genshinimpacttool.tasks.AutoClaimTask;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Timer;

public class GenshinImpactTool extends DiscordPlugin {

    private DailyCheckIn dailyCheckIn;

    @Override
    public void onEnable() {
        initDailyCheckIn();
        CheckReward.init();
        regCmd();
        regEvent();
        regTask();
        FileManager.setup(this);
    }


    private void initDailyCheckIn() {
        String dailyCheckInClassName = "camchua.genshinimpacttool.dailycheckin.DailyCheckIn";
        try {
            Class<?> clazz = Class.forName(dailyCheckInClassName);
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            Object dailyCheckInClass = ctor.newInstance();
            ctor.setAccessible(false);
            this.dailyCheckIn = (DailyCheckIn) dailyCheckInClass;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void regCmd() {
        DiscordCommandExecutor.registerCommand("genshinimpact", new GenshinImpactCmd(this), Arrays.asList("genshin", "gi"));
    }

    private void regEvent() {
        DiscordBot.getBot().addEventListener(new DailyCheckinListener(this));

        new PacketReceiveListener(this).registerEvent();
    }

    private void regTask() {
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new AutoClaimTask(this), 1000, 1000);
    }


    public DailyCheckIn getDailyCheckIn() {
        return this.dailyCheckIn;
    }

}
