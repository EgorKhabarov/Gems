package egorkhabarov.utils;

import egorkhabarov.Gems;
import egorkhabarov.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static @Nullable Integer checkEmerald(@Nullable ItemStack item) {
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
                new NamespacedKey(Gems.getInstance(), "value"),
                PersistentDataType.INTEGER
            )
            || !meta.getPersistentDataContainer().has(
                new NamespacedKey(Gems.getInstance(), "creator"),
                PersistentDataType.STRING
            )
            || !meta.getPersistentDataContainer().has(
                new NamespacedKey(Gems.getInstance(), "creatorName"),
                PersistentDataType.STRING
            )
        ) {
            return null;
        }
        Integer value = meta.getPersistentDataContainer().get(
            new NamespacedKey(Gems.getInstance(), "value"),
            PersistentDataType.INTEGER
        );
        if (value == null || value < 1 || value > Gems.getInstance().getConfigManager().getMaxValue()) {
            return null;
        }
        return value;
    }

    public static ItemStack createGemsItem(int value, int amount, String creator, String creatorName) {
        ConfigManager config = Gems.getInstance().getConfigManager();
        ItemStack item = new ItemStack(Material.EMERALD, amount);
        ItemMeta meta = item.getItemMeta();

        meta.itemName(Component.text(String.format(Locale.US, config.getItemName(), value)));
        if (!config.getItemLore().isEmpty()) {
            meta.lore(List.of(Component.text(config.getItemLore())));
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(
            new NamespacedKey(Gems.getInstance(), "value"),
            PersistentDataType.INTEGER,
            value
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Gems.getInstance(), "creator"),
            PersistentDataType.STRING,
            creator
        );
        meta.getPersistentDataContainer().set(
            new NamespacedKey(Gems.getInstance(), "creatorName"),
            PersistentDataType.STRING,
            creatorName
        );

        item.setItemMeta(meta);
        return item;
    }

    public static String formatPlayer(Player player) {
        return player.getUniqueId() + ":" + player.getName();
    }

    public static String formatGems(ItemStack item) {
        if (!item.hasItemMeta()) {
            return "";
        }
        ItemMeta meta = item.getItemMeta();
        if (
            !(
                meta.getPersistentDataContainer().has(
                    new NamespacedKey(Gems.getInstance(), "value"),
                    PersistentDataType.INTEGER
                )
                && meta.getPersistentDataContainer().has(
                    new NamespacedKey(Gems.getInstance(), "creator"),
                    PersistentDataType.STRING
                )
                && meta.getPersistentDataContainer().has(
                    new NamespacedKey(Gems.getInstance(), "creatorName"),
                    PersistentDataType.STRING
                )
            )
        ) {
            return "";
        }

        Integer value = meta.getPersistentDataContainer().get(
            new NamespacedKey(Gems.getInstance(), "value"),
            PersistentDataType.INTEGER
        );
        if (value == null) {
            value = 1;
        }
        String creator = meta.getPersistentDataContainer().get(
            new NamespacedKey(Gems.getInstance(), "creator"),
            PersistentDataType.STRING
        );
        String creatorName = meta.getPersistentDataContainer().get(
            new NamespacedKey(Gems.getInstance(), "creatorName"),
            PersistentDataType.STRING
        );

        return value * item.getAmount() + " (" + value + "*" + item.getAmount() + ") Gems from [" + creator + ":" + creatorName + "]";
    }
}
