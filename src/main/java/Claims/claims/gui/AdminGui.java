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

                List<String> trustedNames = new ArrayList<>();
                for (UUID trusted : claim.getTrustedPlayers()) {
                    trustedNames.add(Bukkit.getOfflinePlayer(trusted).getName());
                }
                String trustedList = trustedNames.isEmpty() ? "None" : String.join(", ", trustedNames);
                int area = (claim.getMaxX() - claim.getMinX() + 1) * (claim.getMaxZ() - claim.getMinZ() + 1);

                meta.setLore(Arrays.asList(
                        "§7World: " + claim.getWorldName(),
                        "§7X: " + claim.getMinX() + " Z: " + claim.getMinZ(),
                        "§7Area: " + area + " blocks",
                        "§7Trusted: " + trustedList,
                        "",
                        "§eLeft-Click to Teleport",
                        "§cRight-Click to Delete"));
                stack.setItemMeta(meta);
            }
            return stack;
        }
    }

    @Override
    protected void updateInventory() {
        super.updateInventory();
        if (targetPlayer != null) {
            ItemStack returnItem = new ItemStack(Material.ARROW);
            ItemMeta meta = returnItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§eReturn");
                returnItem.setItemMeta(meta);
            }
            inventory.setItem(49, returnItem);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        super.handleClick(event);
        if (targetPlayer != null && event.getSlot() == 49) {
            new AdminGui(plugin, player, null).open();
        }
    }

    @Override
    protected void onItemClick(Object item, InventoryClickEvent event) {
        if (targetPlayer == null) {
            UUID ownerId = (UUID) item;
            new AdminGui(plugin, player, ownerId).open();
        } else {
            Claim claim = (Claim) item;
            if (event.isLeftClick()) {
                // Teleport
                org.bukkit.World world = Bukkit.getWorld(claim.getWorldName());
                if (world != null) {
                    // Teleport to the center or min corner + 1 up
                    int x = (claim.getMinX() + claim.getMaxX()) / 2;
                    int z = (claim.getMinZ() + claim.getMaxZ()) / 2;
                    int y = world.getHighestBlockYAt(x, z) + 1;
                    player.teleport(new org.bukkit.Location(world, x, y, z));
                    player.sendMessage(
                            "§aTeleported to claim '" + (claim.getName() != null ? claim.getName() : "Unnamed") + "'.");
                    player.closeInventory();
                } else {
                    player.sendMessage("§cWorld not loaded.");
                }
            } else if (event.isRightClick()) {
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
}
