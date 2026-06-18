package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CrownManager {

    private final PowersPlugin plugin;
    private final NamespacedKey crownKey;
    private UUID crownHolder = null;

    public CrownManager(PowersPlugin plugin) {
        this.plugin = plugin;
        this.crownKey = new NamespacedKey(plugin, "ancient_crown");
    }

    public ItemStack createCrown() {
        ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.setCustomModelData(9999);
        meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&6&l✦ CROWN OF THE ANCIENT GUARDIAN ✦")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7Contains the power of ALL 7 weapons.").decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7The wearer wields every custom ability.").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&e✦ Fire Lord  &e✦ Storm Bringer").decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&e✦ Void Walker  &e✦ Frost Titan").decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&e✦ Nature Sage  &e✦ Shadow Reaper").decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&e✦ Celestial Knight").decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l[UNIQUE - 1 exists per server]").decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&8Obtained by defeating the Ancient Guardian").decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 10, true);
        meta.addEnchant(Enchantment.DURABILITY, 10, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.THORNS, 5, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);

        meta.getPersistentDataContainer().set(crownKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isCrown(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(crownKey, PersistentDataType.BYTE);
    }

    public boolean hasCrown(Player player) {
        return player.getUniqueId().equals(crownHolder);
    }

    public void setCrownHolder(Player player) {
        if (crownHolder != null) {
            Player old = plugin.getServer().getPlayer(crownHolder);
            if (old != null) {
                plugin.getPowerManager().clearPlayerData(old.getUniqueId());
                old.sendMessage("§8[§6Powers§8] §cYou lost the Crown and all its powers!");
            }
        }
        crownHolder = player.getUniqueId();
        plugin.getPowerManager().grantAllPowers(player);
        plugin.getServer().broadcast(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&6&l✦ " + player.getName() + " now wields the Crown of the Ancient Guardian! ✦"));
    }

    public void clearCrownHolder(UUID uuid) {
        if (uuid.equals(crownHolder)) crownHolder = null;
    }

    public NamespacedKey getCrownKey() { return crownKey; }
}
