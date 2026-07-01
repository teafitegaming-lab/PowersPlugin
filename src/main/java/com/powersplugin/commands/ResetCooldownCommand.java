package com.powersplugin.commands;

import com.powersplugin.PowersPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ResetCooldownCommand implements CommandExecutor {

    private final PowersPlugin plugin;

    public ResetCooldownCommand(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("powersplugin.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        Player target;
        if (args.length >= 1) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) { sender.sendMessage("§cPlayer not found!"); return true; }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("§cUsage: /resetcooldown <player>");
            return true;
        }

        plugin.getWeaponManager().clearCooldown(target);
        sender.sendMessage("§aCleared the Celestial Judgment cooldown for " + target.getName());
        target.sendMessage("§8[§6Powers§8] §aYour Celestial Judgment cooldown has been reset by an admin!");
        return true;
    }
}
