package org.egorkhabarov.gems;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.*;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import org.egorkhabarov.commands.GemsCommand;
import org.egorkhabarov.commands.GemsCommandTabCompleter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;


public final class Gems extends JavaPlugin implements Listener {
    private Economy economy = null;
    private static final Logger logger = Logger.getLogger("GemsLogger");

    String itemName;
    List<String> itemNameStyle;
    String itemLore;
    List<String> itemLoreStyle;
    int max_value;
    boolean death_drop;
    int death_drop_percent;
    Integer death_drop_max;


    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Плагин §6Vault §сне найден на сервере!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getPluginManager().registerEvents(this, this);

        PluginCommand gems = this.getCommand("gems");
        if (gems != null) {
            gems.setExecutor(new GemsCommand(this));
            gems.setTabCompleter(new GemsCommandTabCompleter());
        } else {
            this.getLogger().info("§cDisable");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        this.saveDefaultConfig();
        this.reloadMyConfig();

        // getConfig().set("key", "value");
        // saveConfig();
        
        setupLogger();

        // System.out.println("    ");
        System.out.println("§a§lGems Enabled");
        // System.out.println("    ");

    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
        return economy != null;
    }

    @Override
    public void onDisable() {
        // onDisable
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
            Integer value = checkEmerald(item);
            if (value != null) {
                player.getInventory().setItemInMainHand(null);

                int gems = value * item.getAmount();
                this.log(this.formatPlayer(player), "received "+gems+" Gems " + this.formatGems(item));
                this.economy.depositPlayer(player, gems);
                this.sendGemsMessage(player, gems);
            }
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack item = event.getItem().getItemStack();
            Integer value = checkEmerald(item);
            if (value != null) {
                event.getItem().remove();
                event.setCancelled(true);

                int gems = value * item.getAmount();
                this.log(this.formatPlayer(player), "received "+gems+" Gems " + this.formatGems(item));
                this.economy.depositPlayer(player, gems);
                this.sendGemsMessage(player, gems);
            }
        }
    }

    private @Nullable Integer checkEmerald(@Nullable ItemStack item) {
        if (
            item == null
            || item.getType() != Material.EMERALD
            || !item.hasItemMeta()
        ) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (
            meta == null
            || !meta.getPersistentDataContainer().has(
                new NamespacedKey(this, "value"),
                PersistentDataType.INTEGER
            )
            || !meta.getPersistentDataContainer().has(
                new NamespacedKey(this, "creator"),
                PersistentDataType.STRING
            )
            || !meta.getPersistentDataContainer().has(
                new NamespacedKey(this, "creatorName"),
                PersistentDataType.STRING
            )
        ) {
            return null;
        }
        Integer value = meta.getPersistentDataContainer().get(
            new NamespacedKey(this, "value"),
            PersistentDataType.INTEGER
        );
        if (value == null) {
            return null;
        }
        if (value < 1) {
            return null;
        }
        if (value > 1_000_000) {
            return null;
        }
        return value;
    }

    private void sendGemsMessage(Player player, int value) {
        String message = "§aYou received §l" + String.format(Locale.US, "%,d",value) + " Gems";
        player.sendMessage(message);
        player.sendActionBar(Component.text(message));
    }

    public Economy getEconomy() {
        return economy;
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
            getLogger().severe("Не удалось настроить логирование: " + e.getMessage());
        }
    }

    private @NotNull FileHandler getFileHandler(LocalDate currentDate) throws IOException {
        // File logFile = new File(this.getDataFolder(), "logs/" + currentDate + ".log");
        // String logFileName = logFile.getAbsolutePath();
        //
        // // Создаём директорию для логов, если её нет
        // File logDir = new File(this.getDataFolder(), "logs");
        // if (!logDir.exists()) {
        //     logDir.mkdirs();
        // }
        //
        // // Настройка логера
        // FileHandler fileHandler = new FileHandler(logFileName, true); // Дневной файл логов
        // fileHandler.setFormatter(new SimpleFormatter());
        // fileHandler.setLevel(Level.ALL); // Уровень логирования
        // return fileHandler;
        File logDir = new File(this.getDataFolder(), "logs");
        if (!logDir.exists()) {
            logDir.mkdirs(); // Создаём директорию, если её нет
        }

        String logFileName = logDir + File.separator + currentDate + ".log"; // Путь к текущему лог-файлу
        File logFile = new File(logFileName);

        // Настройка логгера
        FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath(), true); // Дневной файл логов
        fileHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                //return super.format(record);// Форматируем время в UTC
                Date logDate = new Date(record.getMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String timestamp = sdf.format(logDate);

                return String.format("[%s] %s%n", timestamp, record.getMessage());
            }
        });
        fileHandler.setLevel(Level.ALL); // Уровень логирования
        return fileHandler;
    }

    private void archiveOldLogs() {
        // try {
        //     // Получаем текущую дату
        //     LocalDate currentDate = LocalDate.now();
        //     String logFileName = currentDate + ".log";
        //
        //     // Получаем файлы в папке logs
        //     File logDir = new File(this.getDataFolder(), "logs");
        //     File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log") && !name.equals(logFileName));
        //
        //     // Архивируем старые файлы, если они есть
        //     if (logFiles != null) {
        //         for (File logFile : logFiles) {
        //             // Архивируем файл в .gz
        //             String gzFileName = logFile.getAbsolutePath() + ".gz";
        //             try (FileInputStream fis = new FileInputStream(logFile);
        //                  GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(gzFileName))) {
        //
        //                 byte[] buffer = new byte[1024];
        //                 int length;
        //                 while ((length = fis.read(buffer)) > 0) {
        //                     gzos.write(buffer, 0, length);
        //                 }
        //
        //                 // Удаляем оригинальный лог-файл после архивирования
        //                 Files.delete(Paths.get(logFile.getAbsolutePath()));
        //
        //                 this.getLogger().info("Лог файл архивирован: " + gzFileName);
        //             } catch (IOException e) {
        //                 this.getLogger().severe("Ошибка при архивировании лога: " + e.getMessage());
        //             }
        //         }
        //     }
        // } catch (Exception e) {
        //     this.getLogger().severe("Не удалось архивировать старые логи: " + e.getMessage());
        // }
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
            getLogger().severe("Не удалось архивировать старые логи: " + e.getMessage());
        }
    }

    public void log(String playerName, String operationDetails) {

        logger.info("["+playerName+"]" + ": " + operationDetails);
    }

    public String formatPlayer(Player player) {
        return player.getUniqueId()+":"+player.getName();
    }

    public String formatGems(ItemStack item) {
        if (!item.hasItemMeta()) {
            return "";
        }
        ItemMeta meta = item.getItemMeta();
        if (
            !(
                meta.getPersistentDataContainer().has(
                    new NamespacedKey(this, "value"),
                    PersistentDataType.INTEGER
                )
                && meta.getPersistentDataContainer().has(
                    new NamespacedKey(this, "creator"),
                    PersistentDataType.STRING
                )
                && meta.getPersistentDataContainer().has(
                    new NamespacedKey(this, "creatorName"),
                    PersistentDataType.STRING
                )
            )
        ) {
            return "";
        }

        Integer value = meta.getPersistentDataContainer().get(
            new NamespacedKey(this, "value"),
            PersistentDataType.INTEGER
        );
        if (value == null) {
            value = 1;
        }
        String creator = meta.getPersistentDataContainer().get(
            new NamespacedKey(this, "creator"),
            PersistentDataType.STRING
        );
        String creatorName = meta.getPersistentDataContainer().get(
            new NamespacedKey(this, "creatorName"),
            PersistentDataType.STRING
        );
        return value * item.getAmount() + " (" + value + "*" + item.getAmount() + ") Gems from ["+creator+":"+creatorName+"]";
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        // Проверяем, что умер именно игрок
        if (this.death_drop && event.getEntity() instanceof Player player) {

            // Создаём предмет, который должен выпасть с игрока
            int balance = (int) this.getEconomy().getBalance(player);
            int value = (balance * this.death_drop_percent) / 100;
            if (value < 1) {
                return;
            }
            if (this.death_drop_max != null && value > this.death_drop_max) {
                value = 1000;
            }
            EconomyResponse economyResponse = this.getEconomy().withdrawPlayer(player, value);
            if (economyResponse.type == EconomyResponse.ResponseType.FAILURE) {
                this.getLogger().severe("Failed to withdraw Gems " + player.getName());
                return;
            }
            ItemStack item = createGemsItem(value, 1, player.getUniqueId().toString(), player.getName());

            // Добавляем предмет в дроп игрока
            event.getDrops().add(item);
        }
    }

    public ItemStack createGemsItem(int value, int amount, String creator, String creatorName) {
        ItemStack item = new ItemStack(Material.EMERALD, amount);
        ItemMeta meta = item.getItemMeta();
        meta.itemName(
            Component.text(
                String.format(Locale.US, "%,d", value) + " Gems",
                NamedTextColor.GREEN,
                TextDecoration.BOLD
            )
        );
        meta.lore(List.of(Component.text("§fValuable Resource, used for trading")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(
            new NamespacedKey(this, "value"),
            PersistentDataType.INTEGER,
            value
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(this, "creator"),
            PersistentDataType.STRING,
            creator
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(this, "creatorName"),
            PersistentDataType.STRING,
            creatorName
        );
        item.setItemMeta(meta);
        return item;
    }

    public int getMaxValue() {
        return this.max_value;
    }

    public void reloadMyConfig() {
        FileConfiguration config = this.getConfig();
        this.itemName = config.getString("name", "%s Gems");
        this.itemNameStyle = config.getStringList("name_style");
        this.itemLore = config.getString("lore", "Valuable Resource, used for trading");
        this.itemLoreStyle = config.getStringList("lore_style");
        this.max_value = config.getInt("max_value", 1_000_000);
        this.death_drop = config.getBoolean("death_drop", true);
        this.death_drop_percent = config.getInt("death_drop_percent", 5);
        String death_drop_max_temp = config.getString("death_drop_max");
        if (death_drop_max_temp == null || death_drop_max_temp.equals("null")) {
            this.death_drop_max = null;
        } else {
            this.death_drop_max = Integer.parseInt(death_drop_max_temp);
        }
    }
}
