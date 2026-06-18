package com.powersplugin.commands;

import com.powersplugin.PowersPlugin;
import com.powersplugin.powers.CustomPower;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class PowersCommand implements CommandExecutor {

    private final PowersPlugin plugin;

    public PowersCommand(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this!");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            showPowerList(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            showInfo(player);
            return true;
        }

        return true;
    }

    private void showPowerList(Player player) {
        player.sendMessage("§8§m══════════════════════════════");
        player.sendMessage("§6§l  ⚡ YOUR ACTIVE POWERS ⚡");
        player.sendMessage("§8§m══════════════════════════════");

        java.util.Set<CustomPower> powers = plugin.getPowerManager().getPlayerPowers(player);
        if (powers.isEmpty()) {
            player.sendMessage("§7  You have no active powers.");
            player.sendMessage("§7  Craft and use a custom weapon to obtain powers!");
        } else {
            for (CustomPower p : powers) {
                player.sendMessage("  " + p.getDisplayName() + " §7- " + p.getDescription());
            }
        }

        if (plugin.getPowerManager().hasPowerAppleBoost(player)) {
            player.sendMessage("§d  ✦ Power Apple Boost: ACTIVE");
        }
        if (plugin.getCrownManager().hasCrown(player)) {
            player.sendMessage("§6  ✦ Crown of the Ancient Guardian: ACTIVE");
        }
        player.sendMessage("§8§m══════════════════════════════");
    }

    private void showInfo(Player player) {
        player.sendMessage("§8§m══════════════════════════════");
        player.sendMessage("§6§l   Powers Plugin - Info");
        player.sendMessage("§8§m══════════════════════════════");
        player.sendMessage("§e7 Custom Powers  §7| §e7 Custom Weapons");
        player.sendMessage("§e1 God Armor      §7| §e2 Power Apples");
        player.sendMessage("§e1 Ancient Boss   §7| §e1 All-Power Crown");
        player.sendMessage("§7Use §e/powers list §7to see your powers.");
        player.sendMessage("§8§m══════════════════════════════");
    }
}
