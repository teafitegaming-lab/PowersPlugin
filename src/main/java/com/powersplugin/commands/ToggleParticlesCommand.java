package com.powersplugin.commands;

import com.powersplugin.PowersPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ToggleParticlesCommand implements CommandExecutor {

    private final PowersPlugin plugin;

    public ToggleParticlesCommand(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this!");
            return true;
        }

        boolean nowEnabled = plugin.getPowerManager().toggleParticles(player);
        if (nowEnabled) {
            player.sendMessage("§8[§6Powers§8] §aYour power particles are now §lON§r§a.");
        } else {
            player.sendMessage("§8[§6Powers§8] §cYour power particles are now §lOFF§r§c.");
        }
        return true;
    }
}
