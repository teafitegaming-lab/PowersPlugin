package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class ParticleManager {

    private final PowersPlugin plugin;
    private final List<BukkitTask> tasks = new ArrayList<>();

    public ParticleManager(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    public void startParticleTasks() {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!plugin.getPowerManager().particlesEnabled(player)) continue;
                    int tick = plugin.getPowerManager().nextTick(player);

                    if (plugin.getPowerManager().isHoldingSword(player)) {
                        displayFireCone(player, tick);
                    }
                    if (plugin.getPowerManager().isWearingFullGodArmor(player)) {
                        displaySoulCone(player, tick);
                    }
                    if (plugin.getPowerManager().hasPowerAppleBoost(player)) {
                        displayApplePowerAura(player, tick);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        tasks.add(task);
    }

    // ═══════════════════════════════════════════════
    // SWORD — Fire Cone (single ring, rotates clockwise, rises slow)
    // Wide at feet (2.5), narrows at top (3.2 high), resets instantly
    // ═══════════════════════════════════════════════
    private void displayFireCone(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        int cycle = tick % 120;
        double progress = cycle / 120.0;
        double currentHeight = progress * 3.2;
        double coneRadius = 2.5 * (1.0 - progress * 0.96);
        // Clockwise rotation: positive degrees
        double baseAngle = Math.toRadians(tick * 4);
        int points = Math.max(6, (int)(28 * (1.0 - progress * 0.5)));
        for (int i = 0; i < points; i++) {
            double angle = baseAngle + (2 * Math.PI / points) * i;
            double x = Math.cos(angle) * coneRadius;
            double z = Math.sin(angle) * coneRadius;
            Location pos = base.clone().add(x, currentHeight, z);
            player.getWorld().spawnParticle(Particle.FLAME, pos, 1, 0.02, 0.03, 0.02, 0.003);
        }
    }

    // ═══════════════════════════════════════════════
    // GOD ARMOR — Soul Cone (counter-clockwise, faster spin, wider, tighter top)
    // Uses SOUL particle. Rotates OPPOSITE direction to sword.
    // Also rises from feet upward but with a figure-8 / dual helix path.
    // ═══════════════════════════════════════════════
    private void displaySoulCone(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        // Faster cycle: 80 ticks = 4 seconds, counter-clockwise
        int cycle = tick % 80;
        double progress = cycle / 80.0;
        double currentHeight = progress * 3.0;
        // Wider base (3.0), tighter top
        double coneRadius = 3.0 * (1.0 - progress * 0.97);
        // Counter-clockwise: negative degrees
        double baseAngle = Math.toRadians(-(tick * 6));
        int points = Math.max(5, (int)(24 * (1.0 - progress * 0.5)));

        // Main ring
        for (int i = 0; i < points; i++) {
            double angle = baseAngle + (2 * Math.PI / points) * i;
            double x = Math.cos(angle) * coneRadius;
            double z = Math.sin(angle) * coneRadius;
            // Slight wave in the ring for a flowing look (different from fire)
            double yWave = Math.sin(angle * 3 + tick * 0.1) * 0.12;
            Location pos = base.clone().add(x, currentHeight + yWave, z);
            player.getWorld().spawnParticle(Particle.SOUL, pos, 1, 0.03, 0.03, 0.03, 0);
        }

        // Second smaller inner ring offset by half a rotation (dual helix feel)
        double innerRadius = coneRadius * 0.55;
        double innerAngle = baseAngle + Math.PI; // 180° offset
        int innerPoints = Math.max(4, points / 2);
        for (int i = 0; i < innerPoints; i++) {
            double angle = innerAngle + (2 * Math.PI / innerPoints) * i;
            double x = Math.cos(angle) * innerRadius;
            double z = Math.sin(angle) * innerRadius;
            double yWave = Math.sin(angle * 2 + tick * 0.15) * 0.1;
            Location pos = base.clone().add(x, currentHeight + yWave + 0.15, z);
            player.getWorld().spawnParticle(Particle.SOUL, pos, 1, 0.02, 0.02, 0.02, 0);
        }
    }

    // ═══════════════════════════════════════════════
    // POWER APPLE — Golden Spiral
    // ═══════════════════════════════════════════════
    private void displayApplePowerAura(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        double angle = Math.toRadians(tick * 12);
        for (int s = 0; s < 3; s++) {
            double strandAngle = angle + (2 * Math.PI * s / 3);
            double x = Math.cos(strandAngle) * 0.5;
            double z = Math.sin(strandAngle) * 0.5;
            double height = (tick % 30) / 30.0 * 2.0;
            Location pos = base.clone().add(x, height, z);
            player.getWorld().spawnParticle(Particle.END_ROD, pos, 1, 0.02, 0.02, 0.02, 0.01);
        }
        if (tick % 10 == 0) {
            player.getWorld().spawnParticle(Particle.WAX_ON, base.clone().add(0, 1.1, 0), 3, 0.2, 0.2, 0.2, 0.02);
        }
    }

    // ═══════════════════════════════════════════════
    // KILL ANIMATION
    // ═══════════════════════════════════════════════
    public void playKillAnimation(Player killer, org.bukkit.entity.Entity victim) {
        Location loc = victim.getLocation().add(0, 1, 0);
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t > 40) { cancel(); return; }
                for (int y = 0; y < 10; y++) {
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, y, 0), 5, 0.3, 0.1, 0.3, 0.05);
                }
                loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 1, 1, 1, 0.5);
                if (t == 10) loc.getWorld().strikeLightningEffect(loc);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void stopAllTasks() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }
}
