package Claims.claims.listeners;

import Claims.claims.Claims;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListener implements Listener {
    private final Claims plugin;

    public PartyListener(Claims plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Ensure player data exists
        plugin.getClaimManager().getPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Cleanup if needed (e.g. remove from memory if we were unloading data)
        // For now, we keep data loaded or rely on autosave
    }
}
