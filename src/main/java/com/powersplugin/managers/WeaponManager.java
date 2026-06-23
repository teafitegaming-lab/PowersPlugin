package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import com.powersplugin.weapons.CustomWeapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class WeaponManager {

    private final PowersPlugin plugin;
    private final NamespacedKey weaponKey;

    // Custom Model Data values - match these in the resource pack
    public static final int ONE_SHOT_SWORD_MODEL_DATA = 1001;

    // Cooldown tracking: UUID -> timestamp (ms) when cooldown ends
    private final Map<UUID, Long> swordCooldowns = new HashMap<>();
    public static final long COOLDOWN_MS = 12L * 60L * 60L * 1000L; // 12 hours

    public WeaponManager(PowersPlugin plugin) {
        this.plugin = plugin;
        this.weaponKey = new NamespacedKey(plugin, "custom_weapon");
    }

    public ItemStack createWeapon(CustomWeapon weapon) {
        ItemStack item = new ItemStack(weapon.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();

        // Custom Model Data - resource pack uses this to override the texture/model
        meta.setCustomModelData(ONE_SHOT_SWORD_MODEL_DATA);

        // Name
        meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(weapon.getDisplayName())
                .decoration(TextDecoration.ITALIC, false));

        // Lore
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

        meta.setLore(null);
        meta.lore(lore);

        // Enchantments (purely visual glow via hidden enchant + flag)
        meta.addEnchant(Enchantment.DAMAGE_ALL, 10, true);
        meta.addEnchant(Enchantment.DURABILITY, 10, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);

        // NBT Tag
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, weapon.getId());

        item.setItemMeta(meta);
        return item;
    }

    public CustomWeapon getCustomWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(weaponKey, PersistentDataType.STRING);
        if (id == null) return null;

        for (CustomWeapon weapon : CustomWeapon.values()) {
            if (weapon.getId().equals(id)) return weapon;
        }
        return null;
    }

    public boolean isCustomWeapon(ItemStack item) {
        return getCustomWeapon(item) != null;
    }

    // ─── Cooldown Management ────────────────────────────────────────────
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
    }

    public void clearCooldown(Player player) {
        swordCooldowns.remove(player.getUniqueId());
    }

    public long getRemainingCooldownMillis(Player player) {
        Long expiry = swordCooldowns.get(player.getUniqueId());
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public String formatRemainingCooldown(Player player) {
        long ms = getRemainingCooldownMillis(player);
        long totalSeconds = ms / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    public NamespacedKey getWeaponKey() { return weaponKey; }
}
