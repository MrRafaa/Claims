package Claims.claims.gui;

import Claims.claims.listeners.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class ConfirmationGui implements GuiListener.ClaimsGui {
    private final Inventory inventory;
    private final Consumer<Boolean> callback;

    public ConfirmationGui(Player player, String title, Consumer<Boolean> callback) {
        this.callback = callback;
        this.inventory = Bukkit.createInventory(this, 27, title);
        initializeItems();
        player.openInventory(inventory);
    }

    private void initializeItems() {
        ItemStack confirm = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§aConfirm");
            confirm.setItemMeta(confirmMeta);
        }
        inventory.setItem(11, confirm);

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName("§cCancel");
            cancel.setItemMeta(cancelMeta);
        }
        inventory.setItem(15, cancel);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null)
            return;

        if (event.getSlot() == 11) {
            event.getWhoClicked().closeInventory();
            callback.accept(true);
        } else if (event.getSlot() == 15) {
            event.getWhoClicked().closeInventory();
            callback.accept(false);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
