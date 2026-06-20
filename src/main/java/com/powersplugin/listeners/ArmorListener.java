package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ArmorListener implements Listener {

    private final PowersPlugin plugin;

    public ArmorListener(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGodArmorDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (!isWearingFullGodSet(player)) return;

        // Reduce ALL incoming damage by 40%
        e.setDamage(e.getDamage() * 0.60);

        // Visual feedback
        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 5, 0.4, 0.4, 0.4, 0.05);

        // Passive regeneration
        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, false, false, true));
        }
    }

    private boolean isWearingFullGodSet(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack legs = inv.getLeggings();
        ItemStack boots = inv.getBoots();

        return plugin.getCraftingManager().isGodArmor(helmet)
                && plugin.getCraftingManager().isGodArmor(chest)
                && plugin.getCraftingManager().isGodArmor(legs)
                && plugin.getCraftingManager().isGodArmor(boots);
    }
}
