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
        }.runTaskTimer(plugin, 0L, 1L);
        tasks.add(task);
    }

    // ═══════════════════════════════════════════════
    // ONE SHOT SWORD - Single Rotating Ring Cone
    // ONE ring visible at a time — rises from feet to above head,
    // then INSTANTLY resets. Each ring disappears the moment next one forms.
    // Wide at bottom (2.5), narrows to point at top (3.2 blocks high).
    // ═══════════════════════════════════════════════
    private void displayFireWingAura(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle fire = Particle.FLAME;

        // 120 ticks = 6 seconds for full rise, then instant reset
        int cycle = tick % 120;
        double progress = cycle / 120.0;

        // Rises to 3.2 blocks — above the head (head = ~1.8)
        double maxHeight = 3.2;
        double currentHeight = progress * maxHeight;

        // Wide at feet (2.5), narrows to point at top
        double coneRadius = 2.5 * (1.0 - progress * 0.96);

        // Slow rotation: 4 degrees per tick
        double baseAngle = Math.toRadians(tick * 4);

        // Single ring only — no trailing rings, so only 1 visible at a time
        int points = Math.max(6, (int)(28 * (1.0 - progress * 0.5)));

        for (int i = 0; i < points; i++) {
            double angle = baseAngle + (2 * Math.PI / points) * i;
            double x = Math.cos(angle) * coneRadius;
            double z = Math.sin(angle) * coneRadius;
            Location pos = base.clone().add(x, currentHeight, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.02, 0.03, 0.02, 0.003);
        }
    }

    // ═══════════════════════════════════════════════
    // GOD ARMOR - Soul Cloud feet -> head -> feet loop
    // ═══════════════════════════════════════════════
    private void displaySoulCloudLoop(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle soul = Particle.SOUL;

        int cycle = tick % 80;
        double progress = cycle <= 40 ? cycle / 40.0 : (80 - cycle) / 40.0;
        double centerHeight = progress * 1.9;

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

        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI / 8) * i - tick * 0.04;
            double radius = 0.5;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location pos = base.clone().add(x, centerHeight + Math.sin(tick * 0.1 + i) * 0.3, z);
            player.getWorld().spawnParticle(soul, pos, 1, 0.05, 0.05, 0.05, 0);
        }

        if (tick % 2 == 0) {
            double driftX = (Math.random() - 0.5) * 0.8;
            double driftZ = (Math.random() - 0.5) * 0.8;
            Location wisp = base.clone().add(driftX, 1.5 + Math.random() * 0.5, driftZ);
            player.getWorld().spawnParticle(soul, wisp, 1, 0.05, 0.05, 0.05, 0);
        }
    }

    // ═══════════════════════════════════════════════
    // POWER APPLE - Golden Spiral Aura
    // ═══════════════════════════════════════════════
    private void displayApplePowerAura(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle spark = Particle.END_ROD;

        double angle = Math.toRadians(tick * 12);
        int strands = 3;
        for (int s = 0; s < strands; s++) {
            double strandAngle = angle + (2 * Math.PI * s / strands);
            double radius = 0.5;
            double height = (tick % 30) / 30.0 * 2.0;

            double x = Math.cos(strandAngle) * radius;
            double z = Math.sin(strandAngle) * radius;
            Location pos = base.clone().add(x, height, z);
            player.getWorld().spawnParticle(spark, pos, 1, 0.02, 0.02, 0.02, 0.01);
        }

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
                for (int y = 0; y < 10; y++) {
                    Location p = loc.clone().add(0, y, 0);
                    loc.getWorld().spawnParticle(Particle.END_ROD, p, 5, 0.3, 0.1, 0.3, 0.05);
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
