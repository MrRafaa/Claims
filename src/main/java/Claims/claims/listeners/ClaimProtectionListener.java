package Claims.claims.listeners;

import Claims.claims.Claims;
import Claims.claims.models.Claim;
import Claims.claims.models.Party;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public class ClaimProtectionListener implements Listener {
    private final Claims plugin;

    public ClaimProtectionListener(Claims plugin) {
        this.plugin = plugin;
    }

    private boolean canBuild(Player player, Location location) {
        if (player.hasPermission("claims.admin.bypass"))
            return true;

        Claim claim = plugin.getClaimManager().getClaimAt(location);
        if (claim == null)
            return true; // Wilderness

        // Check ownership
        if (claim.getOwnerId().equals(player.getUniqueId()))
            return true;

        // Check trust
        if (claim.isTrusted(player.getUniqueId()))
            return true;

        // Check party
        Party claimParty = plugin.getPartyManager().getParty(claim.getPartyId());
        if (claimParty != null && claimParty.isMember(player.getUniqueId()))
            return true;

        // Also check if player is in same party as owner (if claim not explicitly
        // assigned to party, but owner is in party)
        Party ownerParty = plugin.getPartyManager().getPlayerParty(claim.getOwnerId());
        if (ownerParty != null && ownerParty.isMember(player.getUniqueId()))
            return true;

        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot build here.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot build here.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;
        if (!canBuild(event.getPlayer(), event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
            // event.getPlayer().sendMessage("§cYou cannot interact here."); // Spammy
        }
    }

    @EventHandler
    public void onPVP(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (plugin.getConfigManager().isPvpInClaims())
                return;
            Claim claim = plugin.getClaimManager().getClaimAt(event.getEntity().getLocation());
            if (claim != null) {
                event.setCancelled(true);
                event.getDamager().sendMessage("§cPvP is disabled in claims.");
            }
        } else if (event.getDamager() instanceof Player) {
            // Protect animals/monsters/entities in claim
            Player player = (Player) event.getDamager();
            if (!canBuild(player, event.getEntity().getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot attack entities here.");
            }
        } else if (event.getDamager() instanceof Projectile) {
            Projectile proj = (Projectile) event.getDamager();
            if (proj.getShooter() instanceof Player) {
                Player player = (Player) proj.getShooter();
                if (event.getEntity() instanceof Player) {
                    if (plugin.getConfigManager().isPvpInClaims())
                        return;
                    Claim claim = plugin.getClaimManager().getClaimAt(event.getEntity().getLocation());
                    if (claim != null) {
                        event.setCancelled(true);
                        player.sendMessage("§cPvP is disabled in claims.");
                    }
                } else {
                    if (!canBuild(player, event.getEntity().getLocation())) {
                        event.setCancelled(true);
                        player.sendMessage("§cYou cannot attack entities here.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            Player player = (Player) event.getRemover();
            if (!canBuild(player, event.getEntity().getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot break this.");
            }
        } else if (event.getRemover() instanceof Projectile) {
            Projectile proj = (Projectile) event.getRemover();
            if (proj.getShooter() instanceof Player) {
                Player player = (Player) proj.getShooter();
                if (!canBuild(player, event.getEntity().getLocation())) {
                    event.setCancelled(true);
                    player.sendMessage("§cYou cannot break this.");
                }
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!canBuild(event.getPlayer(), event.getRightClicked().getLocation())) {
            event.setCancelled(true);
            // event.getPlayer().sendMessage("§cYou cannot interact with entities here.");
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getAttacker() instanceof Player) {
            Player player = (Player) event.getAttacker();
            if (!canBuild(player, event.getVehicle().getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot damage vehicles here.");
            }
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getAttacker() instanceof Player) {
            Player player = (Player) event.getAttacker();
            if (!canBuild(player, event.getVehicle().getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot destroy vehicles here.");
            }
        }
    }
}
