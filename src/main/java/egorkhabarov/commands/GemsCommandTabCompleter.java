package egorkhabarov.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GemsCommandTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String s,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            ArrayList<String> list = new ArrayList<>();
            if (sender.hasPermission("gems.*") || sender.hasPermission("gems.withdrawal")) {
                list.add("withdrawal");
            }
            if (sender.hasPermission("gems.*") || sender.hasPermission("gems.give")) {
                list.add("give");
            }
            if (sender.hasPermission("gems.*") || sender.hasPermission("gems.reload")) {
                list.add("reload");
            }
            return list;
        }
        if (args[0].equals("withdrawal") && (sender.hasPermission("gems.*") || sender.hasPermission("gems.withdrawal"))) {
            if (args.length == 2) {
                ArrayList<String> list = new ArrayList<>();
                for (int i = 1000; i < 100001; i+=1000) {
                    list.add(String.valueOf(i));
                }
                list.add("all");
                return list;
            }
            if (args.length == 3 && !args[1].equals("all")) {
                ArrayList<String> list = new ArrayList<>();
                for (int i = 1; i < 65; i++) {
                    list.add(String.valueOf(i));
                }
                return list;
            }
        }
        if (args[0].equals("give") && (sender.hasPermission("gems.*") || sender.hasPermission("gems.give"))) {
            if (args.length == 2) {
                List<String> list = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    list.add(player.getName());
                }
                return list;
            }
            if (args.length == 3) {
                ArrayList<String> list = new ArrayList<>();
                for (int i = 1000; i < 100001; i+=1000) {
                    list.add(String.valueOf(i));
                }
                return list;
            }
            if (args.length == 4) {
                ArrayList<String> list = new ArrayList<>();
                for (int i = 1; i < 65; i++) {
                    list.add(String.valueOf(i));
                }
                return list;
            }
        }
        return null;
    }
}
