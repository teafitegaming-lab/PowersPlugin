package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import com.powersplugin.powers.CustomPower;
import com.powersplugin.weapons.CustomWeapon;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PowerManager {

    private final PowersPlugin plugin;
    private final Map<UUID, Set<CustomPower>> playerPowers = new HashMap<>();
    private final Map<UUID, Long> powerAppleExpiry = new HashMap<>();

    public PowerManager(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    public void grantPower(Player player, CustomPower power) {
        playerPowers.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(power);
        applyPowerEffects(player, power);
        player.sendMessage(plugin.getConfig().getString("plugin.prefix", "§8[§6Powers§8] ")
                + "§aYou obtained §r" + power.getDisplayName() + "§a power!");
    }

    public void removePower(Player player, CustomPower power) {
        Set<CustomPower> powers = playerPowers.get(player.getUniqueId());
        if (powers != null) powers.remove(power);
    }

    public boolean hasPower(Player player, CustomPower power) {
        Set<CustomPower> powers = playerPowers.get(player.getUniqueId());
        return powers != null && powers.contains(power);
    }

    public Set<CustomPower> getPlayerPowers(Player player) {
        return playerPowers.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public void grantAllPowers(Player player) {
        for (CustomPower power : CustomPower.values()) {
            grantPower(player, power);
        }
    }

    // Grant power based on weapon obtained
    public void grantPowerFromWeapon(Player player, CustomWeapon weapon) {
        grantPower(player, weapon.getAssociatedPower());
    }

    // Power Apple - 30 min boost
    public void applyPowerAppleBoost(Player player) {
        long expiry = System.currentTimeMillis() + (30 * 60 * 1000L);
        powerAppleExpiry.put(player.getUniqueId(), expiry);
        player.sendMessage("§8[§6Powers§8] §d✦ §fPower Apple activated! All weapon powers boosted for §e30 minutes§f!");

        // Visual effect
        player.getWorld().spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5, 0.3);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

        // Boost effects
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

    private void applyPowerEffects(Player player, CustomPower power) {
        switch (power) {
            case FIRE_LORD:
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false, true));
                break;
            case STORM_BRINGER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false, true));
                break;
            case VOID_WALKER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false, true));
                break;
            case FROST_TITAN:
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false, true));
                break;
            case NATURE_SAGE:
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, false, false, true));
                break;
            case SHADOW_REAPER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, true));
                break;
            case CELESTIAL_KNIGHT:
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2, false, false, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2, false, false, true));
                break;
        }
    }

    public void clearPlayerData(UUID uuid) {
        playerPowers.remove(uuid);
        powerAppleExpiry.remove(uuid);
    }
}
