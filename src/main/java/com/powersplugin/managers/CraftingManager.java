package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import com.powersplugin.weapons.CustomWeapon;
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
    private final NamespacedKey craftedWeaponKey;

    public CraftingManager(PowersPlugin plugin) {
        this.plugin = plugin;
        this.godArmorKey = new NamespacedKey(plugin, "god_armor");
        this.powerAppleKey = new NamespacedKey(plugin, "power_apple");
        this.craftedWeaponKey = new NamespacedKey(plugin, "crafted_weapon_id");
        registerAllRecipes();
    }

    private void registerAllRecipes() {
        registerWeaponRecipes();
        registerGodArmorRecipe();
        registerPowerAppleRecipe();
        registerBossSummonItemRecipes();
    }

    // ─── WEAPON RECIPES ─────────────────────────────────────────────────
    private void registerWeaponRecipes() {
        // 1. INFERNO BLADE: Netherite Sword + Blaze Rods + Magma Cream
        addShapedRecipe("recipe_inferno_blade", plugin.getWeaponManager().createWeapon(CustomWeapon.INFERNO_BLADE),
                "ABA", "BCB", "ABA",
                'A', Material.BLAZE_ROD,
                'B', Material.MAGMA_CREAM,
                'C', Material.NETHERITE_SWORD);

        // 2. STORM LANCE: Netherite Hoe + Lightning rods + Gold
        addShapedRecipe("recipe_storm_lance", plugin.getWeaponManager().createWeapon(CustomWeapon.STORM_LANCE),
                " A ", "ABA", " C ",
                'A', Material.LIGHTNING_ROD,
                'B', Material.NETHERITE_HOE,
                'C', Material.GOLD_BLOCK);

        // 3. VOID SCYTHE: Netherite Axe + Ender Pearls + Obsidian
        addShapedRecipe("recipe_void_scythe", plugin.getWeaponManager().createWeapon(CustomWeapon.VOID_SCYTHE),
                "AAB", "ACA", "BAA",
                'A', Material.ENDER_PEARL,
                'B', Material.OBSIDIAN,
                'C', Material.NETHERITE_AXE);

        // 4. GLACIAL HAMMER: Netherite Pickaxe + Ice + Diamonds
        addShapedRecipe("recipe_glacial_hammer", plugin.getWeaponManager().createWeapon(CustomWeapon.GLACIAL_HAMMER),
                "DDD", "DPD", "IAI",
                'D', Material.DIAMOND,
                'P', Material.NETHERITE_PICKAXE,
                'I', Material.BLUE_ICE,
                'A', Material.AMETHYST_SHARD);

        // 5. NATURE'S WRATH: Netherite Shovel + Jungle Logs + Emeralds
        addShapedRecipe("recipe_natures_wrath", plugin.getWeaponManager().createWeapon(CustomWeapon.NATURES_WRATH),
                "ESE", "EEE", "LLL",
                'E', Material.EMERALD,
                'S', Material.NETHERITE_SHOVEL,
                'L', Material.JUNGLE_LOG);

        // 6. SHADOW FANG: Netherite Sword + Ink Sacs + Wither Skull
        addShapedRecipe("recipe_shadow_fang", plugin.getWeaponManager().createWeapon(CustomWeapon.SHADOW_FANG),
                "IWI", "ISI", "III",
                'I', Material.INK_SAC,
                'W', Material.WITHER_SKELETON_SKULL,
                'S', Material.NETHERITE_SWORD);

        // 7. CELESTIAL JUDGMENT (Unique 1-shot): Nether Star + Netherite Sword + Totem
        addShapedRecipe("recipe_celestial_judgment", plugin.getWeaponManager().createWeapon(CustomWeapon.CELESTIAL_JUDGMENT),
                "NTN", "NSN", "NNN",
                'N', Material.NETHER_STAR,
                'T', Material.TOTEM_OF_UNDYING,
                'S', Material.NETHERITE_SWORD);
    }

    // ─── GOD ARMOR RECIPE ───────────────────────────────────────────────
    private void registerGodArmorRecipe() {
        ItemStack armor = createGodArmor();
        addShapedRecipe("recipe_god_armor",
                armor,
                "NDN", "DDD", "NDN",
                'N', Material.NETHERITE_INGOT,
                'D', Material.DIAMOND_CHESTPLATE);
    }

    public ItemStack createGodArmor() {
        ItemStack item = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(7777);
        meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&b&l✦ GOD CHESTPLATE ✦")
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7The strongest armor in existence.")
                .decoration(TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&8[Unique - Craft once per server]")
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        meta.addEnchant(Enchantment.PROTECTION, 10, true);
        meta.addEnchant(Enchantment.UNBREAKING, 10, true);
        meta.addEnchant(Enchantment.MENDING, 1, true);
        meta.addEnchant(Enchantment.THORNS, 5, true);
        meta.addEnchant(Enchantment.BLAST_PROTECTION, 10, true);
        meta.addEnchant(Enchantment.PROJECTILE_PROTECTION, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);

        meta.getPersistentDataContainer().set(godArmorKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
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
        lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize("&7Boosts all weapon powers by &e1.5x")
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
        // Soul Fragment: Netherite + Soul Sand
        ItemStack s0 = plugin.getBossManager().createSummonItem(0);
        addShapedRecipe("recipe_summon0", s0,
                "SSS", "SNS", "SSS",
                'S', Material.SOUL_SAND,
                'N', Material.NETHERITE_INGOT);

        // Void Essence: Ender Eye + Obsidian
        ItemStack s1 = plugin.getBossManager().createSummonItem(1);
        addShapedRecipe("recipe_summon1", s1,
                "OOO", "OEO", "OOO",
                'O', Material.OBSIDIAN,
                'E', Material.ENDER_EYE);

        // Dragon's Tear: Dragon Breath + Gold
        ItemStack s2 = plugin.getBossManager().createSummonItem(2);
        addShapedRecipe("recipe_summon2", s2,
                "GGG", "GDG", "GGG",
                'G', Material.GOLD_BLOCK,
                'D', Material.DRAGON_BREATH);

        // Crystalline Core: Amethyst + Prismarine
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
