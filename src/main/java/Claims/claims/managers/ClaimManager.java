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
    private final Map<UUID, String> pendingNames = new ConcurrentHashMap<>();

    private final Map<Long, Set<UUID>> chunkClaims = new ConcurrentHashMap<>();

    public ClaimManager(Claims plugin) {
        this.plugin = plugin;
        loadData();
    }

    private long getChunkKey(int x, int z) {
        return (long) (x >> 4) & 0xffffffffL | ((long) (z >> 4) & 0xffffffffL) << 32;
    }

    private void addToChunks(Claim claim) {
        int minChunkX = claim.getMinX() >> 4;
        int minChunkZ = claim.getMinZ() >> 4;
        int maxChunkX = claim.getMaxX() >> 4;
        int maxChunkZ = claim.getMaxZ() >> 4;

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                long key = getChunkKey(x * 16, z * 16);
                chunkClaims.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(claim.getId());
            }
        }
    }

    private void removeFromChunks(Claim claim) {
        int minChunkX = claim.getMinX() >> 4;
        int minChunkZ = claim.getMinZ() >> 4;
        int maxChunkX = claim.getMaxX() >> 4;
        int maxChunkZ = claim.getMaxZ() >> 4;

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                long key = getChunkKey(x * 16, z * 16);
                Set<UUID> ids = chunkClaims.get(key);
                if (ids != null) {
                    ids.remove(claim.getId());
                    if (ids.isEmpty()) {
                        chunkClaims.remove(key);
                    }
                }
            }
        }
    }

    public void setPendingName(UUID playerId, String name) {
        pendingNames.put(playerId, name);
    }

    public String getPendingName(UUID playerId) {
        return pendingNames.get(playerId);
    }

    public void removePendingName(UUID playerId) {
        pendingNames.remove(playerId);
    }

    private void loadData() {
        plugin.getStorageManager().loadClaims().thenAccept(loadedClaims -> {
            for (Claim claim : loadedClaims) {
                claims.put(claim.getId(), claim);
                addToChunks(claim);
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

        long key = getChunkKey(x, z);
        Set<UUID> claimIds = chunkClaims.get(key);
        if (claimIds == null)
            return null;

        for (UUID id : claimIds) {
            Claim claim = claims.get(id);
            if (claim != null && claim.getWorldName().equals(worldName) && claim.contains(x, z)) {
                return claim;
            }
        }
        return null;
    }

    public boolean createClaim(Player player, Location min, Location max, String name) {
        if (!min.getWorld().equals(max.getWorld()))
            return false;

        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());

        // Check overlap
        // Optimization: Only check claims in relevant chunks
        int minChunkX = minX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                long key = getChunkKey(cx * 16, cz * 16);
                Set<UUID> ids = chunkClaims.get(key);
                if (ids != null) {
                    for (UUID id : ids) {
                        Claim existing = claims.get(id);
                        if (existing != null && existing.getWorldName().equals(min.getWorld().getName())) {
                            if (isOverlapping(minX, minZ, maxX, maxZ, existing)) {
                                return false; // Overlap
                            }
                        }
                    }
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
        // Check buffer using chunks (expanded range)
        int bufferMinChunkX = (minX - buffer) >> 4;
        int bufferMinChunkZ = (minZ - buffer) >> 4;
        int bufferMaxChunkX = (maxX + buffer) >> 4;
        int bufferMaxChunkZ = (maxZ + buffer) >> 4;

        for (int cx = bufferMinChunkX; cx <= bufferMaxChunkX; cx++) {
            for (int cz = bufferMinChunkZ; cz <= bufferMaxChunkZ; cz++) {
                long key = getChunkKey(cx * 16, cz * 16);
                Set<UUID> ids = chunkClaims.get(key);
                if (ids != null) {
                    for (UUID id : ids) {
                        Claim existing = claims.get(id);
                        if (existing != null && existing.getWorldName().equals(min.getWorld().getName())
                                && !existing.getOwnerId().equals(player.getUniqueId())) {
                            if (isTooClose(minX, minZ, maxX, maxZ, existing, buffer)) {
                                return false; // Too close
                            }
                        }
                    }
                }
            }
        }

        // Check name uniqueness
        if (name != null && getClaimByName(player.getUniqueId(), name) != null) {
            player.sendMessage("§cYou already have a claim with that name.");
            return false;
        }

        Claim newClaim = new Claim(player.getUniqueId(), min.getWorld().getName(), minX, minZ, maxX, maxZ, name);
        claims.put(newClaim.getId(), newClaim);
        addToChunks(newClaim);

        data.incrementClaimCount();
        saveData(); // Save immediately for safety
        return true;
    }

    public void deleteClaim(UUID claimId) {
        Claim claim = claims.remove(claimId);
        if (claim != null) {
            removeFromChunks(claim);
            PlayerData data = getPlayerData(claim.getOwnerId());
            data.decrementClaimCount();
            saveData();
        }
    }

    public void deleteAllClaims(UUID ownerId) {
        List<UUID> toRemove = new ArrayList<>();
        for (Claim claim : claims.values()) {
            if (claim.getOwnerId().equals(ownerId)) {
                toRemove.add(claim.getId());
            }
        }
        for (UUID id : toRemove) {
            deleteClaim(id);
        }
    }

    public Claim getClaimByName(UUID ownerId, String name) {
        for (Claim claim : claims.values()) {
            if (claim.getOwnerId().equals(ownerId) &&
                    (claim.getName() != null && claim.getName().equalsIgnoreCase(name))) {
                return claim;
            }
        }
        return null;
    }

    public void trustAll(Player owner, UUID targetId) {
        for (Claim claim : claims.values()) {
            if (claim.getOwnerId().equals(owner.getUniqueId())) {
                claim.addTrustedPlayer(targetId);
            }
        }
        saveData();
    }

    public void untrustAll(Player owner, UUID targetId) {
        for (Claim claim : claims.values()) {
            if (claim.getOwnerId().equals(owner.getUniqueId())) {
                claim.removeTrustedPlayer(targetId);
            }
        }
        saveData();
    }

    private boolean isOverlapping(int minX1, int minZ1, int maxX1, int maxZ1, Claim c2) {
        return minX1 <= c2.getMaxX() && maxX1 >= c2.getMinX() &&
                minZ1 <= c2.getMaxZ() && maxZ1 >= c2.getMinZ();
    }

    private boolean isTooClose(int minX1, int minZ1, int maxX1, int maxZ1, Claim c2, int buffer) {
        return minX1 - buffer <= c2.getMaxX() && maxX1 + buffer >= c2.getMinX() &&
                minZ1 - buffer <= c2.getMaxZ() && maxZ1 + buffer >= c2.getMinZ();
    }

    public void setPlayerClaimLimit(UUID playerId, int limit) {
        getPlayerData(playerId).setMaxClaims(limit);
        saveData();
    }

    public void setPlayerClaimAreaLimit(UUID playerId, int limit) {
        getPlayerData(playerId).setMaxClaimArea(limit);
        saveData();
    }
}
