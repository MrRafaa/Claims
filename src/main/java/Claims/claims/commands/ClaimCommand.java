package Claims.claims.commands;

import Claims.claims.Claims;
import Claims.claims.managers.ClaimManager;
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
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                player.getInventory().addItem(ClaimTool.getTool());
                player.sendMessage("§aReceived claim tool!");
                break;
            case "delete":
                handleDelete(player);
                break;
            case "info":
                handleInfo(player);
                break;
            case "list":
                handleList(player);
                break;
            case "trust":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /claim trust <player>");
                    return true;
                }
                handleTrust(player, args[1], true);
                break;
            case "untrust":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /claim untrust <player>");
                    return true;
                }
                handleTrust(player, args[1], false);
                break;
            case "admin":
                if (!player.hasPermission("claims.admin")) {
                    player.sendMessage("§cNo permission.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /claim admin <delete|bypass|setlimit|setarealimit>");
                    return true;
                }
                handleAdmin(player, args);
                break;
            default:
                player.sendMessage("§cUnknown subcommand. Type /claim help for a list of commands.");
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
                    List<Claim> targetClaims = new ArrayList<>(
                            plugin.getClaimManager().getPlayerClaims(target.getUniqueId()));
                    for (Claim c : targetClaims) {
                        plugin.getClaimManager().deleteClaim(c.getId());
                    }
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
                    plugin.getClaimManager().getPlayerData(targetLimit.getUniqueId()).setMaxClaims(limit);
                    plugin.getClaimManager().saveData();
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
                Player targetArea = Bukkit.getPlayer(args[2]);
                if (targetArea == null) {
                    player.sendMessage("§cPlayer not found.");
                    return;
                }
                try {
                    int limit = Integer.parseInt(args[3]);
                    plugin.getClaimManager().getPlayerData(targetArea.getUniqueId()).setMaxClaimArea(limit);
                    plugin.getClaimManager().saveData();
                    player.sendMessage("§aSet max claim area for " + targetArea.getName() + " to " + limit);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid number.");
                }
                break;
            default:
                player.sendMessage("§cUnknown admin command.");
        }
    }

    private void handleDelete(Player player) {
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage("§cNo claim here.");
            return;
        }
        if (!claim.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("claims.admin")) {
            player.sendMessage("§cYou don't own this claim.");
            return;
        }
        plugin.getClaimManager().deleteClaim(claim.getId());
        player.sendMessage("§aClaim deleted.");
    }

    private void handleInfo(Player player) {
        Claim claim = plugin.getClaimManager().getClaimAt(player.getLocation());
        if (claim == null) {
            player.sendMessage("§cNo claim here.");
            return;
        }
        player.sendMessage("§6--- Claim Info ---");
        player.sendMessage("§eOwner: " + Bukkit.getOfflinePlayer(claim.getOwnerId()).getName());
        player.sendMessage(
                "§eArea: " + ((claim.getMaxX() - claim.getMinX() + 1) * (claim.getMaxZ() - claim.getMinZ() + 1)));
    }

    private void handleList(Player player) {
        List<Claim> claims = plugin.getClaimManager().getPlayerClaims(player.getUniqueId());
        if (claims.isEmpty()) {
            player.sendMessage("§eYou have no claims.");
            return;
        }
        player.sendMessage("§6--- Your Claims ---");
        for (Claim claim : claims) {
            player.sendMessage("§e- " + claim.getWorldName() + " (" + claim.getMinX() + ", " + claim.getMinZ() + ")");
        }
    }

    private void handleTrust(Player player, String targetName, boolean trust) {
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

    private void sendHelp(Player player) {
        player.sendMessage("§6--- Claim Help ---");
        player.sendMessage("§e/claim create §7- Get claim tool");
        player.sendMessage("§e/claim delete §7- Delete claim at your location");
        player.sendMessage("§e/claim info §7- View info about claim at location");
        player.sendMessage("§e/claim list §7- List your claims");
        player.sendMessage("§e/claim trust <player> §7- Trust a player");
        player.sendMessage("§e/claim untrust <player> §7- Untrust a player");
        if (player.hasPermission("claims.admin")) {
            player.sendMessage("§c/claim admin delete [player] §7- Delete claim(s)");
            player.sendMessage("§c/claim admin setlimit <player> <amount> §7- Set max claims");
            player.sendMessage("§c/claim admin setarealimit <player> <amount> §7- Set max area");
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
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            completions.add("delete");
            completions.add("setlimit");
            completions.add("setarealimit");
        }
        return completions;
    }
}
