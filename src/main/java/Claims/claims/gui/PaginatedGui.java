package Claims.claims.gui;

import Claims.claims.Claims;
import Claims.claims.listeners.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class PaginatedGui implements GuiListener.ClaimsGui {
    protected final Claims plugin;
    protected final Player player;
    protected final Inventory inventory;
    protected int page = 0;
    protected final List<?> items;

    public PaginatedGui(Claims plugin, Player player, List<?> items, String title) {
        this.plugin = plugin;
        this.player = player;
        this.items = items;
        this.inventory = Bukkit.createInventory(this, 54, title);
    }

    protected void updateInventory() {
        inventory.clear();
        int maxItemsPerPage = 45;
        int startIndex = page * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            inventory.setItem(i - startIndex, createItem(items.get(i)));
        }

        // Navigation buttons
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§ePrevious Page");
                prev.setItemMeta(meta);
            }
            inventory.setItem(45, prev);
        }

        if (endIndex < items.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§eNext Page");
                next.setItemMeta(meta);
            }
            inventory.setItem(53, next);
        }
    }

    protected abstract ItemStack createItem(Object item);

    protected abstract void onItemClick(Object item, InventoryClickEvent event);

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;

        if (event.getSlot() == 45 && page > 0) {
            page--;
            updateInventory();
            return;
        }

        if (event.getSlot() == 53 && (page + 1) * 45 < items.size()) {
            page++;
            updateInventory();
            return;
        }

        if (event.getSlot() < 45) {
            int index = page * 45 + event.getSlot();
            if (index < items.size()) {
                onItemClick(items.get(index), event);
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
