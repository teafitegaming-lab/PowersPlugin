package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftListener implements Listener {

    private final PowersPlugin plugin;

    public CraftListener(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ItemStack result = e.getRecipe().getResult();

        // ─── God Armor pieces: each piece craftable once per server ──────
        if (plugin.getCraftingManager().isGodArmor(result)) {
            String pieceKey = "god-armor-crafted." + result.getType().name();
            if (plugin.getConfig().getBoolean(pieceKey, false)) {
                e.setCancelled(true);
                player.sendMessage("§c§l[Powers] §cThis God Armor piece has already been crafted on this server!");
                return;
            }
            plugin.getConfig().set(pieceKey, true);
            plugin.saveConfig();
            plugin.getServer().broadcast(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize("&b&l✦ " + player.getName() + " has forged a piece of the GOD ARMOR! ✦"));
        }

        // ─── Power Apple: max 2 per server ────────────────────────────
        if (plugin.getCraftingManager().isPowerApple(result)) {
            int count = plugin.getConfig().getInt("power-apple.crafted-count", 0);
            if (count >= 2) {
                e.setCancelled(true);
                player.sendMessage("§c§l[Powers] §cBoth Power Apples have already been crafted on this server!");
                return;
            }
            plugin.getConfig().set("power-apple.crafted-count", count + 1);
            plugin.saveConfig();
            plugin.getServer().broadcast(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize("&d&l✦ " + player.getName() + " has crafted a Power Apple! (" + (count + 1) + "/2) ✦"));
        }

        // ─── Boss summon items: no limit, just craftable freely ───────
        // (Recipe registration already handles their creation; no extra tracking needed.)
    }
}
