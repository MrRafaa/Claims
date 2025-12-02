package Claims.claims.listeners;

import Claims.claims.Claims;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class GuiListener implements Listener {

    public GuiListener(Claims plugin) {

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof ClaimsGui) {
            event.setCancelled(true);
            ((ClaimsGui) holder).handleClick(event);
        }
    }

    public interface ClaimsGui extends InventoryHolder {
        void handleClick(InventoryClickEvent event);
    }
}
