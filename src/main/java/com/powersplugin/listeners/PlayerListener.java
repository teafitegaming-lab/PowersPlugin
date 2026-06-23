package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final PowersPlugin plugin;

    public PlayerListener(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        // Add player to boss bar if boss is alive
        plugin.getBossManager().addPlayerToBar(player);
    }

    // ─── Power Apple Consumption ───────────────────────────────────────
    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (!plugin.getCraftingManager().isPowerApple(item)) return;

        // Check count limit (max 2 per server)
        int crafted = plugin.getConfig().getInt("power-apple.crafted-count", 0);
        if (crafted <= 0) {
            e.setCancelled(true);
            player.sendMessage("§c[Powers] No Power Apples have been crafted yet!");
            return;
        }

        plugin.getPowerManager().applyPowerAppleBoost(player);
    }
}
