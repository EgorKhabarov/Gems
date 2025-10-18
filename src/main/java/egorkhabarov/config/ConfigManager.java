package egorkhabarov.config;

import egorkhabarov.Gems;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final Gems plugin;

    private String itemName;
    private String itemLore;
    private int maxValue;
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

        this.itemName = config.getString("item.name", "%s Gems");
        this.itemLore = config.getString("item.lore", "Valuable Resource, used for trading");
        this.maxValue = config.getInt("item.max_value", 1_000_000);

        this.deathDrop = config.getBoolean("death_drop.enabled", false);
        this.deathDropPercent = config.getInt("death_drop.percent", 5);
        String deathDropMaxTemp = config.getString("death_drop.max", null);

        if (deathDropMaxTemp == null || deathDropMaxTemp.equals("null")) {
            this.deathDropMax = null;
        } else {
            this.deathDropMax = config.getInt("death_drop.max");
        }
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
