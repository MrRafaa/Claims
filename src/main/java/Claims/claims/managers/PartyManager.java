package Claims.claims.managers;

import Claims.claims.Claims;
import Claims.claims.models.Party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartyManager {
    private final Claims plugin;
    private final Map<UUID, Party> parties = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> pendingInvites = new ConcurrentHashMap<>(); // Target -> PartyID

    public PartyManager(Claims plugin) {
        this.plugin = plugin;
        loadParties();
    }

    private void loadParties() {
        plugin.getStorageManager().loadParties().thenAccept(loadedParties -> {
            for (Party party : loadedParties) {
                parties.put(party.getId(), party);
            }
            plugin.getLogger().info("Loaded " + parties.size() + " parties.");
        });
    }

    public void saveParties() {
        plugin.getStorageManager().saveParties(new ArrayList<>(parties.values()));
    }

    public Party getParty(UUID partyId) {
        return parties.get(partyId);
    }

    public Party getPlayerParty(UUID playerId) {
        for (Party party : parties.values()) {
            if (party.isMember(playerId)) {
                return party;
            }
        }
        return null; // Optimized lookup could be added to PlayerData
    }

    public Party createParty(Player owner, String name) {
        if (getPlayerParty(owner.getUniqueId()) != null)
            return null; // Already in party

        Party party = new Party(name, owner.getUniqueId());
        parties.put(party.getId(), party);

        // Update PlayerData
        // plugin.getClaimManager().getPlayerData(owner.getUniqueId()).setCurrentPartyId(party.getId());

        saveParties();
        return party;
    }

    public void disbandParty(Party party) {
        parties.remove(party.getId());
        // Logic to handle claims? For now, claims stay with owner or get deleted?
        // Requirement said: "Prevent party with claims from being disbanded"
        saveParties();
    }

    public boolean invitePlayer(Player sender, Player target) {
        Party party = getPlayerParty(sender.getUniqueId());
        if (party == null || !party.getOwnerId().equals(sender.getUniqueId()))
            return false;
        if (party.getMembers().size() >= plugin.getConfigManager().getMaxPartyMembers())
            return false;
        if (getPlayerParty(target.getUniqueId()) != null)
            return false;

        pendingInvites.put(target.getUniqueId(), party.getId());

        // Timeout task
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingInvites.get(target.getUniqueId()) != null
                    && pendingInvites.get(target.getUniqueId()).equals(party.getId())) {
                pendingInvites.remove(target.getUniqueId());
                if (target.isOnline()) {
                    target.sendMessage("§cParty invitation from " + party.getName() + " expired.");
                }
            }
        }, plugin.getConfigManager().getPartyInviteTimeout() * 20L);

        return true;
    }

    public boolean acceptInvite(Player player) {
        UUID partyId = pendingInvites.remove(player.getUniqueId());
        if (partyId == null)
            return false;

        Party party = parties.get(partyId);
        if (party == null)
            return false;

        party.addMember(player.getUniqueId());
        saveParties();
        return true;
    }

    public void leaveParty(Player player) {
        Party party = getPlayerParty(player.getUniqueId());
        if (party == null)
            return;

        if (party.getOwnerId().equals(player.getUniqueId())) {
            disbandParty(party);
            player.sendMessage("§cParty disbanded because you left.");
        } else {
            party.removeMember(player.getUniqueId());
            player.sendMessage("§cYou left the party.");
            saveParties();
        }
    }

    public void kickPlayer(Player owner, String targetName) {
        Party party = getPlayerParty(owner.getUniqueId());
        if (party == null || !party.getOwnerId().equals(owner.getUniqueId())) {
            owner.sendMessage("§cYou are not the owner of a party.");
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        UUID targetId = target != null ? target.getUniqueId() : null;

        // If offline, we might need to look up UUID, but for now simple online check or
        // iterate members
        if (targetId == null) {
            // Try to find by name in members (inefficient but works for small parties)
            for (UUID memberId : party.getMembers()) {
                if (Bukkit.getOfflinePlayer(memberId).getName().equalsIgnoreCase(targetName)) {
                    targetId = memberId;
                    break;
                }
            }
        }

        if (targetId == null || !party.isMember(targetId)) {
            owner.sendMessage("§cPlayer not found in party.");
            return;
        }

        if (targetId.equals(owner.getUniqueId())) {
            owner.sendMessage("§cYou cannot kick yourself.");
            return;
        }

        party.removeMember(targetId);
        owner.sendMessage("§aKicked " + targetName);
        if (target != null) {
            target.sendMessage("§cYou were kicked from the party.");
        }
        saveParties();
    }
}
