package egorkhabarov.config;

import egorkhabarov.Gems;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Config {

    public final String itemName;
    public final List<String> itemNameStyle;
    public final String itemLore;
    public final List<String> itemLoreStyle;
    public final int max_value;
    public final boolean death_drop;
    public final int death_drop_percent;
    public final Integer death_drop_max;

    public Config(
        String itemName,
        List<String> itemNameStyle,
        String itemLore,
        List<String> itemLoreStyle,
        int max_value,
        boolean death_drop,
        int death_drop_percent,
        Integer death_drop_max
    ) {
        this.itemName = itemName;
        this.itemNameStyle = itemNameStyle;
        this.itemLore = itemLore;
        this.itemLoreStyle = itemLoreStyle;
        this.max_value = max_value;
        this.death_drop = death_drop;
        this.death_drop_percent = death_drop_percent;
        this.death_drop_max = death_drop_max;
    }

    public static Config load(Gems gems) {
        FileConfiguration config = gems.getConfig();

        String itemName = config.getString("name", "%s Gems");
        List<String> itemNameStyle = config.getStringList("name_style");
        String itemLore = config.getString("lore", "Valuable Resource, used for trading");
        List<String> itemLoreStyle = config.getStringList("lore_style");
        int max_value = config.getInt("max_value", 1_000_000);
        boolean death_drop = config.getBoolean("death_drop", true);
        int death_drop_percent = config.getInt("death_drop_percent", 5);
        String death_drop_max_temp = config.getString("death_drop_max");
        Integer death_drop_max;
        if (death_drop_max_temp == null || death_drop_max_temp.equals("null")) {
            death_drop_max = null;
        } else {
            death_drop_max = Integer.parseInt(death_drop_max_temp);
        }

        return new Config(
            itemName,
            itemNameStyle,
            itemLore,
            itemLoreStyle,
            max_value,
            death_drop,
            death_drop_percent,
            death_drop_max
        );
    }
}
