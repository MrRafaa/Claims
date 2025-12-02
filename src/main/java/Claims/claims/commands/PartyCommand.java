package Claims.claims.commands;

import Claims.claims.Claims;
import Claims.claims.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyCommand implements CommandExecutor, TabCompleter {
    private final Claims plugin;

    public PartyCommand(Claims plugin) {
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
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party create <name>");
                    return true;
                }
                if (plugin.getPartyManager().createParty(player, args[1]) != null) {
                    player.sendMessage("§aParty created!");
                } else {
                    player.sendMessage("§cFailed to create party (already in one?).");
                }
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party invite <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }
                if (plugin.getPartyManager().invitePlayer(player, target)) {
                    player.sendMessage("§aInvited " + target.getName());
                    target.sendMessage("§aYou have been invited to join " + player.getName()
                            + "'s party. Type /party accept to join.");
                } else {
                    player.sendMessage("§cFailed to invite.");
                }
                break;
            case "accept":
                if (plugin.getPartyManager().acceptInvite(player)) {
                    player.sendMessage("§aJoined party!");
                } else {
                    player.sendMessage("§cNo pending invite.");
                }
                break;
            case "info":
                Party party = plugin.getPartyManager().getPlayerParty(player.getUniqueId());
                if (party == null) {
                    player.sendMessage("§cYou are not in a party.");
                    return true;
                }
                player.sendMessage("§6--- Party Info ---");
                player.sendMessage("§eName: " + party.getName());
                player.sendMessage("§eOwner: " + Bukkit.getOfflinePlayer(party.getOwnerId()).getName());
                player.sendMessage("§eMembers: " + party.getMembers().size());
                break;
            default:
                sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6--- Party Help ---");
        player.sendMessage("§e/party create <name>");
        player.sendMessage("§e/party invite <player>");
        player.sendMessage("§e/party accept");
        player.sendMessage("§e/party info");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("create");
            completions.add("invite");
            completions.add("accept");
            completions.add("info");
        }
        return completions;
    }
}
