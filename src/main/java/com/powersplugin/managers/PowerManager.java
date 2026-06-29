package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PowerManager {

    private final PowersPlugin plugin;

    private final Set<UUID> activeHolders = new HashSet<>();
    private final Map<UUID, Integer> animTick = new HashMap<>();
    private final Set<UUID> particlesDisabled = new HashSet<>();
    private final Map<UUID, Long> powerAppleExpiry = new HashMap<>();

    // God Armor Sonic Boom ability cooldown (24 hours)
    private final Map<UUID, Long> sonicBoomCooldown = new HashMap<>();
    public static final long SONIC_BOOM_COOLDOWN_MS = 24L * 60L * 60L * 1000L;

    public PowerManager(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Sword Hand Tracking ────────────────────────────────────────────
    public void setHoldingSword(Player player, boolean holding) {
        UUID id = player.getUniqueId();
        if (holding) {
            activeHolders.add(id);
            animTick.putIfAbsent(id, 0);
        } else {
            activeHolders.remove(id);
        }
    }

    public boolean isHoldingSword(Player player) {
        return activeHolders.contains(player.getUniqueId());
    }

    // ─── Animation Tick ──────────────────────────────────────────────────
    public int nextTick(Player player) {
        return animTick.merge(player.getUniqueId(), 1, Integer::sum);
    }

    // ─── God Armor Full-Set Check ────────────────────────────────────────
    public boolean isWearingFullGodArmor(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack legs = inv.getLeggings();
        ItemStack boots = inv.getBoots();

        return plugin.getCraftingManager().isGodArmor(helmet)
                && plugin.getCraftingManager().isGodArmor(chest)
                && plugin.getCraftingManager().isGodArmor(legs)
                && plugin.getCraftingManager().isGodArmor(boots);
    }

    // ─── Sonic Boom Cooldown ─────────────────────────────────────────────
    public boolean isSonicBoomOnCooldown(Player player) {
        Long expiry = sonicBoomCooldown.get(player.getUniqueId());
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            sonicBoomCooldown.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public void applySonicBoomCooldown(Player player) {
        sonicBoomCooldown.put(player.getUniqueId(), System.currentTimeMillis() + SONIC_BOOM_COOLDOWN_MS);
    }

    public String formatSonicBoomCooldown(Player player) {
        Long expiry = sonicBoomCooldown.get(player.getUniqueId());
        if (expiry == null) return "0h 0m 0s";
        long ms = Math.max(0, expiry - System.currentTimeMillis());
        long h = ms / 3600000;
        long m = (ms % 3600000) / 60000;
        long s = (ms % 60000) / 1000;
        return h + "h " + m + "m " + s + "s";
    }

    // ─── Particle Toggle ──────────────────────────────────────────────
    public boolean particlesEnabled(Player player) {
        return !particlesDisabled.contains(player.getUniqueId());
    }

    public boolean toggleParticles(Player player) {
        UUID id = player.getUniqueId();
        if (particlesDisabled.contains(id)) {
            particlesDisabled.remove(id);
            return true;
        } else {
            particlesDisabled.add(id);
            return false;
        }
    }

    // ─── Power Apple Boost ────────────────────────────────────────────
    public void applyPowerAppleBoost(Player player) {
        long expiry = System.currentTimeMillis() + (30 * 60 * 1000L);
        powerAppleExpiry.put(player.getUniqueId(), expiry);

        player.sendMessage("§8[§6Powers§8] §d✦ §fPower Apple activated! Combat boosted for §e30 minutes§f!");
        player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.3);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 36000, 2, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 36000, 2, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 36000, 2, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 36000, 1, false, true, true));
    }

    public boolean hasPowerAppleBoost(Player player) {
        Long expiry = powerAppleExpiry.get(player.getUniqueId());
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            powerAppleExpiry.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public double getDamageMultiplier(Player player) {
        return hasPowerAppleBoost(player) ? 1.5 : 1.0;
    }

    // ─── Cleanup ──────────────────────────────────────────────────────
    public void clearPlayerData(UUID uuid) {
        activeHolders.remove(uuid);
        animTick.remove(uuid);
        powerAppleExpiry.remove(uuid);
    }
}
