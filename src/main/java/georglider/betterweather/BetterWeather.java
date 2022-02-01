package georglider.betterweather;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public final class BetterWeather extends JavaPlugin {

    public ConfigManager data;
    public int weatherChannel;
    private TokenData token;
    private HttpURLConnection connection;
    private WeatherTypes latestWeather;
    private Boolean overrideDefaultWeather;

    @Override
    public void onEnable() {
        this.data = new ConfigManager(this);

        String token = Objects.requireNonNull(data.getConfig().get("token")).toString();
        if (token.contains("herokuapp")) {
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            console.sendMessage(ChatColor.GREEN + "Привет, спасибо за установку плагина");
            console.sendMessage(ChatColor.GREEN + "Чтобы начать использовать его, перейдите по ссылке");
            console.sendMessage(ChatColor.BLUE + "https://betterweathermc.herokuapp.com");
            console.sendMessage(ChatColor.GREEN + "Зарегистрируйтесь и вставьте токен в файл конфигурации");
            console.sendMessage(ChatColor.GREEN + "Iли воспользуйтесь командой /betterweather token [ТОКЕН]");
            return;
        }

        try {
            this.connection = (HttpURLConnection) new URL("https://betterweathermc.herokuapp.com/api/weather/init/" + token).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.addRequestProperty("User-Agent", "BetterWeather");

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                InputStream inputStream = connection.getInputStream();

                InputStreamReader reader = new InputStreamReader(inputStream);
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();

                this.token = new TokenData(UUID.fromString(token),
                        Integer.parseInt(json.get("cityId").toString()), json.get("name").toString(),
                        json.get("country").toString(), json.get("state").toString());

                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + String.format("Initialized with city %s in %s", this.token.getCityName(), this.token.getCountry()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        this.overrideDefaultWeather = Objects.equals(data.getConfig().get("overrideDefaultWeather"), true);

        if (overrideDefaultWeather) {
            this.weatherChannel = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL("https://betterweathermc.herokuapp.com/api/weather/" + token).openConnection();
                    InputStream inputStream = con.getInputStream();

                    InputStreamReader reader = new InputStreamReader(inputStream);
                    JsonObject json = new JsonParser().parse(reader).getAsJsonObject();

                    String weather = json.get("type").getAsString();

                    if (this.latestWeather == null || !this.latestWeather.getName().equals(weather)) {
                        switch (weather) {
                            case "CLEAR":
                                Bukkit.getWorlds().forEach(x -> x.setStorm(false));
                                break;
                            case "RAIN":
                                Bukkit.getWorlds().forEach(x -> x.setStorm(true));
                                Bukkit.getWorlds().forEach(x -> x.setThundering(false));
                                break;
                            case "THUNDER":
                                Bukkit.getWorlds().forEach(x -> x.setStorm(true));
                                Bukkit.getWorlds().forEach(x -> x.setThundering(true));
                                break;
                        }

                        Bukkit.getWorlds().forEach(x -> x.setWeatherDuration(Integer.MAX_VALUE));
                        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + String.format("Weather changed to %s by BetterWeather", weather));
                        this.latestWeather = WeatherTypes.valueOf(weather);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, 1, 20 * 60 * 5);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(priority= EventPriority.HIGHEST)
    public void onWeatherChange(WeatherChangeEvent event) {
        boolean rain = event.toWeatherState();

        if (rain && overrideDefaultWeather)
            event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onThunderChange(ThunderChangeEvent event) {
        boolean storm = event.toThunderState();

        if (storm && overrideDefaultWeather)
            event.setCancelled(true);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("betterweather")) {
            if (!sender.hasPermission("betterweather.reload")) {
                sender.sendMessage("");
                return false;
            }
            if (args.length == 0) {
                sender.sendMessage("");
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Objects.requireNonNull(this.getConfig().getString("reload.message"))));
                this.reloadConfig();
            }

        }
        return super.onCommand(sender, command, label, args);
    }
}
