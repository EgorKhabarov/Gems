package egorkhabarov.utils;

import egorkhabarov.Gems;
import egorkhabarov.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Locale;

public class ChatUtils {
    public static void sendWithdrawGemsMessage(Player player, int value) {
        // String message = "§aYou withdrew §l" + String.format(Locale.US, "%,d",value) + " Gems";
        ConfigManager config = Gems.getInstance().getConfigManager();
        String message = String.format(Locale.US, config.getWithdrawMessage(), value);
        player.sendMessage(message);
    }

    public static void sendReceivedGemsMessage(Player player, int value) {
        // String message = "§aYou received §l" + String.format(Locale.US, "%,d",value) + " Gems";
        ConfigManager config = Gems.getInstance().getConfigManager();
        String message = String.format(Locale.US, config.getReceivedMessage(), value);
        player.sendMessage(message);
        player.sendActionBar(Component.text(message));
    }
}
