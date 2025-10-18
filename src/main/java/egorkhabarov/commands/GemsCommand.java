package egorkhabarov.commands;

import egorkhabarov.Gems;
import egorkhabarov.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Locale;
import java.util.Map;

public class GemsCommand implements CommandExecutor {
    private final Gems plugin;

    public GemsCommand(Gems gems) {
        this.plugin = gems;
    }

    @Override
    public boolean onCommand(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (
            (sender.isOp() || sender.hasPermission("gems.*") || sender.hasPermission("gems.reload"))
            && args.length == 1
            && args[0].equals("reload")) {
            this.plugin.getConfigManager().reload();
            sender.sendMessage("§aPlugin config successfully reloaded");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько игроки могут использовать эту команду!");
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
                player.sendMessage("§cЭтот игрок не в сети");
                return true;
            }

            int value;
            try {
                value = Integer.parseInt(args[2]);
                if (value < 1) {
                    player.sendMessage("§cЧисло слишком маленькое. Минимум §a§l1 Gems");
                    return true;
                }
                int max_value = this.plugin.getConfigManager().getMaxValue();
                if (value > max_value) {
                    player.sendMessage("§cЧисло слишком большое. Максимум §a§l"+ String.format(Locale.US, "%,d", max_value) + " Gems");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cНеверное значение. Введите целое число.");
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
                    player.sendMessage("§cНеверное значение. Введите целое число не больше 64.");
                    return true;
                }
            }

            String creator = player.getUniqueId().toString();
            String creatorName = player.getName();
            ItemStack gemItem = Utils.createGemsItem(value, amount, creator, creatorName);
            this.plugin.log(creator+":"+creatorName, "give [" + this.plugin.formatPlayer(receiver)+ "] " + value * gemItem.getAmount() + " (" + value + "*" + gemItem.getAmount() + ") Gems");
            this.giveItems(receiver, gemItem);
            player.sendMessage("§aYou give "+ receiverName + " §l" + String.format(Locale.US, "%,d", value * amount) + " Gems");
            receiver.sendMessage("§aAdministrator give you §l" + String.format(Locale.US, "%,d", value * amount) + " Gems");
            return true;
        } else if (args[0].equals("give")) {
            sender.sendMessage("§cИспользование команды: /gems give <player> <value> <count>");
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
                    value = (int) this.plugin.getEconomy().getBalance(player);
                }
                if (value < 1) {
                    player.sendMessage("§cЧисло слишком маленькое. Минимум §a§l1 Gems");
                    return true;
                }
                int max_value = this.plugin.getConfigManager().getMaxValue();
                if (value > max_value) {
                    player.sendMessage("§cЧисло слишком большое. Максимум §a§l"+ String.format(Locale.US, "%,d", max_value) + " Gems");
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage("§cНеверное значение. Введите целое число.");
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
                    player.sendMessage("§cНеверное значение. Введите целое число не больше 64.");
                    return true;
                }
            }

            int balance = (int) this.plugin.getEconomy().getBalance(player);
            if (balance < value * amount) {
                player.sendMessage("§cYou don't have enough §a§lGems.");
                return true;
            }
            EconomyResponse economyResponse = this.plugin.getEconomy().withdrawPlayer(player, value * amount);
            if (economyResponse.type == EconomyResponse.ResponseType.FAILURE) {
                player.sendMessage("§cFailed to withdraw §a§lGems.");
                return true;
            }

            Gems.logger.info(value +" "+ amount +" "+ player.getUniqueId().toString() +" "+ player.getName());
            ItemStack gemItem = Utils.createGemsItem(value, amount, player.getUniqueId().toString(), player.getName());
            this.plugin.log(this.plugin.formatPlayer(player), "withdrawal "+this.plugin.formatGems(gemItem));
            this.giveItems(player, gemItem);
            player.sendMessage("§aYou withdrew §l" + String.format(Locale.US, "%,d", value * amount) + " Gems");
            return true;
        } else {
            sender.sendMessage("§cИспользование команды: /gems withdrawal <value>");
            return true;
        }
    }

    public void giveItems(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftoverItems = player.getInventory().addItem(item);

        // Проверяем, были ли предметы, которые не поместились в инвентарь
        if (!leftoverItems.isEmpty()) {
            // Мы можем вернуть оставшиеся предметы в виде обычных ItemStack
            for (ItemStack leftoverItem : leftoverItems.values()) {
                // Если предметы не помещаются, можно вернуть их игроку
                player.getWorld().dropItem(player.getLocation(), leftoverItem);
            }
        }
    }
}
