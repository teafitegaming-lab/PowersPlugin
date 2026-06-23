package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Sound;

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

        // ── Cooldown Expiry Checker: har 20 ticks (1 second) check karo ──
        // Jab 12 hour cooldown khatam ho, player ko animation aur notification milti hai
        BukkitTask cooldownChecker = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    // isOnCooldown() returns false jab cooldown expire ho jaata hai
                    // aur automatically remove kar leta hai map se
                    boolean wasOnCooldown = plugin.getWeaponManager().hadCooldown(player);
                    boolean nowOnCooldown = plugin.getWeaponManager().isOnCooldown(player);

                    // Agar pehle cooldown tha aur ab nahi hai = abhi expire hua
                    if (wasOnCooldown && !nowOnCooldown) {
                        playCooldownReadyAnimation(player);
                        player.sendMessage("§8[§6Powers§8] §e⚡ §fCelestial Judgment cooldown khatam! §cStrike again!");
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
        tasks.add(cooldownChecker);
    }

    // ═══════════════════════════════════════════════
    // ONE SHOT SWORD - Fire Particles (circular orbit, player ke around se start)
    // Cone style NAHI — sirf circular rotating rings player ke around
    // Fire jaisa effect lekin alag animation mein
    // ═══════════════════════════════════════════════
    private void displayFireWingAura(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle fire = Particle.FLAME;

        // ── Ring 1: Feet level — fast clockwise rotation ──
        double angle1 = Math.toRadians(tick * 8);
        int ring1Points = 16;
        double ring1Radius = 1.1;
        for (int i = 0; i < ring1Points; i++) {
            double a = angle1 + (2 * Math.PI / ring1Points) * i;
            double x = Math.cos(a) * ring1Radius;
            double z = Math.sin(a) * ring1Radius;
            double yOffset = 0.15 + Math.sin(tick * 0.12 + i) * 0.1;
            Location pos = base.clone().add(x, yOffset, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.01, 0.04, 0.01, 0.002);
        }

        // ── Ring 2: Waist level — counter-clockwise ──
        double angle2 = Math.toRadians(-tick * 6);
        int ring2Points = 20;
        double ring2Radius = 0.9;
        for (int i = 0; i < ring2Points; i++) {
            double a = angle2 + (2 * Math.PI / ring2Points) * i;
            double x = Math.cos(a) * ring2Radius;
            double z = Math.sin(a) * ring2Radius;
            double yOffset = 0.9 + Math.sin(tick * 0.1 + i * 1.5) * 0.12;
            Location pos = base.clone().add(x, yOffset, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.01, 0.04, 0.01, 0.002);
        }

        // ── Ring 3: Head level — slow clockwise ──
        double angle3 = Math.toRadians(tick * 5 + 45);
        int ring3Points = 12;
        double ring3Radius = 0.55;
        for (int i = 0; i < ring3Points; i++) {
            double a = angle3 + (2 * Math.PI / ring3Points) * i;
            double x = Math.cos(a) * ring3Radius;
            double z = Math.sin(a) * ring3Radius;
            double yOffset = 1.75 + Math.sin(tick * 0.08 + i) * 0.08;
            Location pos = base.clone().add(x, yOffset, z);
            player.getWorld().spawnParticle(fire, pos, 1, 0.01, 0.03, 0.01, 0.001);
        }

        // ── Occasional rising spark ──
        if (tick % 5 == 0) {
            double rx = (Math.random() - 0.5) * 1.4;
            double rz = (Math.random() - 0.5) * 1.4;
            Location spark = base.clone().add(rx, 0.1, rz);
            player.getWorld().spawnParticle(fire, spark, 1, 0.02, 0.05, 0.02, 0.005);
        }
    }

    // ═══════════════════════════════════════════════
    // GOD ARMOR - Circular Orbital Rings (fire jaisi feel, alag animation)
    // Player ke AROUND se start hoti hain rings — cone NAHI
    // Teen orbital planes + floating soul particles
    // ═══════════════════════════════════════════════
    private void displaySoulCloudLoop(Player player, int tick) {
        Location base = player.getLocation().add(0, 0.05, 0);
        Particle soul = Particle.SOUL;
        Particle flame = Particle.SOUL_FIRE_FLAME;

        // ── Orbital Ring A: Horizontal, feet level, fast spin ──
        double angleA = Math.toRadians(tick * 7);
        int pointsA = 18;
        double radiusA = 1.3;
        for (int i = 0; i < pointsA; i++) {
            double a = angleA + (2 * Math.PI / pointsA) * i;
            double x = Math.cos(a) * radiusA;
            double z = Math.sin(a) * radiusA;
            double y = 0.2 + Math.sin(tick * 0.1 + i * 0.8) * 0.15;
            Location pos = base.clone().add(x, y, z);
            player.getWorld().spawnParticle(soul, pos, 1, 0.02, 0.04, 0.02, 0);
        }

        // ── Orbital Ring B: Tilted vertical ring, rotates around player ──
        double angleB = Math.toRadians(tick * 5);
        int pointsB = 16;
        double radiusB = 1.0;
        for (int i = 0; i < pointsB; i++) {
            double t = (2 * Math.PI / pointsB) * i;
            // Tilted ring: X is orbital rotation, Y+Z form the ring plane
            double ringX = Math.cos(angleB) * Math.cos(t) * radiusB;
            double ringY = Math.sin(t) * radiusB + 1.0;
            double ringZ = Math.sin(angleB) * Math.cos(t) * radiusB;
            Location pos = base.clone().add(ringX, ringY, ringZ);
            player.getWorld().spawnParticle(soul, pos, 1, 0.02, 0.02, 0.02, 0);
        }

        // ── Orbital Ring C: Counter-rotating, head area ──
        double angleC = Math.toRadians(-tick * 6 + 60);
        int pointsC = 12;
        double radiusC = 0.65;
        for (int i = 0; i < pointsC; i++) {
            double a = angleC + (2 * Math.PI / pointsC) * i;
            double x = Math.cos(a) * radiusC;
            double z = Math.sin(a) * radiusC;
            double y = 1.8 + Math.sin(tick * 0.09 + i) * 0.1;
            Location pos = base.clone().add(x, y, z);
            player.getWorld().spawnParticle(flame, pos, 1, 0.01, 0.03, 0.01, 0);
        }

        // ── Floating soul wisps around body ──
        if (tick % 3 == 0) {
            double wx = (Math.random() - 0.5) * 2.0;
            double wz = (Math.random() - 0.5) * 2.0;
            double wy = Math.random() * 2.0;
            Location wisp = base.clone().add(wx, wy, wz);
            player.getWorld().spawnParticle(soul, wisp, 1, 0.04, 0.04, 0.04, 0);
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

    // ═══════════════════════════════════════════════
    // COOLDOWN READY ANIMATION
    // Jab cooldown khatam ho ya /resetcooldown use ho tab chalti hai
    // Screen title + cool particle burst + sound
    // ═══════════════════════════════════════════════
    public void playCooldownReadyAnimation(Player player) {
        // ── Phase 1: Title on screen ──
        player.sendTitle(
            "§6§l⚡ CELESTIAL JUDGMENT ⚡",
            "§f§lREADY TO STRIKE §c§l☠",
            5, 50, 15
        );

        // ── Phase 2: Action bar message ──
        player.sendActionBar(net.kyori.adventure.text.Component.text(
            "§e✦ §fYour blade thirsts for blood... §e✦"
        ));

        // ── Phase 3: Sound effect ──
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.4f);

        // ── Phase 4: Animated particle burst over 2 seconds ──
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t > 40) { cancel(); return; }

                Location loc = player.getLocation().add(0, 1, 0);

                // Expanding ring burst from player
                double radius = (t / 40.0) * 3.0;
                int points = 24;
                double angle = Math.toRadians(t * 9);
                for (int i = 0; i < points; i++) {
                    double a = angle + (2 * Math.PI / points) * i;
                    double x = Math.cos(a) * radius;
                    double z = Math.sin(a) * radius;
                    Location pos = loc.clone().add(x, 0, z);
                    loc.getWorld().spawnParticle(Particle.END_ROD, pos, 1, 0, 0.05, 0, 0.01);
                    loc.getWorld().spawnParticle(Particle.FLAME, pos, 1, 0, 0.05, 0, 0.01);
                }

                // Central pillar of light
                if (t < 20) {
                    for (int y = 0; y < 8; y++) {
                        Location pillar = player.getLocation().add(
                            (Math.random() - 0.5) * 0.3,
                            y * 0.4,
                            (Math.random() - 0.5) * 0.3
                        );
                        loc.getWorld().spawnParticle(Particle.END_ROD, pillar, 1, 0.05, 0, 0.05, 0.02);
                    }
                }

                // At peak (t=20): big explosion burst
                if (t == 20) {
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 3, 0.5, 0.5, 0.5, 0);
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc, 100, 1.5, 1.5, 1.5, 0.4);
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 60, 1.0, 1.0, 1.0, 0.3);
                    player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
                }

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void stopAllTasks() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }
}
