package Claims.claims.listeners;

import Claims.claims.Claims;
import Claims.claims.models.Claim;
import Claims.claims.utils.ClaimUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ClaimVisualizationListener implements Listener {
    private final Claims plugin;

    public ClaimVisualizationListener(Claims plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
            return;

        Claim toClaim = plugin.getClaimManager().getClaimAt(event.getTo());
        Claim fromClaim = plugin.getClaimManager().getClaimAt(event.getFrom());

        if (toClaim != null && !toClaim.equals(fromClaim)) {
            event.getPlayer().sendTitle("§aEntering Claim",
                    "§7Owner: " + Bukkit.getOfflinePlayer(toClaim.getOwnerId()).getName(), 10, 40, 10);
            ClaimUtils.visualizeClaim(event.getPlayer(), toClaim);
        } else if (toClaim == null && fromClaim != null) {
            event.getPlayer().sendTitle("§cWilderness", "", 10, 40, 10);
        }
    }
}
