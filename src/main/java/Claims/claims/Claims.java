package Claims.claims;

import Claims.claims.commands.ClaimCommand;
import Claims.claims.commands.PartyCommand;
import Claims.claims.config.ConfigManager;
import Claims.claims.config.MessageManager;
import Claims.claims.listeners.ClaimCreationListener;
import Claims.claims.listeners.ClaimProtectionListener;
import Claims.claims.listeners.ClaimVisualizationListener;
import Claims.claims.listeners.GuiListener;
import Claims.claims.listeners.PartyListener;
import Claims.claims.managers.ClaimManager;
import Claims.claims.managers.PartyManager;
import Claims.claims.storage.JsonStorageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Claims extends JavaPlugin {

    private static Claims instance;
    private ConfigManager configManager;
    private JsonStorageManager storageManager;
    private MessageManager messageManager;
    private ClaimManager claimManager;
    private PartyManager partyManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize Config & Messages
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);

        // Initialize Storage
        this.storageManager = new JsonStorageManager(this);

        // Initialize Managers
        this.claimManager = new ClaimManager(this);
        this.partyManager = new PartyManager(this);

        // Register Commands
        getCommand("claim").setExecutor(new ClaimCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));

        // Register Listeners
        getServer().getPluginManager().registerEvents(new ClaimProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ClaimCreationListener(this), this);
        getServer().getPluginManager().registerEvents(new PartyListener(this), this);
        getServer().getPluginManager().registerEvents(new ClaimVisualizationListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        // Auto-save task
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            claimManager.saveData();
            partyManager.saveParties();
        }, 6000L, 6000L); // Every 5 minutes

        getLogger().info("Claims plugin enabled!");
    }

    @Override
    public void onDisable() {
        if (claimManager != null)
            claimManager.saveData();
        if (partyManager != null)
            partyManager.saveParties();
        getLogger().info("Claims plugin disabled!");
    }

    public static Claims getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public JsonStorageManager getStorageManager() {
        return storageManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }
}
