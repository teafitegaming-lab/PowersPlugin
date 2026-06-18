package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import com.powersplugin.powers.CustomPower;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ParticleManager {

    private final PowersPlugin plugin;
    private final List<BukkitTask> tasks = new ArrayList<>();
    // Track per-player animation ticks for continuous effects
    private final Map<UUID, Integer> animTick = new HashMap<>();

    public ParticleManager(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    public void startParticleTasks() {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    int tick = animTick.merge(player.getUniqueId(), 1, Integer::sum);
                    Set<CustomPower> powers = plugin.getPowerManager().getPlayerPowers(player);
                    for (CustomPower power : powers) {
                        displayPowerParticles(player, power, tick);
                    }
                    // Crown particle display
                    if (plugin.getCrownManager().hasCrown(player)) {
                        displayCrownParticles(player, tick);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 3L); // Every 3 ticks = 6 times/second for smooth animation
        tasks.add(task);
    }

    private void displayPowerParticles(Player player, CustomPower power, int tick) {
        Location loc = player.getLocation().add(0, 0.1, 0);

        switch (power.getEffectType()) {
            case CUBE_BOTTOM_TO_TOP -> displayCubeBottomToTop(player, loc, power, tick);
            case DOUBLE_HELIX_SPIRAL -> displayDoubleHelixSpiral(player, loc, power, tick);
            case EXPANDING_RINGS -> displayExpandingRings(player, loc, power, tick);
            case HORIZONTAL_ORBIT -> displayHorizontalOrbit(player, loc, power, tick);
            case SWIRLING_CLOAK -> displaySwirlingCloak(player, loc, power, tick);
            case SHADOW_X_TRAIL -> displayShadowXTrail(player, loc, power, tick);
            case ANGEL_WINGS -> displayAngelWings(player, loc, power, tick);
        }
    }

    // ═══════════════════════════════════════════════
    // 1. FIRE LORD - Cube Bottom to Top (Fire Particles)
    // ═══════════════════════════════════════════════
    private void displayCubeBottomToTop(Player player, Location center, CustomPower power, int tick) {
        double size = 0.6;
        double height = (tick % 40) / 40.0 * 2.0; // Animates from 0 to 2 height

        double[][] corners = {
            {-size, -size}, {size, -size}, {size, size}, {-size, size}
        };

        for (double[] corner : corners) {
            Location pos = center.clone().add(corner[0], height, corner[1]);
            spawnColoredDust(player, pos, power.getParticle(), power.getColor(), 0.8f);
        }

        // Edges of cube at current height
        for (int i = 0; i < 4; i++) {
            double x = corners[i][0];
            double z = corners[i][1];
            double nx = corners[(i + 1) % 4][0];
            double nz = corners[(i + 1) % 4][1];

            int steps = 6;
            for (int s = 0; s <= steps; s++) {
                double px = x + (nx - x) * s / steps;
                double pz = z + (nz - z) * s / steps;
                Location edgePos = center.clone().add(px, height, pz);
                player.getWorld().spawnParticle(power.getParticle(), edgePos, 1, 0, 0.05, 0, 0.01);
            }
        }
    }

    // ═══════════════════════════════════════════════
    // 2. STORM BRINGER - Double Helix Spiral (Electric)
    // ═══════════════════════════════════════════════
    private void displayDoubleHelixSpiral(Player player, Location center, CustomPower power, int tick) {
        double angleStep = Math.PI / 20;
        double angle = tick * angleStep;
        double radius = 0.6;

        for (int i = 0; i < 20; i++) {
            double t = i / 20.0;
            double y = t * 2.0; // 0 to 2 blocks height
            double a = angle + t * Math.PI * 4;

            // Strand 1
            double x1 = Math.cos(a) * radius;
            double z1 = Math.sin(a) * radius;
            Location p1 = center.clone().add(x1, y, z1);
            player.getWorld().spawnParticle(power.getParticle(), p1, 1, 0.02, 0.02, 0.02, 0);

            // Strand 2 (offset by PI)
            double x2 = Math.cos(a + Math.PI) * radius;
            double z2 = Math.sin(a + Math.PI) * radius;
            Location p2 = center.clone().add(x2, y, z2);
            player.getWorld().spawnParticle(power.getParticle(), p2, 1, 0.02, 0.02, 0.02, 0);
        }
    }

    // ═══════════════════════════════════════════════
    // 3. VOID WALKER - Expanding Rings (Portal particles)
    // ═══════════════════════════════════════════════
    private void displayExpandingRings(Player player, Location center, CustomPower power, int tick) {
        int phase = tick % 60;
        double expansion = phase / 60.0;

        // 3 rings at different heights
        double[] heights = {0.3, 1.0, 1.7};
        for (double h : heights) {
            double radius = 0.3 + expansion * 0.8;
            int points = 20;
            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                Location pos = center.clone().add(x, h, z);
                player.getWorld().spawnParticle(power.getParticle(), pos, 1, 0, 0, 0, 0.02);
            }
        }
    }

    // ═══════════════════════════════════════════════
    // 4. FROST TITAN - Horizontal Orbit Shield (Snowflakes)
    // ═══════════════════════════════════════════════
    private void displayHorizontalOrbit(Player player, Location center, CustomPower power, int tick) {
        double orbitRadius = 1.0;
        double orbitHeight = 1.0;
        double angleStep = Math.toRadians(tick * 6); // 6 degrees per tick

        // 4 snowflakes orbiting horizontally
        for (int i = 0; i < 4; i++) {
            double angle = angleStep + (Math.PI * 2 * i / 4);
            double x = Math.cos(angle) * orbitRadius;
            double z = Math.sin(angle) * orbitRadius;
            Location pos = center.clone().add(x, orbitHeight, z);
            player.getWorld().spawnParticle(power.getParticle(), pos, 3, 0.05, 0.05, 0.05, 0.01);

            // Trail behind each orbiting flake
            for (int t = 1; t <= 3; t++) {
                double trailAngle = angle - Math.toRadians(t * 10);
                double tx = Math.cos(trailAngle) * orbitRadius;
                double tz = Math.sin(trailAngle) * orbitRadius;
                Location trail = center.clone().add(tx, orbitHeight, tz);
                player.getWorld().spawnParticle(power.getParticle(), trail, 1, 0, 0, 0, 0);
            }
        }
    }

    // ═══════════════════════════════════════════════
    // 5. NATURE SAGE - Swirling Cloak (Leaf particles)
    // ═══════════════════════════════════════════════
    private void displaySwirlingCloak(Player player, Location center, CustomPower power, int tick) {
        double angle = Math.toRadians(tick * 8);
        int numStreams = 5;

        for (int s = 0; s < numStreams; s++) {
            double streamAngle = angle + (2 * Math.PI * s / numStreams);
            for (int i = 0; i < 8; i++) {
                double t = i / 8.0;
                double spiralAngle = streamAngle + t * Math.PI * 2;
                double radius = 0.3 + t * 0.7;
                double height = t * 2.0;
                double x = Math.cos(spiralAngle) * radius;
                double z = Math.sin(spiralAngle) * radius;
                Location pos = center.clone().add(x, height, z);
                player.getWorld().spawnParticle(power.getParticle(), pos, 1, 0, 0.05, 0, 0.01);
            }
        }
    }

    // ═══════════════════════════════════════════════
    // 6. SHADOW REAPER - X Cross Shadow Trail from feet
    // ═══════════════════════════════════════════════
    private void displayShadowXTrail(Player player, Location center, CustomPower power, int tick) {
        double spread = 0.8;
        double height = 0.1 + (tick % 20) / 20.0 * 0.5; // slight floating motion

        // X pattern at feet
        double[][] xPoints = {
            {-spread, -spread}, {-spread/2, -spread/2}, {0, 0},
            {spread/2, spread/2}, {spread, spread},
            {spread, -spread}, {spread/2, -spread/2},
            {-spread/2, spread/2}, {-spread, spread}
        };

        for (double[] pt : xPoints) {
            Location pos = center.clone().add(pt[0], height, pt[1]);
            player.getWorld().spawnParticle(power.getParticle(), pos, 1, 0.05, 0.05, 0.05, 0.01);
        }

        // Rising shadow tendrils
        for (int i = 0; i < 4; i++) {
            double tendAngle = Math.toRadians(tick * 5 + i * 90);
            double tendRadius = 0.4 + Math.sin(tick * 0.2 + i) * 0.2;
            double tx = Math.cos(tendAngle) * tendRadius;
            double tz = Math.sin(tendAngle) * tendRadius;
            double th = (tick % 15) / 15.0 * 1.5;
            Location tendPos = center.clone().add(tx, th, tz);
            player.getWorld().spawnParticle(power.getParticle(), tendPos, 1, 0.03, 0.1, 0.03, 0.005);
        }
    }

    // ═══════════════════════════════════════════════
    // 7. CELESTIAL KNIGHT - Angel Wings (End Rod particles)
    // ═══════════════════════════════════════════════
    private void displayAngelWings(Player player, Location center, CustomPower power, int tick) {
        double wingFlap = Math.sin(tick * 0.15) * 0.3;
        double bodyHeight = 1.3;

        // Left wing
        for (int r = 1; r <= 5; r++) {
            double wRadius = r * 0.25;
            int points = 6 + r * 2;
            for (int p = 0; p < points; p++) {
                double wingAngle = Math.PI * 0.3 + (Math.PI * 0.7 * p / points);
                double wx = -Math.cos(wingAngle) * wRadius * (1 + wingFlap);
                double wy = bodyHeight + Math.sin(wingAngle) * wRadius * 0.6;
                double wz = 0;
                Location wPos = center.clone().add(wx, wy, wz);
                player.getWorld().spawnParticle(power.getParticle(), wPos, 1, 0.02, 0.02, 0.02, 0.01);
            }
        }

        // Right wing (mirror)
        for (int r = 1; r <= 5; r++) {
            double wRadius = r * 0.25;
            int points = 6 + r * 2;
            for (int p = 0; p < points; p++) {
                double wingAngle = Math.PI * 0.3 + (Math.PI * 0.7 * p / points);
                double wx = Math.cos(wingAngle) * wRadius * (1 + wingFlap);
                double wy = bodyHeight + Math.sin(wingAngle) * wRadius * 0.6;
                double wz = 0;
                Location wPos = center.clone().add(wx, wy, wz);
                player.getWorld().spawnParticle(power.getParticle(), wPos, 1, 0.02, 0.02, 0.02, 0.01);
            }
        }

        // Halo above head
        double haloRadius = 0.4;
        int haloPoints = 16;
        for (int i = 0; i < haloPoints; i++) {
            double ha = 2 * Math.PI * i / haloPoints;
            double hx = Math.cos(ha) * haloRadius;
            double hz = Math.sin(ha) * haloRadius;
            Location hPos = center.clone().add(hx, 2.3, hz);
            player.getWorld().spawnParticle(power.getParticle(), hPos, 1, 0, 0.02, 0, 0.005);
        }
    }

    // ═══════════════════════════════════════════════
    // CROWN - All powers combined display
    // ═══════════════════════════════════════════════
    private void displayCrownParticles(Player player, int tick) {
        Location loc = player.getLocation().add(0, 0.1, 0);
        // Rotating ring of all power particles
        for (int i = 0; i < CustomPower.values().length; i++) {
            CustomPower p = CustomPower.values()[i];
            double angle = Math.toRadians(tick * 4 + i * (360.0 / CustomPower.values().length));
            double r = 1.2;
            double x = Math.cos(angle) * r;
            double z = Math.sin(angle) * r;
            Location pos = loc.clone().add(x, 1.0, z);
            player.getWorld().spawnParticle(p.getParticle(), pos, 1, 0.05, 0.1, 0.05, 0.02);
        }

        // Crown above head
        double crownRadius = 0.5;
        int crownPoints = 12;
        for (int i = 0; i < crownPoints; i++) {
            double ca = 2 * Math.PI * i / crownPoints;
            double cx = Math.cos(ca) * crownRadius;
            double cz = Math.sin(ca) * crownRadius;
            double cy = 2.4 + Math.sin(ca * 3 + tick * 0.1) * 0.15;
            Location cPos = player.getLocation().clone().add(cx, cy, cz);
            player.getWorld().spawnParticle(Particle.END_ROD, cPos, 1, 0, 0, 0, 0.01);
        }
    }

    private void spawnColoredDust(Player player, Location loc, Particle particle, org.bukkit.Color color, float size) {
        if (particle == Particle.REDSTONE) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, size);
            player.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dustOptions);
        } else {
            player.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0.01);
        }
    }

    // ═══════════════════════════════════════════════
    // KILL ANIMATIONS - play on kill
    // ═══════════════════════════════════════════════
    public void playKillAnimation(Player killer, org.bukkit.entity.Entity victim, com.powersplugin.weapons.CustomWeapon weapon) {
        Location loc = victim.getLocation().add(0, 1, 0);
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t > 40) { cancel(); return; }
                switch (weapon.getKillEffect()) {
                    case FIRE_EXPLOSION_RING -> killFireRing(loc, t);
                    case LIGHTNING_STORM -> killLightningStorm(loc, t);
                    case VOID_IMPLOSION -> killVoidImplosion(loc, t);
                    case ICE_SHATTER_BURST -> killIceBurst(loc, t);
                    case LEAF_TORNADO -> killLeafTornado(loc, t);
                    case SHADOW_COLLAPSE -> killShadowCollapse(loc, t);
                    case CELESTIAL_JUDGMENT_BEAM -> killCelestialBeam(loc, t);
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void killFireRing(Location loc, int t) {
        double radius = t * 0.25;
        int pts = 24;
        for (int i = 0; i < pts; i++) {
            double a = 2 * Math.PI * i / pts;
            Location p = loc.clone().add(Math.cos(a) * radius, 0, Math.sin(a) * radius);
            loc.getWorld().spawnParticle(Particle.FLAME, p, 3, 0.1, 0.3, 0.1, 0.05);
        }
        if (t == 5) loc.getWorld().createExplosion(loc, 0f, false, false);
    }

    private void killLightningStorm(Location loc, int t) {
        for (int i = 0; i < 3; i++) {
            Location rand = loc.clone().add((Math.random()-0.5)*3, 0, (Math.random()-0.5)*3);
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, rand, 15, 0.5, 1, 0.5, 0.1);
        }
        if (t % 5 == 0) loc.getWorld().strikeLightningEffect(loc);
    }

    private void killVoidImplosion(Location loc, int t) {
        double radius = Math.max(0, 2.0 - t * 0.05);
        int pts = 20;
        for (int i = 0; i < pts; i++) {
            double a = 2 * Math.PI * i / pts;
            Location p = loc.clone().add(Math.cos(a) * radius, Math.random() * 2, Math.sin(a) * radius);
            loc.getWorld().spawnParticle(Particle.PORTAL, p, 5, 0.2, 0.2, 0.2, 0.5);
        }
    }

    private void killIceBurst(Location loc, int t) {
        for (int i = 0; i < 8; i++) {
            double a = 2 * Math.PI * i / 8;
            double r = t * 0.15;
            Location p = loc.clone().add(Math.cos(a) * r, t * 0.05, Math.sin(a) * r);
            loc.getWorld().spawnParticle(Particle.SNOWFLAKE, p, 5, 0.2, 0.3, 0.2, 0.05);
        }
        loc.getWorld().spawnParticle(Particle.ITEM_CRACK, loc, 20, 0.5, 0.5, 0.5, 0.2, new org.bukkit.inventory.ItemStack(org.bukkit.Material.SNOWBALL));
    }

    private void killLeafTornado(Location loc, int t) {
        double angle = Math.toRadians(t * 18);
        double radius = 0.5 + Math.sin(t * 0.3) * 0.3;
        for (int i = 0; i < 5; i++) {
            double a = angle + 2 * Math.PI * i / 5;
            Location p = loc.clone().add(Math.cos(a) * radius, t * 0.05, Math.sin(a) * radius);
            loc.getWorld().spawnParticle(Particle.COMPOSTER, p, 3, 0.1, 0.2, 0.1, 0.1);
        }
    }

    private void killShadowCollapse(Location loc, int t) {
        for (int i = 0; i < 10; i++) {
            double rx = (Math.random() - 0.5) * 3;
            double ry = Math.random() * 3;
            double rz = (Math.random() - 0.5) * 3;
            Location p = loc.clone().add(rx, ry, rz);
            loc.getWorld().spawnParticle(Particle.ASH, p, 3, 0, 0, 0, 0.1);
        }
        loc.getWorld().spawnParticle(Particle.SQUID_INK, loc.clone().add(0,1,0), 30, 0.5, 0.5, 0.5, 0.2);
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
