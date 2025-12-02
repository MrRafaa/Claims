package Claims.claims.gui;

import Claims.claims.Claims;
import Claims.claims.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class AdminGui extends PaginatedGui {
    private final UUID targetPlayer;

    public AdminGui(Claims plugin, Player admin, UUID targetPlayer) {
        super(plugin, admin,
                targetPlayer == null ? getAllClaimOwners(plugin)
                        : plugin.getClaimManager().getPlayerClaims(targetPlayer),
                targetPlayer == null ? "Admin: All Players" : "Admin: Player Claims");
        this.targetPlayer = targetPlayer;
        updateInventory();
    }

    private static List<UUID> getAllClaimOwners(Claims plugin) {
        Set<UUID> owners = new HashSet<>();
        for (Claim claim : plugin.getClaimManager().getAllClaims()) {
            owners.add(claim.getOwnerId());
        }
        return new ArrayList<>(owners);
    }

    @Override
    protected ItemStack createItem(Object item) {
        if (targetPlayer == null) {
            UUID ownerId = (UUID) item;
            OfflinePlayer p = Bukkit.getOfflinePlayer(ownerId);
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(p);
                meta.setDisplayName("§e" + p.getName());
                meta.setLore(Collections.singletonList("§7Click to view claims"));
                skull.setItemMeta(meta);
            }
            return skull;
        } else {
            Claim claim = (Claim) item;
            ItemStack stack = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                String name = claim.getName() != null ? claim.getName() : "Unnamed Claim";
                meta.setDisplayName("§a" + name);
                meta.setLore(Arrays.asList(
                        "§7World: " + claim.getWorldName(),
                        "§7X: " + claim.getMinX() + " Z: " + claim.getMinZ(),
                        "§cClick to delete"));
                stack.setItemMeta(meta);
            }
            return stack;
        }
    }

    @Override
    protected void onItemClick(Object item, InventoryClickEvent event) {
        if (targetPlayer == null) {
            UUID ownerId = (UUID) item;
            new AdminGui(plugin, player, ownerId).open();
        } else {
            Claim claim = (Claim) item;
            new ConfirmationGui(player, "Delete Claim?", (confirmed) -> {
                if (confirmed) {
                    plugin.getClaimManager().deleteClaim(claim.getId());
                    player.sendMessage("§aClaim deleted.");
                    new AdminGui(plugin, player, targetPlayer).open(); // Refresh
                } else {
                    new AdminGui(plugin, player, targetPlayer).open();
                }
            });
        }
    }
}
