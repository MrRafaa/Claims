package Claims.claims.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a player party.
 */
public class Party {
    private final UUID id;
    private String name;
    private UUID ownerId;
    private final Set<UUID> members;
    private final Set<UUID> claimIds;

    public Party(String name, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.ownerId = ownerId;
        this.members = new HashSet<>();
        this.claimIds = new HashSet<>();
        this.members.add(ownerId);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID memberId) {
        this.members.add(memberId);
    }

    public void removeMember(UUID memberId) {
        this.members.remove(memberId);
    }

    public boolean isMember(UUID memberId) {
        return this.members.contains(memberId);
    }

    public Set<UUID> getClaimIds() {
        return claimIds;
    }

    public void addClaim(UUID claimId) {
        this.claimIds.add(claimId);
    }

    public void removeClaim(UUID claimId) {
        this.claimIds.remove(claimId);
    }
}
