package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class CrownListener implements Listener {

    private final PowersPlugin plugin;

    public CrownListener(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    // Remove all powers if crown is dropped
    @EventHandler
    public void onCrownDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();

        if (plugin.getCrownManager().isCrown(item)) {
            if (plugin.getCrownManager().hasCrown(player)) {
                plugin.getPowerManager().clearPlayerData(player.getUniqueId());
                plugin.getCrownManager().clearCrownHolder(player.getUniqueId());
                player.sendMessage("§8[§6Powers§8] §cYou dropped the Crown and lost all powers!");
            }
        }
    }

    // Remove powers on death (crown drops on death)
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCrownHolderDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        if (!plugin.getCrownManager().hasCrown(dead)) return;

        plugin.getPowerManager().clearPlayerData(dead.getUniqueId());
        plugin.getCrownManager().clearCrownHolder(dead.getUniqueId());

        Bukkit.getServer().broadcast(
                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&6&l✦ The Crown of the Ancient Guardian is now free! Claim it! ✦"));
    }

    @EventHandler
    public void onCrownHolderQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (plugin.getCrownManager().hasCrown(player)) {
            plugin.getPowerManager().clearPlayerData(player.getUniqueId());
            plugin.getCrownManager().clearCrownHolder(player.getUniqueId());
        }
    }
}
