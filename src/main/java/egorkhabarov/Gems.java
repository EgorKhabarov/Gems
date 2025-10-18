package egorkhabarov;

import egorkhabarov.config.ConfigManager;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;


public final class Gems extends JavaPlugin implements Listener {
    private static Gems instance;
    private ConfigManager configManager;
    private Economy economy;
    public static final Logger logger = Logger.getLogger("GemsLogger");

    @Override
    public void onEnable() {
        Gems.instance = this;
        if (!this.setupEconomy()) {
            this.getLogger().severe("Plugin §6Vault §сnot found on server!");
            this.getServer().getPluginManager().disablePlugin(this); // TODO ? disablePlugin
            return;
        }

        this.configManager = new ConfigManager(this);

        this.getServer().getPluginManager().registerEvents(this, this);

        PluginCommand gems = this.getCommand("gems");
        if (gems != null) {
            gems.setExecutor(new GemsCommand(this));
            gems.setTabCompleter(new GemsCommandTabCompleter());
        } else {
            this.getLogger().info("§cDisable");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        this.setupLogger();
        this.getLogger().info("§a§lGems Enabled");
    }

    @Override
    public void onDisable() {
        Gems.instance = null;
    }

    public static Gems getInstance() {
        return Gems.instance;
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
                this.log(Utils.formatPlayer(player), "received "+gems+" Gems " + Utils.formatGems(item)); // TODO log + String.format
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
                this.log(Utils.formatPlayer(player), "received "+gems+" Gems " + Utils.formatGems(item)); // TODO log + String.format
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

    private void setupLogger() {
        try {
            // Получаем текущую дату
            LocalDate currentDate = LocalDate.now();
            FileHandler fileHandler = this.getFileHandler(currentDate);
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            // Архивируем старые логи, если они есть
            this.archiveOldLogs();
        } catch (IOException e) {
            this.getLogger().severe("Не удалось настроить логирование: " + e.getMessage());
        }
    }

    private @NotNull FileHandler getFileHandler(LocalDate currentDate) throws IOException {
        File logDir = new File(this.getDataFolder(), "logs");

        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        String logFileName = logDir + File.separator + currentDate + ".log"; // Путь к текущему лог-файлу
        File logFile = new File(logFileName);

        // Настройка логгера
        FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath(), true); // Дневной файл логов
        fileHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                // return super.format(record); // Форматируем время в UTC
                Date logDate = new Date(record.getMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String timestamp = sdf.format(logDate);

                return String.format("[%s] %s%n", timestamp, record.getMessage());
            }
        });
        fileHandler.setLevel(Level.ALL);
        return fileHandler;
    }

    private void archiveOldLogs() {
        try {
            // Получаем текущую дату для текущего лог-файла
            LocalDate currentDate = LocalDate.now();
            String currentLogFileName = currentDate + ".log"; // Имя текущего лог-файла

            // Получаем файлы в папке logs
            File logDir = new File(this.getDataFolder(), "logs");
            File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log") && !name.equals(currentLogFileName));

            // Архивируем старые файлы, если они есть
            if (logFiles != null) {
                for (File logFile : logFiles) {
                    // Архивируем файл в .gz
                    // logFile.renameTo(logFile+".gz");
                    String gzFileName = logFile.getAbsolutePath() + ".gz";
                    try (FileInputStream fis = new FileInputStream(logFile);
                         GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(gzFileName))) {

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            gzos.write(buffer, 0, length);
                        }
                    } catch (IOException e) {
                        this.getLogger().severe("Ошибка при архивировании лога: " + e.getMessage());
                    }
                    // try {
                    //     // Удаляем оригинальный лог-файл после архивирования
                    //     Files.delete(Paths.get(logFile.getAbsolutePath()));
                    //     getLogger().info("Лог файл архивирован: " + gzFileName);
                    //
                    // } catch (IOException e) {
                    //     getLogger().severe("Ошибка при удалении старого лога");
                    // }
                }
            }

        } catch (Exception e) {
            this.getLogger().severe("Не удалось архивировать старые логи: " + e.getMessage());
        }
    }

    public void log(String playerName, String operationDetails) {
        logger.info("["+playerName+"]" + ": " + operationDetails);
    }
}
