package com.powersplugin.weapons;

import com.powersplugin.powers.CustomPower;
import org.bukkit.Material;

public enum CustomWeapon {

    // THE ONLY CUSTOM WEAPON - One Hit Kill Sword
    // Dropped only by the Ancient Guardian boss. Cannot be crafted.
    CELESTIAL_JUDGMENT(
        "celestial_judgment",
        "&f&l✦ CELESTIAL JUDGMENT ✦",
        CustomPower.CELESTIAL_JUDGMENT_POWER,
        Material.NETHERITE_SWORD,
        99999.0, // One-shot damage
        "&4&lInstant Kill. Totems Can't Stop it.",
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
        CELESTIAL_JUDGMENT_BEAM
    }
}
