package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import com.powersplugin.powers.CustomPower;
import com.powersplugin.weapons.CustomWeapon;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WeaponListener implements Listener {

    private final PowersPlugin plugin;

    public WeaponListener(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Weapon Damage ────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;

        ItemStack hand = attacker.getInventory().getItemInMainHand();
        CustomWeapon weapon = plugin.getWeaponManager().getCustomWeapon(hand);
        if (weapon == null) return;

        double base = weapon.getDamage();
        double multiplier = plugin.getPowerManager().getDamageMultiplier(attacker);

        // CROWN holders get extra 2x
        if (plugin.getCrownManager().hasCrown(attacker)) multiplier *= 2.0;

        // ─── CELESTIAL JUDGMENT: 1-shot, bypass totem ─────────────────
        if (weapon.isOneShotWeapon()) {
            e.setCancelled(true);
            handleOneShotKill(attacker, victim);
            return;
        }

        e.setDamage(base * multiplier);

        // Apply power-based hit effects
        applyHitEffect(attacker, victim, weapon);
    }

    private void handleOneShotKill(Player attacker, LivingEntity victim) {
        // Remove totem from inventory if player
        if (victim instanceof Player target) {
            target.getInventory().forEach(item -> {
                if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                    target.getInventory().remove(item);
                }
            });
            target.setHealth(0);
        } else {
            victim.setHealth(0);
        }

        Location loc = victim.getLocation().add(0, 1, 0);
        loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 80, 1.5, 1.5, 1.5, 0.5);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 60, 1, 1, 1, 0.3);
        for (int i = 0; i < 5; i++) {
            loc.getWorld().strikeLightningEffect(loc.clone().add(
                    (Math.random() - 0.5) * 3, 0, (Math.random() - 0.5) * 3));
        }
        attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.5f);

        if (victim instanceof Player t) {
            t.sendMessage("§4§l☠ CELESTIAL JUDGMENT struck you down. No totem can save you. ☠");
        }
        attacker.sendMessage("§f§l✦ Celestial Judgment delivered! ✦");
    }

    private void applyHitEffect(Player attacker, LivingEntity victim, CustomWeapon weapon) {
        CustomPower power = weapon.getAssociatedPower();
        Location loc = victim.getLocation().add(0, 1, 0);

        switch (power) {
            case FIRE_LORD -> {
                victim.setFireTicks(100);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 15, 0.3, 0.5, 0.3, 0.1);
            }
            case STORM_BRINGER -> {
                loc.getWorld().strikeLightningEffect(loc);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 20, 0.5, 0.5, 0.5, 0.2);
            }
            case VOID_WALKER -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                loc.getWorld().spawnParticle(Particle.PORTAL, loc, 30, 0.5, 0.5, 0.5, 0.5);
            }
            case FROST_TITAN -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 4));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 2));
                loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 25, 0.5, 0.5, 0.5, 0.05);
            }
            case NATURE_SAGE -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 2));
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
                loc.getWorld().spawnParticle(Particle.COMPOSTER, loc, 20, 0.4, 0.4, 0.4, 0.1);
            }
            case SHADOW_REAPER -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                loc.getWorld().spawnParticle(Particle.ASH, loc, 30, 0.5, 0.5, 0.5, 0.2);
            }
            case CELESTIAL_KNIGHT -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 3));
                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 20, 0.5, 0.5, 0.5, 0.2);
            }
        }
    }

    // ─── Kill Animation ────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (!(dead.getKiller() instanceof Player killer)) return;

        ItemStack hand = killer.getInventory().getItemInMainHand();
        CustomWeapon weapon = plugin.getWeaponManager().getCustomWeapon(hand);
        if (weapon == null) return;

        plugin.getParticleManager().playKillAnimation(killer, dead, weapon);

        // Grant power to killer when killing with weapon
        plugin.getPowerManager().grantPowerFromWeapon(killer, weapon);

        // Title on kill
        killer.sendTitle(
                weapon.getAssociatedPower().getDisplayName(),
                "§7Kill confirmed with §r" + weapon.getDisplayName(),
                5, 30, 10
        );
    }

    // ─── Power passive effects ─────────────────────────────────────────
    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        // FROST TITAN: slow attackers
        if (plugin.getPowerManager().hasPower(victim, CustomPower.FROST_TITAN)) {
            if (e.getDamager() instanceof LivingEntity attacker) {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                victim.getWorld().spawnParticle(Particle.SNOWFLAKE,
                        victim.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
            }
        }

        // NATURE SAGE: damage reflects as poison
        if (plugin.getPowerManager().hasPower(victim, CustomPower.NATURE_SAGE)) {
            if (e.getDamager() instanceof LivingEntity att) {
                att.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1));
            }
        }

        // SHADOW REAPER: random dodge 20%
        if (plugin.getPowerManager().hasPower(victim, CustomPower.SHADOW_REAPER)) {
            if (Math.random() < 0.20) {
                e.setCancelled(true);
                victim.sendMessage("§0§l[Shadow Reaper] §7Dodged!");
                victim.getWorld().spawnParticle(Particle.ASH, victim.getLocation().add(0,1,0), 15, 0.4, 0.4, 0.4, 0.1);
            }
        }

        // CELESTIAL KNIGHT: reduces damage by 30%
        if (plugin.getPowerManager().hasPower(victim, CustomPower.CELESTIAL_KNIGHT)) {
            e.setDamage(e.getDamage() * 0.70);
        }
    }

    // ─── Celestial Knight: Totem on death ─────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();

        if (plugin.getPowerManager().hasPower(dead, CustomPower.CELESTIAL_KNIGHT)) {
            if (dead.getHealth() <= 0) {
                e.setCancelled(true);
                dead.setHealth(4.0);
                dead.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 4));
                dead.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
                dead.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                        dead.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.5);
                dead.getWorld().playSound(dead.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
                dead.sendMessage("§f§l✦ Celestial Knight saved you from death! ✦");
            }
        }

        // VOID WALKER: teleport away instead of dying
        if (plugin.getPowerManager().hasPower(dead, CustomPower.VOID_WALKER)) {
            if (dead.getHealth() <= 0 && !plugin.getPowerManager().hasPower(dead, CustomPower.CELESTIAL_KNIGHT)) {
                e.setCancelled(true);
                dead.setHealth(2.0);
                Location safe = dead.getLocation().add(
                        (Math.random() - 0.5) * 20, 0, (Math.random() - 0.5) * 20);
                dead.teleport(safe);
                dead.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
                dead.getWorld().spawnParticle(Particle.PORTAL, dead.getLocation().add(0,1,0), 40, 1,1,1,0.5);
                dead.sendMessage("§5§l[Void Walker] §7Phase escape! You blinked away!");
            }
        }
    }
}
