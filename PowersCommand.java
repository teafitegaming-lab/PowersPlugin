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
    // ONE SHOT SWORD - Rotating Fire Cone
    // Flames spawn in a wide circle at the feet and spiral upward,
    // forming a rotating cone that narrows at the top.
    // Resets and repeats continuously.
    // ═══════════════════════════════════════════════
    private void displayFireWingAura(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle fire = Particle.FLAME;

        // Full cone rise cycle: 30 ticks = 1.5 seconds from feet to head, then reset
        int cycle = tick % 30;
        double progress = cycle / 30.0; // 0.0 (feet) → 1.0 (head/top)

        // Cone shape: wide at bottom (radius 0.9), narrow at top (radius 0.1)
        double maxHeight = 2.0; // top of cone (head level)
        double currentHeight = progress * maxHeight;
        double coneRadius = 0.9 * (1.0 - progress * 0.88); // 0.9 at feet → ~0.11 at top

        // Rotation angle increases with tick — makes the ring rotate as it rises
        double baseAngle = Math.toRadians(tick * 18); // rotates 18 degrees per tick

        // Number of flame points in the ring (more at bottom, fewer at top)
        int points = Math.max(4, (int)(16 * (1.0 - progress * 0.6)));

        for (int i = 0; i < points; i++) {
            double angle = baseAngle + (2 * Math.PI / points) * i;
            double x = Math.cos(angle) * coneRadius;
            double z = Math.sin(angle) * coneRadius;

            Location pos = base.clone().add(x, currentHeight, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.02, 0.04, 0.02, 0.005);
        }

        // Second ring slightly behind (half cycle offset) for extra density/trail effect
        double trailProgress = ((tick + 8) % 30) / 30.0;
        double trailHeight = trailProgress * maxHeight;
        double trailRadius = 0.9 * (1.0 - trailProgress * 0.88);
        double trailAngle = Math.toRadians((tick + 8) * 18);
        int trailPoints = Math.max(4, (int)(12 * (1.0 - trailProgress * 0.6)));

        for (int i = 0; i < trailPoints; i++) {
            double angle = trailAngle + (2 * Math.PI / trailPoints) * i;
            double x = Math.cos(angle) * trailRadius;
            double z = Math.sin(angle) * trailRadius;
            Location pos = base.clone().add(x, trailHeight, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.02, 0.03, 0.02, 0.004);
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
