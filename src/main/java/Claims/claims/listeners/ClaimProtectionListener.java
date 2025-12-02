package Claims.claims.listeners;

import Claims.claims.Claims;
import Claims.claims.models.Claim;
import Claims.claims.models.Party;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Iterator;
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
        if (claim.getPartyId() != null) {
            Party claimParty = plugin.getPartyManager().getParty(claim.getPartyId());
            if (claimParty != null && claimParty.isMember(player.getUniqueId()))
                return true;
        }

        // Also check if player is in same party as owner (if claim not explicitly
        // assigned to party, but owner is in party)
        Party ownerParty = plugin.getPartyManager().getPlayerParty(claim.getOwnerId());
        if (ownerParty != null && ownerParty.isMember(player.getUniqueId()))
            return true;

        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().isBlockBreakProtection())
            return;
        if (!canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot build here.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfigManager().isBlockPlaceProtection())
            return;
        if (!canBuild(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot build here.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;
        // Interact usually falls under block break or place or general interaction,
        // but we can add a specific config if needed. For now, let's assume it follows
        // block place/break or just general protection.
        // User didn't specify interact protection config, but "protection" generally
        // implies it.
        // Let's check block break protection as a fallback or add a new one?
        // Actually, let's just leave it as is but ensure canBuild is correct.
        // But wait, user said "break and place blocks... as well as explosion, entity
        // damage, fire spread".
        // Interact is often separate. Let's assume it's protected if block-break/place
        // is, or just always protected in claims for untrusted.
        // Let's stick to canBuild logic.
        if (!canBuild(event.getPlayer(), event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
            // event.getPlayer().sendMessage("§cYou cannot interact here."); // Spammy
        }
    }

    @EventHandler
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        if (!plugin.getConfigManager().isExplosionProtection())
            return;

        Iterator<org.bukkit.block.Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            org.bukkit.block.Block block = iterator.next();
            Claim claim = plugin.getClaimManager().getClaimAt(block.getLocation());
            if (claim != null) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onBlockExplode(org.bukkit.event.block.BlockExplodeEvent event) {
        if (!plugin.getConfigManager().isExplosionProtection())
            return;

        Iterator<org.bukkit.block.Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            org.bukkit.block.Block block = iterator.next();
            Claim claim = plugin.getClaimManager().getClaimAt(block.getLocation());
            if (claim != null) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if (plugin.getConfigManager().isPvpInClaims())
                return;
            Claim claim = plugin.getClaimManager().getClaimAt(event.getEntity().getLocation());
            if (claim != null) {
                event.setCancelled(true);
                event.getDamager().sendMessage("§cPvP is disabled in claims.");
            }
        } else if (event.getDamager() instanceof Player) {
            if (!plugin.getConfigManager().isEntityDamageProtection())
                return;
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
                    if (!plugin.getConfigManager().isEntityDamageProtection())
                        return;
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
        if (!plugin.getConfigManager().isEntityDamageProtection())
            return;
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
        if (!plugin.getConfigManager().isEntityDamageProtection())
            return; // Assuming interact falls under entity protection or add new config
        if (!canBuild(event.getPlayer(), event.getRightClicked().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (!plugin.getConfigManager().isEntityDamageProtection())
            return;
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
        if (!plugin.getConfigManager().isEntityDamageProtection())
            return;
        if (event.getAttacker() instanceof Player) {
            Player player = (Player) event.getAttacker();
            if (!canBuild(player, event.getVehicle().getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot destroy vehicles here.");
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(org.bukkit.event.block.BlockIgniteEvent event) {
        if (!plugin.getConfigManager().isFireSpreadProtection())
            return;
        // Check for SPREAD, LAVA, FIREBALL, LIGHTNING
        switch (event.getCause()) {
            case SPREAD:
            case LAVA:
            case FIREBALL:
            case LIGHTNING:
                Claim claim = plugin.getClaimManager().getClaimAt(event.getBlock().getLocation());
                if (claim != null) {
                    event.setCancelled(true);
                }
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onBlockBurn(org.bukkit.event.block.BlockBurnEvent event) {
        if (!plugin.getConfigManager().isFireSpreadProtection())
            return;
        Claim claim = plugin.getClaimManager().getClaimAt(event.getBlock().getLocation());
        if (claim != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(org.bukkit.event.entity.EntityChangeBlockEvent event) {
        if (!plugin.getConfigManager().isMobGriefingProtection())
            return;

        // Prevent Endermen, Ravagers, Wither, etc. from changing blocks
        if (event.getEntity() instanceof org.bukkit.entity.Monster
                || event.getEntity() instanceof org.bukkit.entity.Wither) {
            Claim claim = plugin.getClaimManager().getClaimAt(event.getBlock().getLocation());
            if (claim != null) {
                event.setCancelled(true);
            }
        }
    }
}
