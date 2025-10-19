package egorkhabarov.config;

import egorkhabarov.Gems;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final Gems plugin;

    private String itemMaterial;
    private String itemName;
    private String itemLore;
    private int maxValue;
    private String message_withdraw;
    private String message_withdraw_failed;
    private String message_received;
    private String message_received_failed;
    private boolean deathDrop;
    private int deathDropPercent;
    private Integer deathDropMax;

    public ConfigManager(Gems plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.itemMaterial = config.getString("item.material", "minecraft:emerald");
        this.itemName = config.getString("item.name", "%s Gems");
        this.itemLore = config.getString("item.lore", "Valuable Resource, used for trading");
        this.maxValue = config.getInt("item.max_value", 1_000_000);

        this.message_withdraw = config.getString("messages.withdraw", "§aYou withdrew §l%,d Gems");
        this.message_received = config.getString("messages.received", "§aYou received §l%,d Gems");
        this.message_withdraw_failed = config.getString("messages.withdraw_failed", "");
        this.message_received_failed = config.getString("messages.received_failed", "");

        this.deathDrop = config.getBoolean("death_drop.enabled", false);
        this.deathDropPercent = config.getInt("death_drop.percent", 5);
        String deathDropMaxTemp = config.getString("death_drop.max", null);

        if (deathDropMaxTemp == null || deathDropMaxTemp.equals("null")) {
            this.deathDropMax = null;
        } else {
            this.deathDropMax = config.getInt("death_drop.max");
        }
    }

    public String getItemMaterial() {
        return this.itemMaterial;
    }
    public String getItemName() {
        return this.itemName;
    }
    public String getItemLore() {
        return this.itemLore;
    }
    public int getMaxValue() {
        return this.maxValue;
    }

    public String getWithdrawMessage() {
        return this.message_withdraw;
    }
    public String getWithdrawFailedMessage() {
        return this.message_withdraw_failed;
    }
    public String getReceivedMessage() {
        return this.message_received;
    }
    public String getReceivedFailedMessage() {
        return this.message_received_failed;
    }

    public boolean isDeathDrop() {
        return this.deathDrop;
    }
    public int getDeathDropPercent() {
        return this.deathDropPercent;
    }
    public Integer getDeathDropMax() {
        return this.deathDropMax;
    }
}
