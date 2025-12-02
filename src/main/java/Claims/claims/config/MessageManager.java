package Claims.claims.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MessageManager {
    private final JavaPlugin plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private final Map<String, String> messageCache = new HashMap<>();

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Load defaults if missing
        InputStream defConfigStream = plugin.getResource("messages.yml");
        if (defConfigStream != null) {
            messagesConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream)));
        }

        // Cache messages
        for (String key : messagesConfig.getKeys(true)) {
            if (messagesConfig.isString(key)) {
                messageCache.put(key, ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(key)));
            }
        }
    }

    public String getMessage(String key) {
        return messageCache.getOrDefault(key, key);
    }

    public void sendMessage(Player player, String key, String... placeholders) {
        String msg = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                msg = msg.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        player.sendMessage(msg);
    }
}
