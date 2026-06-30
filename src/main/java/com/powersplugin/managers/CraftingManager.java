package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CraftingManager {

    private final PowersPlugin plugin;
    private final NamespacedKey godArmorKey;
    private final NamespacedKey powerAppleKey;

    // Custom Model Data values for the god armor pieces - match in resource pack
    public static final int GOD_HELMET_MODEL_DATA = 7001;
    public static final int GOD_CHESTPLATE_MODEL_DATA = 7002;
    public static final int GOD_LEGGINGS_MODEL_DATA = 7003;
    public static final int GOD_BOOTS_MODEL_DATA = 7004;

    public CraftingManager(PowersPlugin plugin) {
        this.plugin = plugin;
        this.godArmorKey = new NamespacedKey(plugin, "god_armor");
        this.powerAppleKey = new NamespacedKey(plugin, "power_apple");
        registerAllRecipes();
    }

    private void registerAllRecipes() {
        registerGodArmorRecipes();
        registerPowerAppleRecipe();
        registerBossSummonItemRecipes();
    }

    // ─── GOD ARMOR RECIPES (Full Set: Helmet, Chestplate, Leggings, Boots) ─
    private void registerGodArmorRecipes() {
        addShapedRecipe("recipe_god_helmet", createGodHelmet(),
                "NNN", "NDN", "   ",
                'N', Material.NETHERITE_INGOT,
                'D', Material.DIAMOND_HELMET);

        addShapedRecipe("recipe_god_chestplate", createGodChestplate(),
                "NDN", "DDD", "NDN",
                'N', Material.NETHERITE_INGOT,
                'D', Material.DIAMOND_CHESTPLATE);

        addShapedRecipe("recipe_god_leggings", createGodLeggings(),
                "NDN", "NDN", "NDN",
                'N', Material.NETHERITE_INGOT,
                'D', Material.DIAMOND_LEGGINGS);

        addShapedRecipe("recipe_god_boots", createGodBoots(),
                "N N", "NDN", "   ",
                'N', Material.NETHERITE_INGOT,
                'D', Material.DIAMOND_BOOTS);
    }

    private ItemMeta baseGodMeta(ItemStack item, String pieceName, int modelData) {
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(modelData);
        meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&b&l✦ GOD " + pieceName + " ✦")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7Part of the strongest armor set in existence.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&b&lAbility: §fShift + Right Click")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7Unleash a Sonic Boom — destroys")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7nearby enemies' armor. &e24h cooldown.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[Unique - Craft once per server]")
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addEnchant(org.bukkit.Registry.ENCHANTMENT.get(
                org.bukkit.NamespacedKey.minecraft("protection")), 10, true);
        meta.addEnchant(org.bukkit.Registry.ENCHANTMENT.get(
                org.bukkit.NamespacedKey.minecraft("unbreaking")), 10, true);
        meta.addEnchant(org.bukkit.Registry.ENCHANTMENT.get(
                org.bukkit.NamespacedKey.minecraft("mending")), 1, true);
        meta.addEnchant(org.bukkit.Registry.ENCHANTMENT.get(
                org.bukkit.NamespacedKey.minecraft("thorns")), 5, true);
        meta.addEnchant(org.bukkit.Registry.ENCHANTMENT.get(
                org.bukkit.NamespacedKey.minecraft("blast_protection")), 10, true);
        meta.addEnchant(org.bukkit.Registry.ENCHANTMENT.get(
                org.bukkit.NamespacedKey.minecraft("projectile_protection")), 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);

        meta.getPersistentDataContainer().set(godArmorKey, PersistentDataType.BYTE, (byte) 1);
        return meta;
    }

    public ItemStack createGodHelmet() {
        ItemStack item = new ItemStack(Material.NETHERITE_HELMET);
        item.setItemMeta(baseGodMeta(item, "HELMET", GOD_HELMET_MODEL_DATA));
        return item;
    }

    public ItemStack createGodChestplate() {
        ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE);
        item.setItemMeta(baseGodMeta(item, "CHESTPLATE", GOD_CHESTPLATE_MODEL_DATA));
        return item;
    }

    public ItemStack createGodLeggings() {
        ItemStack item = new ItemStack(Material.NETHERITE_LEGGINGS);
        item.setItemMeta(baseGodMeta(item, "LEGGINGS", GOD_LEGGINGS_MODEL_DATA));
        return item;
    }

    public ItemStack createGodBoots() {
        ItemStack item = new ItemStack(Material.NETHERITE_BOOTS);
        item.setItemMeta(baseGodMeta(item, "BOOTS", GOD_BOOTS_MODEL_DATA));
        return item;
    }

    // ─── POWER APPLE RECIPE ─────────────────────────────────────────────
    private void registerPowerAppleRecipe() {
        ItemStack apple = createPowerApple();
        addShapedRecipe("recipe_power_apple",
                apple,
                "GGG", "GAG", "GGG",
                'G', Material.NETHER_STAR,
                'A', Material.ENCHANTED_GOLDEN_APPLE);
    }

    public ItemStack createPowerApple() {
        ItemStack item = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(6666);
        meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&d&l✦ Power Apple ✦")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7Boosts combat ability by &e1.5x")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7for &a30 minutes&7.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l[Only 2 can exist per server]")
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(powerAppleKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    // ─── BOSS SUMMON ITEM RECIPES ────────────────────────────────────────
    private void registerBossSummonItemRecipes() {
        ItemStack s0 = plugin.getBossManager().createSummonItem(0);
        addShapedRecipe("recipe_summon0", s0,
                "SSS", "SNS", "SSS",
                'S', Material.SOUL_SAND,
                'N', Material.NETHERITE_INGOT);

        ItemStack s1 = plugin.getBossManager().createSummonItem(1);
        addShapedRecipe("recipe_summon1", s1,
                "OOO", "OEO", "OOO",
                'O', Material.OBSIDIAN,
                'E', Material.ENDER_EYE);

        ItemStack s2 = plugin.getBossManager().createSummonItem(2);
        addShapedRecipe("recipe_summon2", s2,
                "GGG", "GDG", "GGG",
                'G', Material.GOLD_BLOCK,
                'D', Material.DRAGON_BREATH);

        ItemStack s3 = plugin.getBossManager().createSummonItem(3);
        addShapedRecipe("recipe_summon3", s3,
                "PPP", "PAP", "PPP",
                'P', Material.PRISMARINE_CRYSTALS,
                'A', Material.AMETHYST_SHARD);
    }

    // ─── Helper ─────────────────────────────────────────────────────────
    private void addShapedRecipe(String keyName, ItemStack result, String r1, String r2, String r3,
                                  Object... ingredients) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(r1, r2, r3);

        for (int i = 0; i < ingredients.length - 1; i += 2) {
            char c = (char) ingredients[i];
            Material m = (Material) ingredients[i + 1];
            recipe.setIngredient(c, m);
        }
        plugin.getServer().addRecipe(recipe);
    }

    public boolean isGodArmor(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(godArmorKey, PersistentDataType.BYTE);
    }

    public boolean isPowerApple(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(powerAppleKey, PersistentDataType.BYTE);
    }

    public NamespacedKey getGodArmorKey() { return godArmorKey; }
    public NamespacedKey getPowerAppleKey() { return powerAppleKey; }
}
