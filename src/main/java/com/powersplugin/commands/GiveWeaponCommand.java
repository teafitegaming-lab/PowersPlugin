package com.powersplugin.commands;

import com.powersplugin.PowersPlugin;
import com.powersplugin.weapons.CustomWeapon;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class GiveWeaponCommand implements CommandExecutor {

    private final PowersPlugin plugin;

    public GiveWeaponCommand(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("powersplugin.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /givecustomweapon <player>");
            sender.sendMessage("§7Gives the Celestial Judgment sword (admin override - normally boss-drop only)");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        target.getInventory().addItem(plugin.getWeaponManager().createWeapon(CustomWeapon.CELESTIAL_JUDGMENT));
        sender.sendMessage("§aGave Celestial Judgment to " + target.getName());
        target.sendMessage("§aYou received the Celestial Judgment sword!");
        return true;
    }
}
