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
                    // Respect per-player particle toggle
                    if (!plugin.getPowerManager().particlesEnabled(player)) continue;

                    int tick = plugin.getPowerManager().nextTick(player);

                    if (plugin.getPowerManager().isHoldingSword(player)) {
                        displayFireWingAura(player, tick);
                    }
                    if (plugin.getPowerManager().isWearingFullGodArmor(player)) {
                        displaySoulCloudLoop(player, tick);
                    }
                    if (plugin.getPowerManager().hasPowerAppleBoost(player)) {
                        displayApplePowerAura(player, tick);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Every tick = 20x/second for a dense, smooth loop
        tasks.add(task);
    }

    // ═══════════════════════════════════════════════
    // ONE SHOT SWORD - Rotating Fire Cone (Wide + Slow)
    // Wide ring at feet, slowly rotates and rises to head, then resets
    // ═══════════════════════════════════════════════
    private void displayFireWingAura(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle fire = Particle.FLAME;

        // Slow cycle: 120 ticks = 6 seconds from feet to head, then reset
        int cycle = tick % 120;
        double progress = cycle / 120.0; // 0.0 (feet) → 1.0 (head)

        // Wide cone: starts at radius 2.5, narrows to 0.15 at top
        double maxHeight = 2.0;
        double currentHeight = progress * maxHeight;
        double coneRadius = 2.5 * (1.0 - progress * 0.94);

        // Very slow rotation: 4 degrees per tick
        double baseAngle = Math.toRadians(tick * 4);

        // Dense ring of flames
        int points = Math.max(6, (int)(28 * (1.0 - progress * 0.5)));

        for (int i = 0; i < points; i++) {
            double angle = baseAngle + (2 * Math.PI / points) * i;
            double x = Math.cos(angle) * coneRadius;
            double z = Math.sin(angle) * coneRadius;
            Location pos = base.clone().add(x, currentHeight, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.02, 0.04, 0.02, 0.003);
        }

        // Second trailing ring for extra density
        double trailProgress = ((tick + 15) % 120) / 120.0;
        double trailHeight = trailProgress * maxHeight;
        double trailRadius = 2.5 * (1.0 - trailProgress * 0.94);
        double trailAngle = Math.toRadians((tick + 15) * 4);
        int trailPoints = Math.max(5, (int)(22 * (1.0 - trailProgress * 0.5)));

        for (int i = 0; i < trailPoints; i++) {
            double angle = trailAngle + (2 * Math.PI / trailPoints) * i;
            double x = Math.cos(angle) * trailRadius;
            double z = Math.sin(angle) * trailRadius;
            Location pos = base.clone().add(x, trailHeight, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.02, 0.03, 0.02, 0.003);
        }
    }

    // ═══════════════════════════════════════════════
    // GOD ARMOR - Soul Cloud, feet -> head -> feet, looping
    // Dense cluster of soul particles cycling along the body, cloud-like
    // rather than a rigid geometric shape.
    // ═══════════════════════════════════════════════
    private void displaySoulCloudLoop(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle soul = Particle.SOUL;

        // Full loop = 40 ticks up + 40 ticks down = 80 ticks total (~8 seconds at 10/s)
        int cycle = tick % 80;
        double progress = cycle <= 40 ? cycle / 40.0 : (80 - cycle) / 40.0;
        double centerHeight = progress * 1.9; // travels from feet (0) to head (~1.9)

        // Dense cluster of particles around the current height, randomized for a cloud look
        int particleCount = 14;
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI / particleCount) * i + tick * 0.05;
            double radius = 0.3 + Math.sin(tick * 0.1 + i) * 0.15;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double yJitter = Math.sin(tick * 0.15 + i * 2) * 0.25;

            Location pos = base.clone().add(x, centerHeight + yJitter, z);
            player.getWorld().spawnParticle(soul, pos, 1, 0.06, 0.06, 0.06, 0);
        }

        // Trailing cluster slightly behind the main band for extra thickness
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI / 8) * i - tick * 0.04;
            double radius = 0.5;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location pos = base.clone().add(x, centerHeight + Math.sin(tick * 0.1 + i) * 0.3, z);
            player.getWorld().spawnParticle(soul, pos, 1, 0.05, 0.05, 0.05, 0);
        }

        // A few extra wispy soul particles drifting near the head for a "cloud" feel
        if (tick % 2 == 0) {
            double driftX = (Math.random() - 0.5) * 0.8;
            double driftZ = (Math.random() - 0.5) * 0.8;
            Location wisp = base.clone().add(driftX, 1.5 + Math.random() * 0.5, driftZ);
            player.getWorld().spawnParticle(soul, wisp, 1, 0.05, 0.05, 0.05, 0);
        }
    }

    // ═══════════════════════════════════════════════
    // POWER APPLE - Golden Spark Aura (distinct from sword/armor effects)
    // Rising spiral of golden sparkles around the player while the boost is active.
    // ═══════════════════════════════════════════════
    private void displayApplePowerAura(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle spark = Particle.END_ROD;

        double angle = Math.toRadians(tick * 12);
        int strands = 3;
        for (int s = 0; s < strands; s++) {
            double strandAngle = angle + (2 * Math.PI * s / strands);
            double radius = 0.5;
            double height = (tick % 30) / 30.0 * 2.0; // continuous rising spiral, resets each loop

            double x = Math.cos(strandAngle) * radius;
            double z = Math.sin(strandAngle) * radius;

            Location pos = base.clone().add(x, height, z);
            player.getWorld().spawnParticle(spark, pos, 1, 0.02, 0.02, 0.02, 0.01);
        }

        // Gentle golden glow burst at the player's chest every second
        if (tick % 10 == 0) {
            player.getWorld().spawnParticle(Particle.WAX_ON, base.clone().add(0, 1.1, 0), 3, 0.2, 0.2, 0.2, 0.02);
        }
    }

    // ═══════════════════════════════════════════════
    // KILL ANIMATION - Celestial Judgment Beam
    // ═══════════════════════════════════════════════
    public void playKillAnimation(Player killer, org.bukkit.entity.Entity victim) {
        Location loc = victim.getLocation().add(0, 1, 0);
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t > 40) { cancel(); return; }
                killCelestialBeam(loc, t);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void killCelestialBeam(Location loc, int t) {
        // Beam from sky down
        for (int y = 0; y < 10; y++) {
            Location p = loc.clone().add(0, y, 0);
            loc.getWorld().spawnParticle(Particle.END_ROD, p, 5, 0.3, 0.1, 0.3, 0.05);
        }
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 1, 1, 1, 0.5);
        if (t == 10) loc.getWorld().strikeLightningEffect(loc);
    }

    public void stopAllTasks() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }
}
