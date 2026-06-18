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

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /givecustomweapon <player> <weapon>");
            sender.sendMessage("§7Weapons: inferno_blade, storm_lance, void_scythe, glacial_hammer, natures_wrath, shadow_fang, celestial_judgment");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        String weaponId = args[1].toLowerCase();
        CustomWeapon found = null;
        for (CustomWeapon w : CustomWeapon.values()) {
            if (w.getId().equals(weaponId)) { found = w; break; }
        }

        if (found == null) {
            sender.sendMessage("§cUnknown weapon: " + weaponId);
            sender.sendMessage("§7Available: inferno_blade, storm_lance, void_scythe, glacial_hammer, natures_wrath, shadow_fang, celestial_judgment");
            return true;
        }

        target.getInventory().addItem(plugin.getWeaponManager().createWeapon(found));
        plugin.getPowerManager().grantPowerFromWeapon(target, found);
        sender.sendMessage("§aGave " + found.getDisplayName() + " §ato " + target.getName());
        target.sendMessage("§aYou received " + found.getDisplayName() + "§a!");
        return true;
    }
}
