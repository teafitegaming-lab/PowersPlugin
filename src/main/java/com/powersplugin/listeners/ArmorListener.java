package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ArmorListener implements Listener {

    private final PowersPlugin plugin;
    private final Map<UUID, Long> sonicBoomCooldown = new HashMap<>();
    private static final long SONIC_BOOM_COOLDOWN_MS = 24L * 60L * 60L * 1000L;
    private BukkitTask resistanceTask;

    public ArmorListener(PowersPlugin plugin) {
        this.plugin = plugin;
        startResistanceTask();
    }

    // ─── Permanent Resistance while wearing ANY God Armor piece ──────
    private void startResistanceTask() {
        resistanceTask = new BukkitRunnable() {
            @Override public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!isWearingAnyGodArmor(player)) continue;

                    // Resistance V = effectively cannot die from normal hits
                    // Duration 60 ticks (3 sec) refreshed every 2 sec = always active
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.RESISTANCE,
                            60, 4,  // amplifier 4 = Resistance V
                            false, false, true  // no particles, no icon, show icon
                    ));
                }
            }
        }.runTaskTimer(plugin, 0L, 40L); // refresh every 2 seconds
    }

    // ─── 40% damage reduction (stacks with resistance) ───────────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onGodArmorDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!isWearingAnyGodArmor(player)) return;

        e.setDamage(e.getDamage() * 0.60);

        player.getWorld().spawnParticle(Particle.END_ROD,
                player.getLocation().add(0, 1, 0), 5, 0.4, 0.4, 0.4, 0.05);

        if (!player.hasPotionEffect(PotionEffectType.REGENERATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                    40, 1, false, false, true));
        }
    }

    // ─── Sonic Boom — Right Click with ANY God Armor piece ───────────
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (!player.isSneaking()) return; // Must be Shift + Right Click
        if (!isWearingAnyGodArmor(player)) return;

        UUID id = player.getUniqueId();
        if (sonicBoomCooldown.containsKey(id)) {
            long remaining = sonicBoomCooldown.get(id) - System.currentTimeMillis();
            if (remaining > 0) {
                long hours = remaining / 3600000;
                long mins = (remaining % 3600000) / 60000;
                long secs = (remaining % 60000) / 1000;
                player.sendMessage("§b§l[God Armor] §7Sonic Boom cooldown: §e"
                        + hours + "h " + mins + "m " + secs + "s");
                return;
            }
        }

        sonicBoomCooldown.put(id, System.currentTimeMillis() + SONIC_BOOM_COOLDOWN_MS);
        activateSonicBoom(player);
        e.setCancelled(true);
    }

    // ─── Public method for ResetArmorCooldownCommand ─────────────────
    public void resetSonicBoomCooldown(Player player) {
        sonicBoomCooldown.remove(player.getUniqueId());
    }

    private void activateSonicBoom(Player player) {
        Location origin = player.getEyeLocation();
        World world = player.getWorld();

        world.playSound(origin, Sound.ENTITY_WARDEN_SONIC_BOOM, 4f, 1f);
        world.playSound(origin, Sound.ENTITY_WARDEN_SONIC_CHARGE, 4f, 0.8f);
        world.playSound(origin, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1.5f);

        world.spawnParticle(Particle.SONIC_BOOM, origin, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.SCULK_SOUL, origin, 25, 2, 0.5, 2, 0.4);
        world.spawnParticle(Particle.END_ROD, origin, 50, 2.5, 0.5, 2.5, 0.4);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, origin, 5, 1, 0.5, 1, 0);

        player.sendTitle("§b⚡ SONIC BOOM", "§7All nearby armor destroyed!", 5, 50, 15);
        player.sendMessage("§b§l[God Armor] §fSonic Boom fired! §724hr cooldown started.");

        List<Player> targets = new ArrayList<>();
        for (Entity nearby : world.getNearbyEntities(origin, 15, 15, 15)) {
            if (nearby instanceof Player t && !t.equals(player)) targets.add(t);
        }

        for (Player target : targets) {
            org.bukkit.util.Vector kb = target.getLocation().toVector()
                    .subtract(origin.toVector()).normalize().multiply(3.0);
            kb.setY(0.8);
            target.setVelocity(kb);

            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 120, 3));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 2));

            target.sendTitle("§4§l⚠ ARMOR SHATTERED", "§cGod Armor Sonic Boom!", 5, 50, 15);
            target.sendMessage("§c§l[⚡] §4Sonic Boom §c— Your armor has been destroyed!");

            breakArmorWithAnimation(target);
        }

        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > 25) { cancel(); return; }
                double radius = t * 0.75;
                int pts = 36;
                for (int i = 0; i < pts; i++) {
                    double angle = 2 * Math.PI * i / pts;
                    Location ring = origin.clone().add(
                            Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                    world.spawnParticle(Particle.SCULK_SOUL, ring, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.END_ROD, ring, 1, 0, 0.08, 0, 0.01);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void breakArmorWithAnimation(Player target) {
        Location loc = target.getLocation().add(0, 1, 0);
        target.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 5, 0.5, 0.5, 0.5, 0);
        target.getWorld().spawnParticle(Particle.CRIT, loc, 40, 0.5, 1, 0.5, 0.4);
        target.getWorld().spawnParticle(Particle.SCULK_SOUL, loc, 15, 0.5, 0.5, 0.5, 0.1);
        target.getWorld().playSound(loc, Sound.ENTITY_ITEM_BREAK, 2f, 0.5f);

        new BukkitRunnable() { @Override public void run() {
            ItemStack h = target.getInventory().getHelmet();
            if (h != null && !plugin.getCraftingManager().isGodArmor(h)) {
                crackSound(target); target.getInventory().setHelmet(null);
            }
        }}.runTaskLater(plugin, 3L);

        new BukkitRunnable() { @Override public void run() {
            ItemStack c = target.getInventory().getChestplate();
            if (c != null && !plugin.getCraftingManager().isGodArmor(c)) {
                crackSound(target); target.getInventory().setChestplate(null);
            }
        }}.runTaskLater(plugin, 8L);

        new BukkitRunnable() { @Override public void run() {
            ItemStack l = target.getInventory().getLeggings();
            if (l != null && !plugin.getCraftingManager().isGodArmor(l)) {
                crackSound(target); target.getInventory().setLeggings(null);
            }
        }}.runTaskLater(plugin, 13L);

        new BukkitRunnable() { @Override public void run() {
            ItemStack b = target.getInventory().getBoots();
            if (b != null && !plugin.getCraftingManager().isGodArmor(b)) {
                crackSound(target); target.getInventory().setBoots(null);
            }
        }}.runTaskLater(plugin, 18L);
    }

    private void crackSound(Player target) {
        Location loc = target.getLocation().add(0, 1, 0);
        target.getWorld().playSound(loc, Sound.ENTITY_ITEM_BREAK, 1.5f, 0.6f);
        target.getWorld().playSound(loc, Sound.ITEM_SHIELD_BREAK, 1f, 0.8f);
        target.getWorld().spawnParticle(Particle.CRIT, loc, 20, 0.3, 0.3, 0.3, 0.3);
        target.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 2, 0.2, 0.2, 0.2, 0);
    }

    private boolean isWearingAnyGodArmor(Player player) {
        PlayerInventory inv = player.getInventory();
        return plugin.getCraftingManager().isGodArmor(inv.getHelmet())
            || plugin.getCraftingManager().isGodArmor(inv.getChestplate())
            || plugin.getCraftingManager().isGodArmor(inv.getLeggings())
            || plugin.getCraftingManager().isGodArmor(inv.getBoots());
    }

    public void stopTasks() {
        if (resistanceTask != null) resistanceTask.cancel();
    }
}
