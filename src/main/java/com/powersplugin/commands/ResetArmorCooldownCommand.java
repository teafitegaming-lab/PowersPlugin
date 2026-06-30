package com.powersplugin.commands;

import com.powersplugin.PowersPlugin;
import com.powersplugin.listeners.ArmorListener;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ResetArmorCooldownCommand implements CommandExecutor {

    private final ArmorListener armorListener;

    public ResetArmorCooldownCommand(ArmorListener armorListener) {
        this.armorListener = armorListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("powersplugin.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        Player target;
        if (args.length >= 1) {
            target = org.bukkit.Bukkit.getPlayer(args[0]);
            if (target == null) { sender.sendMessage("§cPlayer not found!"); return true; }
        } else if (sender instanceof Player p) {
            target = p;
        } else {
            sender.sendMessage("§cUsage: /resetarmorcooldown <player>");
            return true;
        }

        armorListener.resetSonicBoomCooldown(target);
        sender.sendMessage("§aReset Sonic Boom cooldown for §e" + target.getName());
        target.sendMessage("§8[§6Powers§8] §bYour §fSonic Boom §bcooldown has been reset by an admin!");
        return true;
    }
}
