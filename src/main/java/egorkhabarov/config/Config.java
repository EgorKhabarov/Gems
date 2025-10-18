package egorkhabarov.config;

import egorkhabarov.Gems;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {
    private final Gems gems;

    public String itemName;
    public List<String> itemNameStyle;
    public String itemLore;
    public List<String> itemLoreStyle;
    public int max_value;
    public boolean death_drop;
    public int death_drop_percent;
    public Integer death_drop_max;

    public Config(Gems gems) {
        this.gems = gems;
        this.load();
    }

    public void load() {
        FileConfiguration config = this.gems.getConfig();

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
