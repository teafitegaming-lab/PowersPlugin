package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import com.powersplugin.weapons.CustomWeapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class WeaponManager {

    private final PowersPlugin plugin;
    private final NamespacedKey weaponKey;
    private final NamespacedKey oneShotCraftedKey;

    public WeaponManager(PowersPlugin plugin) {
        this.plugin = plugin;
        this.weaponKey = new NamespacedKey(plugin, "custom_weapon");
        this.oneShotCraftedKey = new NamespacedKey(plugin, "oneshot_crafted");
    }

    public ItemStack createWeapon(CustomWeapon weapon) {
        ItemStack item = new ItemStack(weapon.getBaseMaterial());
        ItemMeta meta = item.getItemMeta();

        // Unique model data per weapon
        meta.setCustomModelData(weapon.ordinal() + 1001);

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
                .deserialize("&6Power: " + weapon.getAssociatedPower().getDisplayName())
                .decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&e" + weapon.getAssociatedPower().getDescription())
                .decoration(TextDecoration.ITALIC, false));
        if (weapon.isOneShotWeapon()) {
            lore.add(Component.empty());
            lore.add(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&c&l⚠ ONE SHOT KILL - IGNORES TOTEM ⚠")
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&8[Unique - Only 1 exists per server]")
                    .decoration(TextDecoration.ITALIC, false));
        }

        meta.setLore(null); // clear default
        meta.lore(lore);

        // Enchantments
        meta.addEnchant(Enchantment.DAMAGE_ALL, weapon.isOneShotWeapon() ? 10 : 7, true);
        meta.addEnchant(Enchantment.DURABILITY, 10, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        if (weapon.isOneShotWeapon()) {
            meta.addEnchant(Enchantment.SWEEPING, 5, true);
        }

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

    public NamespacedKey getWeaponKey() { return weaponKey; }
    public NamespacedKey getOneShotCraftedKey() { return oneShotCraftedKey; }
}
