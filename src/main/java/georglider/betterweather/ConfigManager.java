package georglider.betterweather;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {

    private final BetterWeather plugin;
    private FileConfiguration dataConfig = null;
    private File configFile = null;

    public ConfigManager(BetterWeather plugin) {
        this.plugin = plugin;
        // Init config
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null)
            this.configFile = new File(this.plugin.getDataFolder(), "config.yml");

        this.dataConfig = YamlConfiguration.loadConfiguration(this.configFile);

        InputStream defaultStream = this.plugin.getResource("config.yml");

        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null) {
            reloadConfig();
        }
        return this.dataConfig;
    }

    public void saveConfig() {
        if (this.dataConfig == null || this.configFile == null)
            return;

        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
        }

        if (!this.configFile.exists()) {
            this.plugin.saveResource("config.yml", false);
        }

        if (this.configFile.length() == 0) {
            this.getConfig().set("token", "https://betterweathermc.herokuapp.com");
            this.getConfig().set("overrideDefaultWeather", true);
            this.saveConfig();
        }
    }

}
