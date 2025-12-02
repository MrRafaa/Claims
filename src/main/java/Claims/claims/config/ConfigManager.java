package Claims.claims.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private int maxClaimsPerPlayer;
    private Material claimItem;
    private String claimItemName;
    private int claimBlockBuffer;
    private int maxPartyMembers;
    private int partyInviteTimeout;
    private boolean pvpInClaims;
    private int maxClaimArea;

    private boolean protectionBlockBreak;
    private boolean protectionBlockPlace;
    private boolean protectionEntityDamage;
    private boolean protectionExplosion;
    private boolean protectionFireSpread;
    private boolean protectionMobGriefing;
    private int maxTrusted;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.maxClaimsPerPlayer = config.getInt("max-claims-per-player", 3);

        String materialName = config.getString("claim-item.material", "GOLDEN_SHOVEL");
        this.claimItem = Material.getMaterial(materialName);
        if (this.claimItem == null) {
            this.claimItem = Material.GOLDEN_SHOVEL;
            plugin.getLogger()
                    .warning("Invalid claim-item material: " + materialName + ". Defaulting to GOLDEN_SHOVEL.");
        }

        this.claimItemName = config.getString("claim-item.name", "&eClaim Tool");
        this.claimBlockBuffer = config.getInt("claim-block-buffer", 10);
        this.maxPartyMembers = config.getInt("max-party-members", 10);
        this.partyInviteTimeout = config.getInt("party-invite-timeout", 60);
        this.pvpInClaims = config.getBoolean("pvp-in-claims", false);
        this.maxClaimArea = config.getInt("max-claim-area", 2500);

        this.protectionBlockBreak = config.getBoolean("protection.block-break", true);
        this.protectionBlockPlace = config.getBoolean("protection.block-place", true);
        this.protectionEntityDamage = config.getBoolean("protection.entity-damage", true);
        this.protectionExplosion = config.getBoolean("protection.explosion", true);
        this.protectionFireSpread = config.getBoolean("protection.fire-spread", true);
        this.protectionMobGriefing = config.getBoolean("protection.mob-griefing", true);
        this.maxTrusted = config.getInt("max-trusted", 10);
    }

    public int getMaxClaimsPerPlayer() {
        return maxClaimsPerPlayer;
    }

    public Material getClaimItem() {
        return claimItem;
    }

    public String getClaimItemName() {
        return claimItemName;
    }

    public int getClaimBlockBuffer() {
        return claimBlockBuffer;
    }

    public int getMaxPartyMembers() {
        return maxPartyMembers;
    }

    public int getPartyInviteTimeout() {
        return partyInviteTimeout;
    }

    public boolean isPvpInClaims() {
        return pvpInClaims;
    }

    public int getMaxClaimArea() {
        return maxClaimArea;
    }

    public boolean isBlockBreakProtection() {
        return protectionBlockBreak;
    }

    public boolean isBlockPlaceProtection() {
        return protectionBlockPlace;
    }

    public boolean isEntityDamageProtection() {
        return protectionEntityDamage;
    }

    public boolean isExplosionProtection() {
        return protectionExplosion;
    }

    public boolean isFireSpreadProtection() {
        return protectionFireSpread;
    }

    public boolean isMobGriefingProtection() {
        return protectionMobGriefing;
    }

    public int getMaxTrusted() {
        return maxTrusted;
    }
}
