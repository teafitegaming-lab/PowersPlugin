package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Tracks which players currently have the one-shot sword's particle
 * effect active. This is purely tied to "is the sword in main hand right now" -
 * no persistent powers, no granted abilities, nothing else.
 */
public class PowerManager {

    private final PowersPlugin plugin;
    // Players who currently have the sword in their main hand (particles should render)
    private final Set<UUID> activeHolders = new HashSet<>();
    // Per-player animation tick, reset whenever the sword is re-equipped
    private final Map<UUID, Integer> animTick = new HashMap<>();

    public PowerManager(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    public void setHoldingSword(Player player, boolean holding) {
        UUID id = player.getUniqueId();
        if (holding) {
            if (activeHolders.add(id)) {
                animTick.put(id, 0); // reset animation when re-equipped
            }
        } else {
            activeHolders.remove(id);
            animTick.remove(id);
        }
    }

    public boolean isHoldingSword(Player player) {
        return activeHolders.contains(player.getUniqueId());
    }

    public int nextTick(Player player) {
        return animTick.merge(player.getUniqueId(), 1, Integer::sum);
    }

    public void clearPlayerData(UUID uuid) {
        activeHolders.remove(uuid);
        animTick.remove(uuid);
    }
}
