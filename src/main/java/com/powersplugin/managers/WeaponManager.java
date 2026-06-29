package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import com.powersplugin.weapons.CustomWeapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WeaponManager {

    private final PowersPlugin plugin;
    private final NamespacedKey weaponKey;

    public static final int ONE_SHOT_SWORD_MODEL_DATA = 1001;
    public static final long COOLDOWN_MS = 12L * 60L * 60L * 1000L;

    // UUID -> cooldown expiry timestamp (ms)
    private final Map<UUID, Long> swordCooldowns = new HashMap<>();
    // Track which players we already notified when cooldown expired
    private final Set<UUID> notified = new HashSet<>();

    public WeaponManager(PowersPlugin plugin) {
        this.plugin = plugin;
        this.weaponKey = new NamespacedKey(plugin, "custom_weapon");
        startCooldownNotificationTask();
    }

    // ─── Cooldown expiry notification task ──────────────────────────
    private void startCooldownNotificationTask() {
        new BukkitRunnable() {
            @Override public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    UUID id = player.getUniqueId();
                    Long expiry = swordCooldowns.get(id);
                    if (expiry == null) continue;

                    boolean expired = System.currentTimeMillis() >= expiry;
                    if (expired && !notified.contains(id)) {
                        notified.add(id);
                        swordCooldowns.remove(id);
                        fireCooldownReadyEffect(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // check every second
    }

    private void fireCooldownReadyEffect(Player player) {
        // Sound
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.5f);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        // Title in the middle of the screen
        player.sendTitle(
                "§f§l✦ CELESTIAL JUDGMENT ✦",
                "§c§lReady to strike again!",
                10, 80, 20
        );

        // Chat message
        player.sendMessage("§8[§6Powers§8] §f§l✦ §eYour §fCelestial Judgment §eis ready! §7Strike once more.");

        // Particle burst around the player
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.END_ROD, loc, 80, 1.5, 1.5, 1.5, 0.4);
        player.getWorld().spawnParticle(Particle.FLAME, loc, 60, 1, 1, 1, 0.3);

        // Rising spiral animation
        new BukkitRunnable() {
            int t = 0;
            @Override public void run() {
                if (t > 40) { cancel(); return; }
                double angle = Math.toRadians(t * 18);
                double radius = 1.2 - t * 0.025;
                double height = t * 0.07;
                Location p1 = player.getLocation().add(
                        Math.cos(angle) * radius, height, Math.sin(angle) * radius);
                Location p2 = player.getLocation().add(
                        Math.cos(angle + Math.PI) * radius, height, Math.sin(angle + Math.PI) * radius);
                player.getWorld().spawnParticle(Particle.FLAME, p1, 2, 0.05, 0.05, 0.05, 0.02);
                player.getWorld().spawnParticle(Particle.END_ROD, p2, 2, 0.05, 0.05, 0.05, 0.02);
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Lightning bolt visual above player
        player.getWorld().strikeLightningEffect(player.getLocation());
    }

    // ─── Weapon creation ────────────────────────────────────────────
    public ItemStack createWeapon(CustomWeapon weapon) {
        ItemStack item = new ItemStack(weapon.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.setCustomModelData(ONE_SHOT_SWORD_MODEL_DATA);

        meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(weapon.getDisplayName())
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(weapon.getLore()).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&c&l⚠ ONE SHOT KILL ⚠")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&7Shields fully block this weapon's attack.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&7Using it triggers a &e12 hour &7cooldown.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&8[Unique - Dropped only by the Ancient Guardian]")
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addEnchant(Enchantment.DAMAGE_ALL, 10, true);
        meta.addEnchant(Enchantment.DURABILITY, 10, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, weapon.getId());

        item.setItemMeta(meta);
        return item;
    }

    // ─── Cooldown methods ───────────────────────────────────────────
    public boolean isOnCooldown(Player player) {
        Long expiry = swordCooldowns.get(player.getUniqueId());
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            swordCooldowns.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public void applyCooldown(Player player) {
        swordCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + COOLDOWN_MS);
        notified.remove(player.getUniqueId()); // reset notification flag
    }

    public void clearCooldown(Player player) {
        swordCooldowns.remove(player.getUniqueId());
        notified.remove(player.getUniqueId());
        // Immediately fire the ready effect since it was manually cleared
        fireCooldownReadyEffect(player);
    }

    public String formatRemainingCooldown(Player player) {
        Long expiry = swordCooldowns.get(player.getUniqueId());
        if (expiry == null) return "0s";
        long ms = Math.max(0, expiry - System.currentTimeMillis());
        long h = ms / 3600000;
        long m = (ms % 3600000) / 60000;
        long s = (ms % 60000) / 1000;
        return h + "h " + m + "m " + s + "s";
    }

    // ─── Identification ─────────────────────────────────────────────
    public CustomWeapon getCustomWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer()
                .get(weaponKey, PersistentDataType.STRING);
        if (id == null) return null;
        for (CustomWeapon w : CustomWeapon.values()) {
            if (w.getId().equals(id)) return w;
        }
        return null;
    }

    public boolean isCustomWeapon(ItemStack item) {
        return getCustomWeapon(item) != null;
    }

    public NamespacedKey getWeaponKey() { return weaponKey; }
}
