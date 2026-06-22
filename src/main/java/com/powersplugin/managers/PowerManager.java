package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Tracks per-player runtime state needed for visuals and effects:
 *  - whether the one-shot sword is in the main hand right now (drives sword particles)
 *  - whether the full god armor set is worn right now (drives armor particles)
 *  - whether a power apple boost is active (drives apple particles)
 *  - whether the player has particles toggled on/off
 */
public class PowerManager {

    private final PowersPlugin plugin;

    // Players who currently have the sword in their main hand (particles should render)
    private final Set<UUID> activeHolders = new HashSet<>();
    // Per-player animation tick, reset whenever the sword is re-equipped
    private final Map<UUID, Integer> animTick = new HashMap<>();
    // Players who have disabled particle effects for themselves (default: enabled)
    private final Set<UUID> particlesDisabled = new HashSet<>();
    // Power apple boost expiry timestamps (ms since epoch)
    private final Map<UUID, Long> powerAppleExpiry = new HashMap<>();

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

    // ─── Animation Tick (shared by all active effects for this player) ──
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

    // ─── Particle Toggle ──────────────────────────────────────────────
    public boolean particlesEnabled(Player player) {
        return !particlesDisabled.contains(player.getUniqueId());
    }

    public boolean toggleParticles(Player player) {
        UUID id = player.getUniqueId();
        if (particlesDisabled.contains(id)) {
            particlesDisabled.remove(id);
            return true; // now enabled
        } else {
            particlesDisabled.add(id);
            return false; // now disabled
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

    // ─── Cleanup ────────────────────────────────────────────────────────
    public void clearPlayerData(UUID uuid) {
        activeHolders.remove(uuid);
        animTick.remove(uuid);
        powerAppleExpiry.remove(uuid);
        // Note: particle toggle preference intentionally persists across sessions in-memory
    }
}
