package Claims.claims.commands;

import Claims.claims.Claims;
import Claims.claims.gui.PartyGui;
import Claims.claims.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
                String partyName = args[1];
                if (partyName.length() > 12) {
                    player.sendMessage("§cParty name cannot exceed 12 characters.");
                    return true;
                }
                if (plugin.getPartyManager().createParty(player, partyName) != null) {
                    player.sendMessage("§aParty '" + partyName + "' created!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                } else {
                    player.sendMessage("§cYou are already in a party or name is taken.");
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
                    if (plugin.getPartyManager().getPlayerParty(target.getUniqueId()) != null) {
                        target.sendMessage("§aYou have been invited to join " + player.getName()
                                + "'s party. §eAccepting will leave your current party.");
                    } else {
                        target.sendMessage("§aYou have been invited to join " + player.getName()
                                + "'s party. Type /party accept to join.");
                    }
                } else {
                    player.sendMessage("§cFailed to invite. Party full or player already invited.");
                }
                break;

            case "accept":
                if (plugin.getPartyManager().acceptInvite(player)) {
                    player.sendMessage("§aJoined party!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                } else {
                    player.sendMessage("§cNo pending invite.");
                }
                break;

            case "leave":
                plugin.getPartyManager().leaveParty(player);
                break;

            case "kick":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /party kick <player>");
                    return true;
                }
                plugin.getPartyManager().kickPlayer(player, args[1]);
                break;

            case "info":
            case "gui":
                Party party = plugin.getPartyManager().getPlayerParty(player.getUniqueId());
                if (party == null) {
                    player.sendMessage("§cYou are not in a party.");
                    return true;
                }
                new PartyGui(plugin, player, party).open();
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
        player.sendMessage("§e/party leave");
        player.sendMessage("§e/party kick <player>");
        player.sendMessage("§e/party info §7- Open Party GUI");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("create");
            completions.add("invite");
            completions.add("accept");
            completions.add("leave");
            completions.add("kick");
            completions.add("info");
        }
        return completions;
    }
}
