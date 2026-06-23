package com.powersplugin.commands;

import com.powersplugin.PowersPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
            showStatus(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            showInfo(player);
            return true;
        }

        return true;
    }

    private void showStatus(Player player) {
        player.sendMessage("§8§m══════════════════════════════");
        player.sendMessage("§6§l  ⚡ YOUR STATUS ⚡");
        player.sendMessage("§8§m══════════════════════════════");

        ItemStack main = player.getInventory().getItemInMainHand();
        if (plugin.getWeaponManager().isCustomWeapon(main)) {
            player.sendMessage("§c  ✦ Holding: Celestial Judgment");
            if (plugin.getWeaponManager().isOnCooldown(player)) {
                player.sendMessage("§7  Cooldown: §e" + plugin.getWeaponManager().formatRemainingCooldown(player));
            } else {
                player.sendMessage("§a  Ready to strike!");
            }
        } else {
            player.sendMessage("§7  Not holding the Celestial Judgment sword.");
            if (plugin.getWeaponManager().isOnCooldown(player)) {
                player.sendMessage("§7  Sword Cooldown: §e" + plugin.getWeaponManager().formatRemainingCooldown(player));
            }
        }
        player.sendMessage("§8§m══════════════════════════════");
    }

    private void showInfo(Player player) {
        player.sendMessage("§8§m══════════════════════════════");
        player.sendMessage("§6§l   Powers Plugin - Info");
        player.sendMessage("§8§m══════════════════════════════");
        player.sendMessage("§eCelestial Judgment §7- One-shot sword (boss drop only)");
        player.sendMessage("§eGod Armor Set       §7- Helmet, Chest, Legs, Boots");
        player.sendMessage("§ePower Apples (x2)   §7- 30 min combat boost");
        player.sendMessage("§eAncient Guardian     §7- Boss that drops the sword");
        player.sendMessage("§7Use §e/powers list §7to see your status.");
        player.sendMessage("§8§m══════════════════════════════");
    }
}
