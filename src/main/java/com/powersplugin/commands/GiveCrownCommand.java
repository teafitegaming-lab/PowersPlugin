package com.powersplugin.commands;

import com.powersplugin.PowersPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class GiveCrownCommand implements CommandExecutor {

    private final PowersPlugin plugin;

    public GiveCrownCommand(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("powersplugin.admin")) {
            sender.sendMessage("§cNo permission!"); return true;
        }

        Player target;
        if (args.length >= 1) {
            target = plugin.getServer().getPlayer(args[0]);
            if (target == null) { sender.sendMessage("§cPlayer not found!"); return true; }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("§cSpecify a player!"); return true;
        }

        target.getInventory().addItem(plugin.getCrownManager().createCrown());
        plugin.getCrownManager().setCrownHolder(target);
        sender.sendMessage("§aGave Crown of the Ancient Guardian to " + target.getName());
        target.sendMessage("§6§l✦ You received the Crown of the Ancient Guardian! ✦");
        return true;
    }
}
