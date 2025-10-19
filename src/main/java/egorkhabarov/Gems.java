package egorkhabarov;

import egorkhabarov.config.ConfigManager;
import egorkhabarov.logger.PluginLogger;
import egorkhabarov.utils.ChatUtils;
import egorkhabarov.commands.GemsCommand;
import egorkhabarov.commands.GemsCommandTabCompleter;
import egorkhabarov.utils.Utils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;


public final class Gems extends JavaPlugin implements Listener {
    private static Gems instance;
    private ConfigManager configManager;
    private PluginLogger pluginLogger;
    private Economy economy;

    @Override
    public void onEnable() {
        Gems.instance = this;
        if (!this.setupEconomy()) {
            this.getLogger().severe("Plugin §6Vault §сnot found on server!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.configManager = new ConfigManager(this);
        this.pluginLogger = new PluginLogger(this);
        this.getServer().getPluginManager().registerEvents(this, this);

        PluginCommand gems = this.getCommand("gems");
        if (gems != null) {
            gems.setExecutor(new GemsCommand(this));
            gems.setTabCompleter(new GemsCommandTabCompleter());
        } else {
            this.getLogger().info("§cDisable");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        this.getLogger().info("§a§lGems Enabled");
    }

    @Override
    public void onDisable() {
        Gems.instance = null;
    }

    public static Gems getInstance() {
        return Gems.instance;
    }

    public PluginLogger getPluginLogger() {
        return pluginLogger;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            this.economy = rsp.getProvider();
        }
        return this.economy != null;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        if (
            event.getAction().toString().equals("RIGHT_CLICK_AIR")
            || event.getAction().toString().equals("RIGHT_CLICK_BLOCK")
            || event.getAction().toString().equals("LEFT_CLICK_AIR")
            || event.getAction().toString().equals("LEFT_CLICK_BLOCK")
        ) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();
            Integer value = Utils.checkEmerald(item);
            if (value != null) {
                player.getInventory().setItemInMainHand(null);

                int gems = value * item.getAmount();
                this.pluginLogger.log(Utils.formatPlayer(player), "received "+gems+" Gems " + Utils.formatGems(item)); // TODO log + String.format
                this.economy.depositPlayer(player, gems);
                ChatUtils.sendGemsMessage(player, gems);
            }
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack item = event.getItem().getItemStack();
            Integer value = Utils.checkEmerald(item);
            if (value != null) {
                event.getItem().remove();
                event.setCancelled(true);

                int gems = value * item.getAmount();
                this.pluginLogger.log(Utils.formatPlayer(player), "received "+gems+" Gems " + Utils.formatGems(item)); // TODO log + String.format
                this.economy.depositPlayer(player, gems);
                ChatUtils.sendGemsMessage(player, gems);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        // Проверяем, что умер именно игрок
        if (this.configManager.isDeathDrop() && event.getEntity() instanceof Player player) {

            // Создаём предмет, который должен выпасть с игрока
            int balance = (int) this.getEconomy().getBalance(player);
            int value = (balance * this.configManager.getDeathDropPercent()) / 100;
            if (value < 1) {
                return;
            }
            if (this.configManager.getDeathDropMax() != null && value > this.configManager.getDeathDropMax()) {
                value = this.configManager.getDeathDropMax();
            }
            EconomyResponse economyResponse = this.getEconomy().withdrawPlayer(player, value);
            if (economyResponse.type == EconomyResponse.ResponseType.FAILURE) { // TODO добавить такую же проверку на другие хендлеры
                this.getLogger().severe("Failed to withdraw Gems " + player.getName());
                return;
            }
            ItemStack item = Utils.createGemsItem(value, 1, player.getUniqueId().toString(), player.getName());

            // Добавляем предмет в дроп игрока
            event.getDrops().add(item);
        }
    }
}
