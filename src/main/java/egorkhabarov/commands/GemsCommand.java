package egorkhabarov.commands;

import egorkhabarov.Gems;
import egorkhabarov.config.ConfigManager;
import egorkhabarov.logger.PluginLogger;
import egorkhabarov.utils.ChatUtils;
import egorkhabarov.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Locale;


public class GemsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        Gems plugin = Gems.getInstance();
        ConfigManager config = plugin.getConfigManager();
        PluginLogger logger = plugin.getPluginLogger();
        Economy economy = plugin.getEconomy();

        if (
            (sender.isOp() || sender.hasPermission("gems.*") || sender.hasPermission("gems.reload"))
            && args.length == 1
            && args[0].equals("reload")) {
            config.reload();
            sender.sendMessage("§aPlugin config successfully reloaded");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("""
                [§a§lGems§7]
                §a
                §7/gems withdrawal §f<§avalue§f:§5int§f> <§ecount:§5int§f> §8- Withdrawal gems
                """);
            return true;
        }

        if (
            (player.hasPermission("gems.*") || player.hasPermission("gems.give"))
            && args[0].equals("give")
            && (args.length == 3 || args.length == 4)
        ) {
            Player receiver = null;
            String receiverName = args[1];
            for (Player onlinePlayer : player.getServer().getOnlinePlayers()) {
                if (receiverName.equals(onlinePlayer.getName())) {
                    receiver = onlinePlayer;
                }
            }
            if (receiver == null) {
                player.sendMessage("§cThis player is offline");
                return true;
            }

            int value;
            try {
                value = Integer.parseInt(args[2]);
                if (value < 1) {
                    player.sendMessage("§cThe number is too small. Minimum §a§l1 Gems");
                    return true;
                }
                int max_value = config.getMaxValue();
                if (value > max_value) {
                    player.sendMessage("§cThe number is too large. Maximum §a§l"+ String.format(Locale.US, "%,d", max_value) + " Gems");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid value. Please enter an integer.");
                return true;
            }

            int amount = 1;
            if (args.length == 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount < 1) {
                        throw new NumberFormatException();
                    }
                    if (amount > 64) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid value. Please enter an integer no greater than 64.");
                    return true;
                }
            }

            String creator = player.getUniqueId().toString();
            String creatorName = player.getName();
            ItemStack gemItem = Utils.createGemsItem(value, amount, creator, creatorName);
            logger.log(creator+":"+creatorName, "give [" + Utils.formatPlayer(receiver)+ "] " + value * gemItem.getAmount() + " (" + value + "*" + gemItem.getAmount() + ") Gems");
            Utils.giveItems(receiver, gemItem);
            player.sendMessage("§aYou give "+ receiverName + " §l" + String.format(Locale.US, "%,d", value * amount) + " Gems");
            receiver.sendMessage("§aAdministrator give you §l" + String.format(Locale.US, "%,d", value * amount) + " Gems");
            return true;
        } else if (args[0].equals("give")) {
            sender.sendMessage("§cUsing the command: /gems give <player> <value> <count>");
            return true;
        }

        if (
            (sender.hasPermission("gems.*") || sender.hasPermission("gems.withdrawal"))
            && args[0].equals("withdrawal")
            && (args.length == 2 || args.length == 3)
        ) {
            int value;
            try {
                if (!args[1].equals("all")) {
                    value = Integer.parseInt(args[1]);
                } else {
                    value = (int) economy.getBalance(player);
                }
                if (value < 1) {
                    player.sendMessage("§cThe number is too small. Minimum §a§l1 Gems");
                    return true;
                }
                int max_value = config.getMaxValue();
                if (value > max_value) {
                    player.sendMessage("§cThe number is too large. Maximum §a§l"+ String.format(Locale.US, "%,d", max_value) + " Gems");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid value. Please enter an integer.");
                return true;
            }
            int amount = 1;
            if (args.length == 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount < 1) {
                        throw new NumberFormatException();
                    }
                    if (amount > 64) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid value. Please enter an integer no greater than 64.");
                    return true;
                }
            }

            int balance = (int) economy.getBalance(player);
            if (balance < value * amount) {
                player.sendMessage("§cYou don't have enough §a§lGems.");
                return true;
            }
            EconomyResponse economyResponse = economy.withdrawPlayer(player, value * amount);
            if (economyResponse.type == EconomyResponse.ResponseType.FAILURE) {
                player.sendMessage("§cFailed to withdraw §a§lGems.");
                return true;
            }

            ItemStack gemItem = Utils.createGemsItem(value, amount, player.getUniqueId().toString(), player.getName());
            logger.log(Utils.formatPlayer(player), "withdrawal "+Utils.formatGems(gemItem));
            Utils.giveItems(player, gemItem);
            ChatUtils.sendWithdrawGemsMessage(player, value * amount);
            return true;
        } else {
            sender.sendMessage("§cUsing the command: /gems withdrawal <value>");
            return true;
        }
    }
}
