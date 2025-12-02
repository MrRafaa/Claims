package Claims.claims.commands;

import Claims.claims.Claims;
import Claims.claims.gui.AdminGui;
import Claims.claims.gui.ClaimDetailGui;
import Claims.claims.gui.ConfirmationGui;
import Claims.claims.gui.PlayerClaimsGui;
import Claims.claims.models.Claim;
import Claims.claims.utils.ClaimTool;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimCommand implements CommandExecutor, TabCompleter {
    private final Claims plugin;

    public ClaimCommand(Claims plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player, null);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /claim create <name>");
                    return true;
                }
                String name = args[1];
                if (name.length() > 12) {
                    player.sendMessage("§cClaim name cannot exceed 12 characters.");
                    return true;
                }
                if (plugin.getClaimManager().getClaimByName(player.getUniqueId(), name) != null) {
                    player.sendMessage("§cYou already have a claim with that name.");
                    return true;
                }
                plugin.getClaimManager().setPendingName(player.getUniqueId(), name);
                player.getInventory().addItem(ClaimTool.getTool());
                player.sendMessage("§aReceived claim tool! Right click corners to claim '" + name + "'.");
                break;
            case "delete":
                handleDelete(player, args);
                break;
            case "info":
                handleInfo(player);
                break;
            case "list":
                new PlayerClaimsGui(plugin, player, plugin.getClaimManager().getPlayerClaims(player.getUniqueId()))
                        .open();
                break;
            case "trust":
                handleTrust(player, args, true);
                break;
            case "untrust":
                handleTrust(player, args, false);
                break;
            case "admin":
                if (!player.hasPermission("claims.admin")) {
                    player.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length < 2) {
                    new AdminGui(plugin, player, null).open();
                    return true;
                }
                handleAdmin(player, args);
                break;
            case "help":
                sendHelp(player, args.length > 1 ? args[1] : null);
                break;
            default:
                sendHelp(player, null);
        }
        return true;
    }

    private void handleAdmin(Player player, String[] args) {
        switch (args[1].toLowerCase()) {
            case "delete":
                if (args.length == 3) {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        player.sendMessage("§cPlayer not found.");
                        return;
                    }
                    plugin.getClaimManager().deleteAllClaims(target.getUniqueId());
                    player.sendMessage("§aDeleted all claims for " + target.getName());
                } else {
                    Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
                    if (claim == null) {
                        player.sendMessage("§cNo claim here.");
                        return;
                    }
                    plugin.getClaimManager().deleteClaim(claim.getId());
                    player.sendMessage("§aClaim deleted (Admin).");
                }
                break;
            case "bypass":
                player.sendMessage("§eBypass mode is permission-based. If you have 'claims.admin.bypass', you bypass.");
                break;
            case "setlimit":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /claim admin setlimit <player> <amount>");
                    return;
                }
                Player targetLimit = Bukkit.getPlayer(args[2]);
                if (targetLimit == null) {
                    player.sendMessage("§cPlayer not found.");
                    return;
                }
                try {
                    int limit = Integer.parseInt(args[3]);
                    plugin.getClaimManager().setPlayerClaimLimit(targetLimit.getUniqueId(), limit);
                    player.sendMessage("§aSet max claims for " + targetLimit.getName() + " to " + limit);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid number.");
                }
                break;
            default:
                player.sendMessage("§cUnknown admin command.");
        }
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length > 1) {
            String target = args[1];
            if (target.equalsIgnoreCase("all")) {
                new ConfirmationGui(player, "Delete ALL Claims?", (confirmed) -> {
                    if (confirmed) {
                        plugin.getClaimManager().deleteAllClaims(player.getUniqueId());
                        player.sendMessage("§aDeleted all your claims.");
                    }
                });
                return;
            }
            // Delete by name
            Claim claim = plugin.getClaimManager().getClaimByName(player.getUniqueId(), target);
            if (claim != null) {
                new ConfirmationGui(player, "Delete '" + target + "'?", (confirmed) -> {
                    if (confirmed) {
                        plugin.getClaimManager().deleteClaim(claim.getId());
                        player.sendMessage("§aDeleted claim '" + target + "'.");
                    }
                });
                return;
            }
            player.sendMessage("§cClaim '" + target + "' not found.");
        } else {
            Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
            if (claim == null) {
                player.sendMessage("§cNo claim here.");
                return;
            }
            if (!claim.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("claims.admin")) {
                player.sendMessage("§cYou don't own this claim.");
                return;
            }
            new ConfirmationGui(player, "Delete Claim Here?", (confirmed) -> {
                if (confirmed) {
                    plugin.getClaimManager().deleteClaim(claim.getId());
                    player.sendMessage("§aClaim deleted.");
                }
            });
        }
    }

    private void handleInfo(Player player) {
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage("§cNo claim here.");
            return;
        }
        new ClaimDetailGui(plugin, player, claim);
    }

    private void handleTrust(Player player, String[] args, boolean trust) {
        if (args.length < 2) {
            // List trusted
            Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
            if (claim == null) {
                player.sendMessage("§cNo claim here.");
                return;
            }
            List<String> trustedNames = new ArrayList<>();
            for (UUID uuid : claim.getTrustedPlayers()) {
                trustedNames.add(Bukkit.getOfflinePlayer(uuid).getName());
            }
            player.sendMessage(
                    "§eTrusted players: " + (trustedNames.isEmpty() ? "None" : String.join(", ", trustedNames)));
            return;
        }

        String targetName = args[1];
        if (args.length > 2 && args[2].equalsIgnoreCase("all")) {
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return;
            }
            if (trust)
                plugin.getClaimManager().trustAll(player, target.getUniqueId());
            else
                plugin.getClaimManager().untrustAll(player, target.getUniqueId());
            player.sendMessage(
                    "§a" + (trust ? "Trusted " : "Untrusted ") + target.getName() + " on all your claims.");
            return;
        }

        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage("§cNo claim here.");
            return;
        }
        if (!claim.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou don't own this claim.");
            return;
        }
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return;
        }

        if (trust) {
            claim.addTrustedPlayer(target.getUniqueId());
            player.sendMessage("§aTrusted " + target.getName());
        } else {
            claim.removeTrustedPlayer(target.getUniqueId());
            player.sendMessage("§aUntrusted " + target.getName());
        }
        plugin.getClaimManager().saveData();
    }

    private void sendHelp(Player player, String topic) {
        player.sendMessage("§6--- Claim Help ---");
        player.sendMessage("§e/claim create <name> §7- Get claim tool");
        player.sendMessage("§e/claim list §7- Manage your claims (GUI)");
        player.sendMessage("§e/claim trust | untrust <player> [all] §7- Manage trust");
        player.sendMessage("§e/claim info §7- View claim info");
        player.sendMessage("§e/party §7- Party commands");
        if (player.hasPermission("claims.admin")) {
            player.sendMessage("§c/claim admin §7- Open Admin GUI");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("create");
            completions.add("delete");
            completions.add("info");
            completions.add("list");
            completions.add("trust");
            completions.add("untrust");
            if (sender.hasPermission("claims.admin")) {
                completions.add("admin");
            }
        }
        return completions;
    }
}
