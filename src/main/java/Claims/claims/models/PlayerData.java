package Claims.claims.models;

import java.util.UUID;

/**
 * Stores persistent data for a player.
 */
public class PlayerData {
    private final UUID uuid;
    private int claimCount;
    private UUID currentPartyId;
    private int maxClaims = -1; // -1 means use config default
    private int maxClaimArea = -1; // -1 means use config default

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.claimCount = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getClaimCount() {
        return claimCount;
    }

    public void setClaimCount(int claimCount) {
        this.claimCount = claimCount;
    }

    public void incrementClaimCount() {
        this.claimCount++;
    }

    public void decrementClaimCount() {
        if (this.claimCount > 0) {
            this.claimCount--;
        }
    }

    public UUID getCurrentPartyId() {
        return currentPartyId;
    }

    public void setCurrentPartyId(UUID currentPartyId) {
        this.currentPartyId = currentPartyId;
    }

    public int getMaxClaims() {
        return maxClaims;
    }

    public void setMaxClaims(int maxClaims) {
        this.maxClaims = maxClaims;
    }

    public int getMaxClaimArea() {
        return maxClaimArea;
    }

    public void setMaxClaimArea(int maxClaimArea) {
        this.maxClaimArea = maxClaimArea;
    }
}
