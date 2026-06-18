package com.powersplugin.powers;

import org.bukkit.Color;
import org.bukkit.Particle;

public enum CustomPower {

    // 1. FIRE LORD - Fire particles bottom to top in cube form
    FIRE_LORD(
        "Fire Lord",
        "&c&lFIRE LORD",
        "&7Grants immunity to fire & explosive attacks.",
        Particle.FLAME,
        Color.ORANGE,
        PowerEffect.CUBE_BOTTOM_TO_TOP,
        0.8f
    ),

    // 2. STORM BRINGER - Lightning spiral around body
    STORM_BRINGER(
        "Storm Bringer",
        "&e&lSTORM BRINGER",
        "&7Charges attacks with lightning damage.",
        Particle.ELECTRIC_SPARK,
        Color.YELLOW,
        PowerEffect.DOUBLE_HELIX_SPIRAL,
        0.5f
    ),

    // 3. VOID WALKER - Dark void particles pulse outward in rings
    VOID_WALKER(
        "Void Walker",
        "&5&lVOID WALKER",
        "&7Phase through blocks briefly & gain speed.",
        Particle.PORTAL,
        Color.PURPLE,
        PowerEffect.EXPANDING_RINGS,
        0.6f
    ),

    // 4. FROST TITAN - Ice shards orbit like a horizontal shield
    FROST_TITAN(
        "Frost Titan",
        "&b&lFROST TITAN",
        "&7Slows all nearby enemies passively.",
        Particle.SNOWFLAKE,
        Color.AQUA,
        PowerEffect.HORIZONTAL_ORBIT,
        0.7f
    ),

    // 5. NATURE SAGE - Leaf particles form a swirling cloak
    NATURE_SAGE(
        "Nature Sage",
        "&a&lNATURE SAGE",
        "&7Regeneration aura & poison on attackers.",
        Particle.COMPOSTER,
        Color.GREEN,
        PowerEffect.SWIRLING_CLOAK,
        0.9f
    ),

    // 6. SHADOW REAPER - Dark smoke trails from feet in an X pattern
    SHADOW_REAPER(
        "Shadow Reaper",
        "&0&lSHADOW REAPER",
        "&7Grants invisibility bursts & wither aura.",
        Particle.ASH,
        Color.fromRGB(30, 0, 30),
        PowerEffect.SHADOW_X_TRAIL,
        0.4f
    ),

    // 7. CELESTIAL KNIGHT - Star/totem particles form angelic wings
    CELESTIAL_KNIGHT(
        "Celestial Knight",
        "&f&lCELESTIAL KNIGHT",
        "&7Totem effect on death & angel wings display.",
        Particle.END_ROD,
        Color.WHITE,
        PowerEffect.ANGEL_WINGS,
        1.0f
    );

    private final String id;
    private final String displayName;
    private final String description;
    private final Particle particle;
    private final Color color;
    private final PowerEffect effectType;
    private final float particleSize;

    CustomPower(String id, String displayName, String description,
                Particle particle, Color color, PowerEffect effectType, float particleSize) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.particle = particle;
        this.color = color;
        this.effectType = effectType;
        this.particleSize = particleSize;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Particle getParticle() { return particle; }
    public Color getColor() { return color; }
    public PowerEffect getEffectType() { return effectType; }
    public float getParticleSize() { return particleSize; }

    public enum PowerEffect {
        CUBE_BOTTOM_TO_TOP,
        DOUBLE_HELIX_SPIRAL,
        EXPANDING_RINGS,
        HORIZONTAL_ORBIT,
        SWIRLING_CLOAK,
        SHADOW_X_TRAIL,
        ANGEL_WINGS
    }
}
