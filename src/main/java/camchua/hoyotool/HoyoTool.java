package camchua.hoyotool;

import camchua.discordbot.DiscordBot;
import camchua.discordbot.command.Command;
import camchua.discordbot.command.CommandMap;
import camchua.discordbot.plugin.api.DiscordPlugin;
import camchua.hoyotool.buttonlistener.DailyCheckinListener;
import camchua.hoyotool.commands.GenshinImpactCmd;
import camchua.hoyotool.commands.HoyoLabCmd;
import camchua.hoyotool.dailycheckin.DailyCheckIn;
import camchua.hoyotool.dailycheckin.model.CheckReward;
import camchua.hoyotool.tasks.AutoClaimTask;

import java.lang.reflect.Constructor;
import java.util.Timer;

public class HoyoTool extends DiscordPlugin {

    private DailyCheckIn dailyCheckIn;

    @Override
    public void onEnable() {
        initDailyCheckIn();
        CheckReward.init();
        regCmd();
        regEvent();
        regTask();
    }


    private void initDailyCheckIn() {
        String dailyCheckInClassName = "camchua.hoyotool.dailycheckin.DailyCheckIn";
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
        CommandMap commandMap = DiscordBot.getCommandMap();

        commandMap.registerCommand(this, GenshinImpactCmd.class.getAnnotation(Command.class).command(), new GenshinImpactCmd(this));
        commandMap.registerCommand(this, HoyoLabCmd.class.getAnnotation(Command.class).command(), new HoyoLabCmd(this));
    }

    private void regEvent() {
        DiscordBot.getBot().addEventListener(new DailyCheckinListener(this));

    }

    private void regTask() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new AutoClaimTask(this), 1000, 1000);
    }


    public DailyCheckIn getDailyCheckIn() {
        return this.dailyCheckIn;
    }

}
