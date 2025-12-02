package Claims.claims.gui;

import Claims.claims.Claims;
import Claims.claims.listeners.GuiListener;
import Claims.claims.models.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimDetailGui implements GuiListener.ClaimsGui {
    private final Claims plugin;
    private final Player player;
    private final Claim claim;
    private final Inventory inventory;

    public ClaimDetailGui(Claims plugin, Player player, Claim claim) {
        this.plugin = plugin;
        this.player = player;
        this.claim = claim;
        this.inventory = Bukkit.createInventory(this, 27,
                "Claim: " + (claim.getName() != null ? claim.getName() : "Unnamed"));
        initializeItems();
        player.openInventory(inventory);
    }

    private void initializeItems() {
        // Info Item
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§eClaim Info");
            List<String> lore = new ArrayList<>();
            lore.add("§7World: " + claim.getWorldName());
            lore.add("§7Coords: " + claim.getMinX() + "," + claim.getMinZ() + " to " + claim.getMaxX() + ","
                    + claim.getMaxZ());
            int area = (claim.getMaxX() - claim.getMinX() + 1) * (claim.getMaxZ() - claim.getMinZ() + 1);
            lore.add("§7Area: " + area);

            List<String> trustedNames = new ArrayList<>();
            for (UUID uuid : claim.getTrustedPlayers()) {
                trustedNames.add(Bukkit.getOfflinePlayer(uuid).getName());
            }
            lore.add("§7Trusted: " + (trustedNames.isEmpty() ? "None" : String.join(", ", trustedNames)));
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        inventory.setItem(13, info);

        // Delete Button (Only for owner or admin)
        if (player.getUniqueId().equals(claim.getOwnerId()) || player.hasPermission("claims.admin")) {
            ItemStack delete = new ItemStack(Material.RED_WOOL);
            ItemMeta deleteMeta = delete.getItemMeta();
            if (deleteMeta != null) {
                deleteMeta.setDisplayName("§cDelete Claim");
                delete.setItemMeta(deleteMeta);
            }
            inventory.setItem(22, delete);
        }

        // Back Button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Back");
            back.setItemMeta(backMeta);
        }
        inventory.setItem(18, back);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta())
            return;

        String displayName = clicked.getItemMeta().getDisplayName();

        if (displayName.equals("§cDelete Claim")) {
            new ConfirmationGui(player, "Delete this claim?", (confirmed) -> {
                if (confirmed) {
                    plugin.getClaimManager().deleteClaim(claim.getId());
                    player.sendMessage("§aClaim deleted.");
                    player.closeInventory();
                } else {
                    new ClaimDetailGui(plugin, player, claim);
                }
            });
        } else if (displayName.equals("§7Back")) {
            new PlayerClaimsGui(plugin, player, plugin.getClaimManager().getPlayerClaims(player.getUniqueId())).open();
        }
    }
}
