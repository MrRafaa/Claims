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

import Claims.claims.models.PlayerData;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Arrays;

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

                // Check limit before giving tool
                PlayerData data = plugin.getClaimManager().getPlayerData(player.getUniqueId());
                int maxClaims = data.getMaxClaims() != -1 ? data.getMaxClaims()
                        : plugin.getConfigManager().getMaxClaimsPerPlayer();

                if (data.getClaimCount() >= maxClaims && !player.hasPermission("claims.admin.bypass")) {
                    player.sendMessage("§cYou have reached your claim limit of " + maxClaims + ".");
                    return true;
                }

                // Check if player already has tool
                if (Arrays.stream(player.getInventory().getContents())
                        .anyMatch(ClaimTool::isClaimTool)) {
                    player.sendMessage("§cYou already have a claim tool in your inventory.");
                    return true;
                }

                plugin.getClaimManager().setPendingName(player.getUniqueId(), name);
                player.getInventory().addItem(ClaimTool.getTool());
                player.sendMessage("§aReceived claim tool! Right click corners to claim '" + name + "'.");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
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
                // Check if first argument is a claim name
                Claim claim = plugin.getClaimManager().getClaimByName(player.getUniqueId(), args[0]);
                if (claim != null) {
                    if (args.length < 2) {
                        player.sendMessage("§cUsage: /claim " + args[0] + " trust | untrust <player>");
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("trust")) {
                        handleTrust(player, claim, args, true);
                    } else if (args[1].equalsIgnoreCase("untrust")) {
                        handleTrust(player, claim, args, false);
                    } else {
                        player.sendMessage("§cUnknown subcommand for claim '" + args[0] + "'.");
                    }
                    return true;
                }
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
            case "setarealimit":
                if (args.length < 4) {
                    player.sendMessage("§cUsage: /claim admin setarealimit <player> <amount>");
                    return;
                }
                Player targetAreaLimit = Bukkit.getPlayer(args[2]);
                if (targetAreaLimit == null) {
                    player.sendMessage("§cPlayer not found.");
                    return;
                }
                try {
                    int limit = Integer.parseInt(args[3]);
                    plugin.getClaimManager().setPlayerClaimAreaLimit(targetAreaLimit.getUniqueId(), limit);
                    player.sendMessage("§aSet max claim area for " + targetAreaLimit.getName() + " to " + limit);
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
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
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

    private void handleTrust(Player player, Claim claim, String[] args, boolean trust) {
        if (args.length < 3) {
            // List trusted
            List<String> trustedNames = new ArrayList<>();
            for (UUID uuid : claim.getTrustedPlayers()) {
                trustedNames.add(Bukkit.getOfflinePlayer(uuid).getName());
            }
            player.sendMessage(
                    "§eTrusted players in '" + claim.getName() + "': "
                            + (trustedNames.isEmpty() ? "None" : String.join(", ", trustedNames)));
            return;
        }

        String targetName = args[2];
        if (!trust && targetName.equalsIgnoreCase("all")) {
            new ConfirmationGui(player, "Untrust ALL in '" + claim.getName() + "'?", (confirmed) -> {
                if (confirmed) {
                    claim.getTrustedPlayers().clear();
                    plugin.getClaimManager().saveData();
                    player.sendMessage("§aUntrusted everyone in '" + claim.getName() + "'.");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            });
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage("§cPlayer not found.");
            return;
        }

        if (trust) {
            if (claim.getTrustedPlayers().size() >= plugin.getConfigManager().getMaxTrusted()) {
                player.sendMessage("§cThis claim has reached the maximum number of trusted players ("
                        + plugin.getConfigManager().getMaxTrusted() + ").");
                return;
            }
            if (claim.isTrusted(target.getUniqueId())) {
                player.sendMessage("§cPlayer is already trusted.");
                return;
            }
            claim.addTrustedPlayer(target.getUniqueId());
            player.sendMessage("§aTrusted " + target.getName() + " in '" + claim.getName() + "'.");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        } else {
            if (!claim.isTrusted(target.getUniqueId())) {
                player.sendMessage("§cPlayer is not trusted.");
                return;
            }
            claim.removeTrustedPlayer(target.getUniqueId());
            player.sendMessage("§aUntrusted " + target.getName() + " in '" + claim.getName() + "'.");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
        plugin.getClaimManager().saveData();
    }

    private void sendHelp(Player player, String topic) {
        player.sendMessage("§6--- Claim Help ---");
        player.sendMessage("§e/claim create <name> §7- Get claim tool");
        player.sendMessage("§e/claim list §7- Manage your claims (GUI)");
        player.sendMessage("§e/claim <name> trust | untrust <player> | all §7- Manage trust");
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
            for (Claim claim : plugin.getClaimManager().getPlayerClaims(((Player) sender).getUniqueId())) {
                if (claim.getName() != null)
                    completions.add(claim.getName());
            }
            if (sender.hasPermission("claims.admin")) {
                completions.add("admin");
            }
        }
        return completions;
    }
}
