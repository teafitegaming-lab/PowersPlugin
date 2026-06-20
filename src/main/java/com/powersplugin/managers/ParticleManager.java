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
        }.runTaskTimer(plugin, 0L, 2L); // Every 2 ticks = 10x/second for a smooth loop
        tasks.add(task);
    }

    // ═══════════════════════════════════════════════
    // ONE SHOT SWORD - Fire Column + Ground Ring
    // A wide ring of flames sits at the player's feet at all times, while a
    // central column of flame continuously rises from the feet, past the head,
    // peaks above, then falls back down and repeats - matching the reference look.
    // ═══════════════════════════════════════════════
    private void displayFireWingAura(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle fire = Particle.FLAME;

        // ── Ground ring: a wide circle of flame around the feet, always present ──
        int ringPoints = 14;
        double ringRadius = 0.9;
        for (int i = 0; i < ringPoints; i++) {
            double angle = (2 * Math.PI / ringPoints) * i + tick * 0.03;
            double radius = ringRadius + Math.sin(tick * 0.2 + i) * 0.1;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            // Slight flicker height so the ring isn't perfectly flat
            double y = 0.05 + Math.abs(Math.sin(tick * 0.3 + i * 1.5)) * 0.25;

            Location pos = base.clone().add(x, y, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.03, 0.02, 0.03, 0.005);
        }

        // ── Rising central column: travels from feet, past head, peaks, falls back ──
        // Full cycle = 60 ticks (~6 seconds): 0-40 rising past head, 40-60 falling back
        int cycle = tick % 60;
        double columnProgress;
        if (cycle <= 40) {
            columnProgress = cycle / 40.0;       // 0 -> 1 (feet to above head)
        } else {
            columnProgress = (60 - cycle) / 20.0; // 1 -> 0 (falling back faster)
        }
        double peakHeight = 3.2; // rises well above the head (~1.8 block player height)
        double columnHeight = columnProgress * peakHeight;

        // Column tapers into a tighter point near the top, like a flame jet
        double columnSpread = 0.18 * (1.0 - columnProgress * 0.6);
        int columnPoints = 5;
        for (int i = 0; i < columnPoints; i++) {
            double angle = (2 * Math.PI / columnPoints) * i + tick * 0.4;
            double x = Math.cos(angle) * columnSpread;
            double z = Math.sin(angle) * columnSpread;
            Location pos = base.clone().add(x, columnHeight, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.02, 0.04, 0.02, 0.008);
        }

        // Trailing embers just below the column tip for a denser, "licking" flame look
        Location trail1 = base.clone().add(0, Math.max(0, columnHeight - 0.3), 0);
        Location trail2 = base.clone().add(0, Math.max(0, columnHeight - 0.6), 0);
        player.getWorld().spawnParticle(fire, trail1, 2, 0.1, 0.05, 0.1, 0.01);
        player.getWorld().spawnParticle(fire, trail2, 2, 0.15, 0.05, 0.15, 0.01);
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

        // Cluster of particles around the current height, randomized for a cloud look
        int particleCount = 6;
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI / particleCount) * i + tick * 0.05;
            double radius = 0.35 + Math.sin(tick * 0.1 + i) * 0.1;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double yJitter = Math.sin(tick * 0.15 + i * 2) * 0.15;

            Location pos = base.clone().add(x, centerHeight + yJitter, z);
            player.getWorld().spawnParticle(soul, pos, 1, 0.05, 0.05, 0.05, 0);
        }

        // A few extra wispy soul particles drifting near the head for a "cloud" feel
        if (tick % 3 == 0) {
            double driftX = (Math.random() - 0.5) * 0.7;
            double driftZ = (Math.random() - 0.5) * 0.7;
            Location wisp = base.clone().add(driftX, 1.6 + Math.random() * 0.4, driftZ);
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
