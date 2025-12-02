package Claims.claims.listeners;

import Claims.claims.Claims;
import Claims.claims.utils.ClaimTool;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimCreationListener implements Listener {
    private final Claims plugin;
    private final Map<UUID, Location> firstPoints = new HashMap<>();

    public ClaimCreationListener(Claims plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        Player player = event.getPlayer();
        if (!ClaimTool.isClaimTool(player.getInventory().getItemInMainHand()))
            return;

        event.setCancelled(true);

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location loc = event.getClickedBlock().getLocation();
            firstPoints.put(player.getUniqueId(), loc);
            player.sendMessage("§eFirst corner set at " + loc.getBlockX() + ", " + loc.getBlockZ());
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Location loc2 = event.getClickedBlock().getLocation();
            Location loc1 = firstPoints.get(player.getUniqueId());

            if (loc1 == null) {
                player.sendMessage("§cPlease select the first corner with Right-Click first.");
                return;
            }

            if (!loc1.getWorld().equals(loc2.getWorld())) {
                player.sendMessage("§cCorners must be in the same world.");
                return;
            }

            int area = (Math.abs(loc1.getBlockX() - loc2.getBlockX()) + 1)
                    * (Math.abs(loc1.getBlockZ() - loc2.getBlockZ()) + 1);

            if (plugin.getClaimManager().createClaim(player, loc1, loc2)) {
                player.sendMessage("§aClaim created! Area: " + area + " blocks.");
                firstPoints.remove(player.getUniqueId());
            } else {
                player.sendMessage(
                        "§cCould not create claim. Overlap, limit reached, too big, or too close to others.");
            }
        }
    }
}
