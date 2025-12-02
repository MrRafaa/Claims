package Claims.claims.managers;

import Claims.claims.Claims;
import Claims.claims.models.Claim;
import Claims.claims.models.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimManager {
    private final Claims plugin;
    private final Map<UUID, Claim> claims = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

    public ClaimManager(Claims plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        plugin.getStorageManager().loadClaims().thenAccept(loadedClaims -> {
            for (Claim claim : loadedClaims) {
                claims.put(claim.getId(), claim);
            }
            plugin.getLogger().info("Loaded " + claims.size() + " claims.");
        });

        plugin.getStorageManager().loadPlayerData().thenAccept(loadedData -> {
            for (PlayerData data : loadedData) {
                playerData.put(data.getUuid(), data);
            }
            plugin.getLogger().info("Loaded " + playerData.size() + " player data entries.");
        });
    }

    public void saveData() {
        plugin.getStorageManager().saveClaims(new ArrayList<>(claims.values()));
        plugin.getStorageManager().savePlayerData(new ArrayList<>(playerData.values()));
    }

    public PlayerData getPlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, PlayerData::new);
    }

    public Claim getClaim(UUID claimId) {
        return claims.get(claimId);
    }

    public Collection<Claim> getAllClaims() {
        return Collections.unmodifiableCollection(claims.values());
    }

    public List<Claim> getPlayerClaims(UUID playerId) {
        List<Claim> playerClaims = new ArrayList<>();
        for (Claim claim : claims.values()) {
            if (claim.getOwnerId().equals(playerId)) {
                playerClaims.add(claim);
            }
        }
        return playerClaims;
    }

    public Claim getClaimAt(Location location) {
        if (location == null || location.getWorld() == null)
            return null;
        String worldName = location.getWorld().getName();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        // Linear search for now - can be optimized with a spatial data structure
        // (QuadTree) later
        for (Claim claim : claims.values()) {
            if (claim.getWorldName().equals(worldName) && claim.contains(x, z)) {
                return claim;
            }
        }
        return null;
    }

    public boolean createClaim(Player player, Location min, Location max) {
        if (!min.getWorld().equals(max.getWorld()))
            return false;

        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());

        // Check overlap
        for (Claim existing : claims.values()) {
            if (existing.getWorldName().equals(min.getWorld().getName())) {
                if (isOverlapping(minX, minZ, maxX, maxZ, existing)) {
                    return false; // Overlap
                }
            }
        }

        // Check limits
        PlayerData data = getPlayerData(player.getUniqueId());
        int maxClaims = data.getMaxClaims() != -1 ? data.getMaxClaims()
                : plugin.getConfigManager().getMaxClaimsPerPlayer();

        if (data.getClaimCount() >= maxClaims && !player.hasPermission("claims.admin.bypass")) {
            return false; // Limit reached
        }

        // Check area limit
        int area = (maxX - minX + 1) * (maxZ - minZ + 1);
        int maxArea = data.getMaxClaimArea() != -1 ? data.getMaxClaimArea()
                : plugin.getConfigManager().getMaxClaimArea();

        if (area > maxArea && !player.hasPermission("claims.admin.bypass")) {
            player.sendMessage("§cClaim too big! Max area: " + maxArea + " blocks.");
            return false;
        }

        // Check buffer
        int buffer = plugin.getConfigManager().getClaimBlockBuffer();
        for (Claim existing : claims.values()) {
            if (existing.getWorldName().equals(min.getWorld().getName())
                    && !existing.getOwnerId().equals(player.getUniqueId())) {
                if (isTooClose(minX, minZ, maxX, maxZ, existing, buffer)) {
                    return false; // Too close
                }
            }
        }

        Claim newClaim = new Claim(player.getUniqueId(), min.getWorld().getName(), minX, minZ, maxX, maxZ);
        claims.put(newClaim.getId(), newClaim);

        data.incrementClaimCount();
        saveData(); // Save immediately for safety
        return true;
    }

    public void deleteClaim(UUID claimId) {
        Claim claim = claims.remove(claimId);
        if (claim != null) {
            PlayerData data = getPlayerData(claim.getOwnerId());
            data.decrementClaimCount();
            saveData();
        }
    }

    private boolean isOverlapping(int minX1, int minZ1, int maxX1, int maxZ1, Claim c2) {
        return minX1 <= c2.getMaxX() && maxX1 >= c2.getMinX() &&
                minZ1 <= c2.getMaxZ() && maxZ1 >= c2.getMinZ();
    }

    private boolean isTooClose(int minX1, int minZ1, int maxX1, int maxZ1, Claim c2, int buffer) {
        return minX1 - buffer <= c2.getMaxX() && maxX1 + buffer >= c2.getMinX() &&
                minZ1 - buffer <= c2.getMaxZ() && maxZ1 + buffer >= c2.getMinZ();
    }
}
