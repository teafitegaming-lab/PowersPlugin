package com.powersplugin.commands;

import com.powersplugin.PowersPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SpawnBossCommand implements CommandExecutor {

    private final PowersPlugin plugin;

    public SpawnBossCommand(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("powersplugin.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this!");
            return true;
        }

        plugin.getBossManager().spawnBoss(player.getLocation());
        sender.sendMessage("§aAncient Guardian spawned!");
        return true;
    }
}
