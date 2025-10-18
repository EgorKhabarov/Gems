package egorkhabarov.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Locale;

public class ChatUtils {
    public static void sendGemsMessage(Player player, int value) {
        String message = "§aYou received §l" + String.format(Locale.US, "%,d",value) + " Gems";
        player.sendMessage(message);
        player.sendActionBar(Component.text(message));
    }
}
