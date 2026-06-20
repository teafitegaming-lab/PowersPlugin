package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

        applyPowerAppleBoost(player);
    }

    private void applyPowerAppleBoost(Player player) {
        player.sendMessage("§8[§6Powers§8] §d✦ §fPower Apple activated! Combat boosted for §e30 minutes§f!");

        player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.3);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 36000, 2, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 36000, 2, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 36000, 2, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 36000, 1, false, true, true));
    }
}
