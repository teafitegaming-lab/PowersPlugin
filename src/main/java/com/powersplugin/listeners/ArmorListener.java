package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArmorListener implements Listener {

    private final PowersPlugin plugin;
    // 24hr cooldown tracking: UUID -> expiry timestamp (ms)
    private final Map<UUID, Long> sonicBoomCooldown = new HashMap<>();
    private static final long SONIC_BOOM_COOLDOWN_MS = 24L * 60L * 60L * 1000L;

    public ArmorListener(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── God Armor damage reduction ───────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onGodArmorDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!plugin.getPowerManager().isWearingFullGodArmor(player)) return;

        e.setDamage(e.getDamage() * 0.60);

        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 5, 0.4, 0.4, 0.4, 0.05);

        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1, false, false, true));
        }
    }

    // ─── Sonic Boom Ability — Right Click with God Armor ──────────────
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();

        // Right click only
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        // Must be wearing full god armor
        if (!plugin.getPowerManager().isWearingFullGodArmor(player)) return;

        // Cooldown check
        UUID id = player.getUniqueId();
        if (sonicBoomCooldown.containsKey(id)) {
            long remaining = sonicBoomCooldown.get(id) - System.currentTimeMillis();
            if (remaining > 0) {
                long hours = remaining / 3600000;
                long mins = (remaining % 3600000) / 60000;
                player.sendMessage("§c§l[God Armor] §7Sonic Boom cooldown: §e" + hours + "h " + mins + "m");
                return;
            }
        }

        // Apply cooldown
        sonicBoomCooldown.put(id, System.currentTimeMillis() + SONIC_BOOM_COOLDOWN_MS);

        // Fire the ability
        activateSonicBoom(player);
        e.setCancelled(true);
    }

    private void activateSonicBoom(Player player) {
        Location origin = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();

        // ── Sound + Visual burst ─────────────────────────────────────
        world.playSound(origin, Sound.ENTITY_WARDEN_SONIC_BOOM, 3f, 1f);
        world.playSound(origin, Sound.ENTITY_WARDEN_SONIC_CHARGE, 3f, 0.8f);

        // Initial burst particles
        world.spawnParticle(Particle.SONIC_BOOM, origin, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.SCULK_SOUL, origin, 20, 1.5, 0.5, 1.5, 0.3);
        world.spawnParticle(Particle.END_ROD, origin, 40, 2, 0.5, 2, 0.3);

        // ── Find all players within 15 blocks ────────────────────────
        List<Player> targets = new ArrayList<>();
        for (Entity nearby : world.getNearbyEntities(origin, 15, 15, 15)) {
            if (!(nearby instanceof Player target)) continue;
            if (target.equals(player)) continue;
            targets.add(target);
        }

        // ── Crazy animation + armor break for each target ────────────
        for (Player target : targets) {
            // Push them back (knockback effect)
            org.bukkit.util.Vector knockback = target.getLocation().toVector()
                    .subtract(origin.toVector()).normalize().multiply(2.5);
            knockback.setY(0.5);
            target.setVelocity(knockback);

            // Debuffs
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 3));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 2));

            // Armor break animation — remove each armor piece with delay
            breakArmorWithAnimation(target);

            target.sendMessage("§8§l[⚡] §4§lGod Armor Sonic Boom §8— §cYour armor has been destroyed!");
        }

        // ── Expanding shockwave ring animation ────────────────────────
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t > 20) { cancel(); return; }
                double radius = t * 0.85;
                int pts = 32;
                for (int i = 0; i < pts; i++) {
                    double angle = 2 * Math.PI * i / pts;
                    Location ring = origin.clone().add(
                            Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                    world.spawnParticle(Particle.SCULK_SOUL, ring, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.END_ROD, ring, 1, 0, 0.1, 0, 0.02);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        player.sendMessage("§b§l[⚡] §fSonic Boom activated! §7All nearby players' armor destroyed!");
        player.sendTitle("§b⚡ SONIC BOOM", "§7Cooldown: 24 hours", 5, 40, 10);
    }

    private void breakArmorWithAnimation(Player target) {
        // Shatter particles on the target
        Location loc = target.getLocation().add(0, 1, 0);
        target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 3, 0.5, 0.5, 0.5, 0);
        target.getWorld().spawnParticle(Particle.CRIT, loc, 30, 0.5, 1, 0.5, 0.3);
        target.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, 10, 0.4, 0.4, 0.4, 0.1);
        target.getWorld().playSound(loc, Sound.ENTITY_ITEM_BREAK, 2f, 0.5f);
        target.getWorld().playSound(loc, Sound.ITEM_SHIELD_BREAK, 2f, 0.8f);

        // Break armor with staggered delays for dramatic effect
        new BukkitRunnable() {
            @Override public void run() {
                ItemStack helmet = target.getInventory().getHelmet();
                if (helmet != null && plugin.getCraftingManager().isGodArmor(helmet)) return; // protect god armor
                shattersound(target);
                target.getInventory().setHelmet(null);
            }
        }.runTaskLater(plugin, 2L);

        new BukkitRunnable() {
            @Override public void run() {
                ItemStack chest = target.getInventory().getChestplate();
                if (chest != null && plugin.getCraftingManager().isGodArmor(chest)) return;
                shattersound(target);
                target.getInventory().setChestplate(null);
            }
        }.runTaskLater(plugin, 6L);

        new BukkitRunnable() {
            @Override public void run() {
                ItemStack legs = target.getInventory().getLeggings();
                if (legs != null && plugin.getCraftingManager().isGodArmor(legs)) return;
                shattersound(target);
                target.getInventory().setLeggings(null);
            }
        }.runTaskLater(plugin, 10L);

        new BukkitRunnable() {
            @Override public void run() {
                ItemStack boots = target.getInventory().getBoots();
                if (boots != null && plugin.getCraftingManager().isGodArmor(boots)) return;
                shattersound(target);
                target.getInventory().setBoots(null);
            }
        }.runTaskLater(plugin, 14L);
    }

    private void shattersound(Player target) {
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.5f, 0.7f);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0,1,0), 15, 0.4, 0.4, 0.4, 0.2);
    }
}
