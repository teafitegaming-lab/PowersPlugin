package com.powersplugin.powers;

import org.bukkit.Color;
import org.bukkit.Particle;

public enum CustomPower {

    // ONE-SHOT SWORD POWER - Fire particles, cube form, feet -> head -> feet (looping)
    CELESTIAL_JUDGMENT_POWER(
        "Celestial Judgment",
        "&c&lCELESTIAL JUDGMENT",
        "&7One hit kill. No shield can save you.",
        Particle.FLAME,
        Color.ORANGE,
        PowerEffect.CUBE_LOOP_FEET_TO_HEAD,
        0.8f
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
        CUBE_LOOP_FEET_TO_HEAD
    }
}
