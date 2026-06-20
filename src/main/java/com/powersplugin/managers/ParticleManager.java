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
                    if (!plugin.getPowerManager().isHoldingSword(player)) continue;
                    int tick = plugin.getPowerManager().nextTick(player);
                    displayCubeLoopFeetToHead(player, tick);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Every 2 ticks = 10x/second for a smooth loop
        tasks.add(task);
    }

    // ═══════════════════════════════════════════════
    // ONE SHOT SWORD - Fire cube, feet -> head -> feet, looping
    // ═══════════════════════════════════════════════
    private void displayCubeLoopFeetToHead(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        double size = 0.55;

        // Full loop = 40 ticks up + 40 ticks down = 80 ticks total (~8 seconds at 10/s)
        int cycle = tick % 80;
        double progress;
        if (cycle <= 40) {
            progress = cycle / 40.0;      // 0 -> 1 (feet to head)
        } else {
            progress = (80 - cycle) / 40.0; // 1 -> 0 (head to feet)
        }
        double height = progress * 1.9; // player height ~1.8-2.0 blocks

        double[][] corners = {
            {-size, -size}, {size, -size}, {size, size}, {-size, size}
        };

        Particle fire = Particle.FLAME;

        // Corner pillars (small vertical flicker at each corner)
        for (double[] corner : corners) {
            Location pos = base.clone().add(corner[0], height, corner[1]);
            player.getWorld().spawnParticle(fire, pos, 1, 0.03, 0.05, 0.03, 0.01);
        }

        // Edges of the square ring at current height
        for (int i = 0; i < 4; i++) {
            double x = corners[i][0];
            double z = corners[i][1];
            double nx = corners[(i + 1) % 4][0];
            double nz = corners[(i + 1) % 4][1];

            int steps = 6;
            for (int s = 0; s <= steps; s++) {
                double px = x + (nx - x) * s / steps;
                double pz = z + (nz - z) * s / steps;
                Location edgePos = base.clone().add(px, height, pz);
                player.getWorld().spawnParticle(fire, edgePos, 1, 0, 0.02, 0, 0.005);
            }
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
