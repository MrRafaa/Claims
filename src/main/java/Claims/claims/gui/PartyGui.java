package Claims.claims.gui;

import Claims.claims.Claims;
import Claims.claims.listeners.GuiListener;
import Claims.claims.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;
import java.util.UUID;

public class PartyGui implements GuiListener.ClaimsGui {

    private final Player player;
    private final Party party;
    private final Inventory inventory;

    public PartyGui(Claims plugin, Player player, Party party) {

        this.player = player;
        this.party = party;
        this.inventory = Bukkit.createInventory(this, 54, "Party: " + party.getName());
        initializeItems();
    }

    private void initializeItems() {
        // Members
        int slot = 0;
        for (UUID memberId : party.getMembers()) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(memberId));
                meta.setDisplayName("§e" + Bukkit.getOfflinePlayer(memberId).getName());
                if (party.getOwnerId().equals(player.getUniqueId()) && !memberId.equals(player.getUniqueId())) {
                    meta.setLore(Collections.singletonList("§cClick to kick"));
                } else if (memberId.equals(party.getOwnerId())) {
                    meta.setLore(Collections.singletonList("§6Owner"));
                }
                skull.setItemMeta(meta);
            }
            inventory.setItem(slot++, skull);
        }

        // Leave Button
        ItemStack leave = new ItemStack(Material.RED_WOOL);
        ItemMeta leaveMeta = leave.getItemMeta();
        if (leaveMeta != null) {
            leaveMeta.setDisplayName("§cLeave Party");
            leave.setItemMeta(leaveMeta);
        }
        inventory.setItem(49, leave);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;

        if (event.getSlot() == 49) {
            player.closeInventory();
            player.performCommand("party leave");
            return;
        }

        if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            if (!party.getOwnerId().equals(player.getUniqueId()))
                return;

            SkullMeta meta = (SkullMeta) event.getCurrentItem().getItemMeta();
            if (meta != null && meta.getOwningPlayer() != null) {
                Player target = meta.getOwningPlayer().getPlayer();
                if (target != null && !target.getUniqueId().equals(player.getUniqueId())) {
                    player.closeInventory();
                    player.performCommand("party kick " + target.getName());
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open() {
        player.openInventory(inventory);
    }
}
