package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;

public class BossListener implements Listener {

    private final PowersPlugin plugin;

    public BossListener(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBossDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (!plugin.getBossManager().isBoss(entity)) return;

        e.getDrops().clear();
        e.setDroppedExp(5000);

        plugin.getBossManager().onBossDeath(entity.getLocation());
    }

    @EventHandler
    public void onBossDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity le)) return;
        if (!plugin.getBossManager().isBoss(le)) return;

        // Boss takes 50% reduced damage from explosions
        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
            e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            e.setDamage(e.getDamage() * 0.5);
        }
    }

    @EventHandler
    public void onBossTarget(EntityTargetEvent e) {
        if (!(e.getEntity() instanceof LivingEntity le)) return;
        if (!plugin.getBossManager().isBoss(le)) return;

        // Boss always targets nearest player
        if (e.getTarget() == null && e.getEntity().getLocation().getWorld() != null) {
            org.bukkit.entity.Player nearest = null;
            double dist = Double.MAX_VALUE;
            for (org.bukkit.entity.Player p : e.getEntity().getWorld().getPlayers()) {
                double d = p.getLocation().distanceSquared(e.getEntity().getLocation());
                if (d < dist) { dist = d; nearest = p; }
            }
            if (nearest != null) e.setTarget(nearest);
        }
    }
}
