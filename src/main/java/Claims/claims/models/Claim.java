package Claims.claims.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a land claim.
 */
public class Claim {
    private final UUID id;
    private final UUID ownerId;
    private final String worldName;
    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxZ;
    private final long creationTime;
    private UUID partyId;
    private final Set<UUID> trustedPlayers;

    public Claim(UUID ownerId, String worldName, int minX, int minZ, int maxX, int maxZ) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.worldName = worldName;
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.creationTime = System.currentTimeMillis();
        this.trustedPlayers = new HashSet<>();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public UUID getPartyId() {
        return partyId;
    }

    public void setPartyId(UUID partyId) {
        this.partyId = partyId;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public void addTrustedPlayer(UUID playerId) {
        this.trustedPlayers.add(playerId);
    }

    public void removeTrustedPlayer(UUID playerId) {
        this.trustedPlayers.remove(playerId);
    }

    public boolean isTrusted(UUID playerId) {
        return this.trustedPlayers.contains(playerId);
    }

    /**
     * Checks if a location (x, z) is within this claim.
     * 
     * @param x The X coordinate.
     * @param z The Z coordinate.
     * @return True if inside, false otherwise.
     */
    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
