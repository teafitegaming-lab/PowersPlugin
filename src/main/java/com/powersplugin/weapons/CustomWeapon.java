package com.powersplugin.weapons;

import com.powersplugin.powers.CustomPower;
import org.bukkit.Material;

public enum CustomWeapon {

    // 1. INFERNO BLADE - Fire Lord weapon
    INFERNO_BLADE(
        "inferno_blade",
        "&c&lInferno Blade",
        CustomPower.FIRE_LORD,
        Material.NETHERITE_SWORD,
        18.0,
        "&7A blade forged in the heart of a volcano.",
        KillEffect.FIRE_EXPLOSION_RING,
        false
    ),

    // 2. STORM LANCE - Storm Bringer weapon
    STORM_LANCE(
        "storm_lance",
        "&e&lStorm Lance",
        CustomPower.STORM_BRINGER,
        Material.NETHERITE_HOE, // custom model
        20.0,
        "&7A lance crackling with eternal lightning.",
        KillEffect.LIGHTNING_STORM,
        false
    ),

    // 3. VOID SCYTHE - Void Walker weapon
    VOID_SCYTHE(
        "void_scythe",
        "&5&lVoid Scythe",
        CustomPower.VOID_WALKER,
        Material.NETHERITE_AXE,
        22.0,
        "&7A scythe that tears the fabric of reality.",
        KillEffect.VOID_IMPLOSION,
        false
    ),

    // 4. GLACIAL HAMMER - Frost Titan weapon
    GLACIAL_HAMMER(
        "glacial_hammer",
        "&b&lGlacial Hammer",
        CustomPower.FROST_TITAN,
        Material.NETHERITE_PICKAXE,
        25.0,
        "&7A hammer of pure glacial ice.",
        KillEffect.ICE_SHATTER_BURST,
        false
    ),

    // 5. NATURE'S WRATH - Nature Sage weapon
    NATURES_WRATH(
        "natures_wrath",
        "&a&lNature's Wrath",
        CustomPower.NATURE_SAGE,
        Material.NETHERITE_SHOVEL,
        15.0,
        "&7Nature strikes back against all who harm it.",
        KillEffect.LEAF_TORNADO,
        false
    ),

    // 6. SHADOW FANG - Shadow Reaper weapon
    SHADOW_FANG(
        "shadow_fang",
        "&0&l&nShadow Fang",
        CustomPower.SHADOW_REAPER,
        Material.NETHERITE_SWORD,
        30.0,
        "&7A dagger that kills from the shadows.",
        KillEffect.SHADOW_COLLAPSE,
        false
    ),

    // 7. ONE SHOT - CELESTIAL JUDGMENT (1 per server, bypasses Totem)
    CELESTIAL_JUDGMENT(
        "celestial_judgment",
        "&f&l✦ CELESTIAL JUDGMENT ✦",
        CustomPower.CELESTIAL_KNIGHT,
        Material.NETHERITE_SWORD,
        99999.0, // One-shot damage
        "&4&lInstant Kill. No totem can save you.",
        KillEffect.CELESTIAL_JUDGMENT_BEAM,
        true  // isOneShotWeapon
    );

    private final String id;
    private final String displayName;
    private final CustomPower associatedPower;
    private final Material baseMaterial;
    private final double damage;
    private final String lore;
    private final KillEffect killEffect;
    private final boolean isOneShotWeapon;

    CustomWeapon(String id, String displayName, CustomPower power, Material material,
                 double damage, String lore, KillEffect killEffect, boolean isOneShot) {
        this.id = id;
        this.displayName = displayName;
        this.associatedPower = power;
        this.baseMaterial = material;
        this.damage = damage;
        this.lore = lore;
        this.killEffect = killEffect;
        this.isOneShotWeapon = isOneShot;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public CustomPower getAssociatedPower() { return associatedPower; }
    public Material getBaseMaterial() { return baseMaterial; }
    public double getDamage() { return damage; }
    public String getLore() { return lore; }
    public KillEffect getKillEffect() { return killEffect; }
    public boolean isOneShotWeapon() { return isOneShotWeapon; }

    public enum KillEffect {
        FIRE_EXPLOSION_RING,
        LIGHTNING_STORM,
        VOID_IMPLOSION,
        ICE_SHATTER_BURST,
        LEAF_TORNADO,
        SHADOW_COLLAPSE,
        CELESTIAL_JUDGMENT_BEAM
    }
}
