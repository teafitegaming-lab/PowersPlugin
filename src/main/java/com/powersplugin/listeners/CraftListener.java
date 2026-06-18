package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import com.powersplugin.weapons.CustomWeapon;
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

        // ─── Celestial Judgment: only 1 per server ────────────────────
        CustomWeapon weapon = plugin.getWeaponManager().getCustomWeapon(result);
        if (weapon != null && weapon.isOneShotWeapon()) {
            boolean crafted = plugin.getConfig().getBoolean("oneshot-weapon.crafted", false);
            if (crafted) {
                e.setCancelled(true);
                player.sendMessage("§c§l[Powers] §cCelestial Judgment already exists on this server! Only 1 can exist.");
                return;
            }
            plugin.getConfig().set("oneshot-weapon.crafted", true);
            plugin.saveConfig();
            plugin.getServer().broadcast(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize("&f&l✦ " + player.getName() + " has forged the CELESTIAL JUDGMENT! The ultimate weapon exists! ✦"));
        }

        // ─── God Armor: only 1 per server ─────────────────────────────
        if (plugin.getCraftingManager().isGodArmor(result)) {
            boolean crafted = plugin.getConfig().getBoolean("god-armor.crafted", false);
            if (crafted) {
                e.setCancelled(true);
                player.sendMessage("§c§l[Powers] §cGod Armor has already been crafted on this server!");
                return;
            }
            plugin.getConfig().set("god-armor.crafted", true);
            plugin.saveConfig();
            plugin.getServer().broadcast(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize("&b&l✦ " + player.getName() + " has forged the GOD CHESTPLATE! ✦"));
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

        // ─── Regular weapons: 1 per server each ───────────────────────
        if (weapon != null && !weapon.isOneShotWeapon()) {
            String cfgKey = "weapon-crafted." + weapon.getId();
            if (plugin.getConfig().getBoolean(cfgKey, false)) {
                e.setCancelled(true);
                player.sendMessage("§c§l[Powers] §c" + weapon.getDisplayName()
                        .replaceAll("§.", "").replaceAll("&.", "") + " has already been crafted on this server!");
                return;
            }
            plugin.getConfig().set(cfgKey, true);
            plugin.saveConfig();
            plugin.getServer().broadcast(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize("&6&l✦ " + player.getName() + " has forged "
                            + weapon.getDisplayName() + "&6&l! ✦"));
        }
    }
}
