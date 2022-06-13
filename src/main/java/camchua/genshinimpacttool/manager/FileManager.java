package camchua.genshinimpacttool.manager;

import camchua.discordbot.configuration.file.FileConfiguration;
import camchua.discordbot.configuration.file.YamlConfiguration;
import camchua.discordbot.plugin.api.DiscordPlugin;

import java.io.File;
import java.util.HashMap;

public class FileManager {

    private static HashMap<Files, File> file = new HashMap<Files, File>();
    private static HashMap<Files, FileConfiguration> configuration = new HashMap<Files, FileConfiguration>();

    public static void setup(DiscordPlugin plugin) {
        for (Files f : Files.values()) {
            File fl = new File(plugin.getDataFolder(), f.getLocation());
            if (!fl.exists()) {
                fl.getParentFile().mkdirs();
                plugin.saveResource(f.getLocation(), false);
            }
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(fl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            file.put(f, fl);
            configuration.put(f, config);
        }
    }

    public static FileConfiguration getFileConfig(Files f) {
        return configuration.get(f);
    }

    public static void saveFileConfig(FileConfiguration data, Files f) {
        try {
            data.save(file.get(f));
        } catch (Exception ex) {

        }
    }

    public enum Files {

        CONFIG("config.yml"),

        ;

        private String location;

        Files(String l) {
            this.location = l;
        }

        public String getLocation() {
            return this.location;
        }

    }

}
