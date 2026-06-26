package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import com.powersplugin.weapons.CustomWeapon;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class WeaponListener implements Listener {

    private final PowersPlugin plugin;

    public WeaponListener(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Weapon Damage ────────────────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;

        ItemStack hand = attacker.getInventory().getItemInMainHand();
        CustomWeapon weapon = plugin.getWeaponManager().getCustomWeapon(hand);
        if (weapon == null) return;

        if (plugin.getWeaponManager().isOnCooldown(attacker)) {
            e.setCancelled(true);
            attacker.sendMessage("§c§l[Celestial Judgment] §7On cooldown for §e"
                    + plugin.getWeaponManager().formatRemainingCooldown(attacker));
            return;
        }

        if (isBlockingWithShield(victim)) {
            e.setCancelled(true);
            attacker.sendMessage("§c§l[Celestial Judgment] §7Your attack was blocked by a shield!");
            if (victim instanceof Player p) {
                p.sendMessage("§a§lYou blocked the Celestial Judgment with your shield!");
            }
            plugin.getWeaponManager().applyCooldown(attacker);
            playShieldBlockEffect(victim);
            return;
        }

        e.setCancelled(true);
        plugin.getWeaponManager().applyCooldown(attacker);
        handleOneShotKill(attacker, victim);
    }

    private boolean isBlockingWithShield(LivingEntity entity) {
        if (!(entity instanceof Player player)) return false;
        if (!player.isBlocking()) return false;
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        return main.getType() == Material.SHIELD || off.getType() == Material.SHIELD;
    }

    private void playShieldBlockEffect(LivingEntity victim) {
        Location loc = victim.getLocation().add(0, 1, 0);
        loc.getWorld().spawnParticle(Particle.CRIT, loc, 20, 0.3, 0.3, 0.3, 0.2);
        loc.getWorld().playSound(loc, Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
    }

    private void handleOneShotKill(Player attacker, LivingEntity victim) {
        if (victim instanceof Player target) {
            target.setHealth(0);
        } else {
            victim.setHealth(0);
        }

        Location loc = victim.getLocation().add(0, 1, 0);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 80, 1.5, 1.5, 1.5, 0.5);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 60, 1, 1, 1, 0.3);
        for (int i = 0; i < 5; i++) {
            loc.getWorld().strikeLightningEffect(loc.clone().add(
                    (Math.random() - 0.5) * 3, 0, (Math.random() - 0.5) * 3));
        }
        attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1f, 1.5f);

        if (victim instanceof Player t) {
            t.sendMessage("§4§l☠ CELESTIAL JUDGMENT struck you down. ☠");
        }
        attacker.sendMessage("§f§l✦ Celestial Judgment delivered! §7Cooldown: 12h");
    }

    // ─── Kill Animation ───────────────────────────────────────────────
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead.getKiller() == null) return;
        Player killer = dead.getKiller();

        ItemStack hand = killer.getInventory().getItemInMainHand();
        CustomWeapon weapon = plugin.getWeaponManager().getCustomWeapon(hand);
        if (weapon == null) return;

        plugin.getParticleManager().playKillAnimation(killer, dead);
        killer.sendTitle("§c§lCELESTIAL JUDGMENT", "§7Kill confirmed", 5, 30, 10);
    }

    // ─── BUG FIX: Q press se drop — same slot se drop karne pe bhi particles band ho ──
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (!plugin.getWeaponManager().isCustomWeapon(e.getItemDrop().getItemStack())) return;

        // Turant OFF karo — Q press ke waqt sword haath se nikal chuka hota hai
        plugin.getPowerManager().setHoldingSword(player, false);

        // 1 tick baad bhi confirm karo (inventory click drop ke liye)
        new BukkitRunnable() {
            @Override public void run() {
                ItemStack main = player.getInventory().getItemInMainHand();
                plugin.getPowerManager().setHoldingSword(player,
                        plugin.getWeaponManager().isCustomWeapon(main));
            }
        }.runTaskLater(plugin, 1L);
    }

    // ─── Slot change tracking ─────────────────────────────────────────
    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack newItem = player.getInventory().getItem(e.getNewSlot());
        plugin.getPowerManager().setHoldingSword(player,
                plugin.getWeaponManager().isCustomWeapon(newItem));
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        plugin.getPowerManager().setHoldingSword(player,
                plugin.getWeaponManager().isCustomWeapon(e.getOffHandItem()));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        new BukkitRunnable() {
            @Override public void run() {
                ItemStack main = player.getInventory().getItemInMainHand();
                plugin.getPowerManager().setHoldingSword(player,
                        plugin.getWeaponManager().isCustomWeapon(main));
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        plugin.getPowerManager().setHoldingSword(player,
                plugin.getWeaponManager().isCustomWeapon(main));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        plugin.getPowerManager().clearPlayerData(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        plugin.getPowerManager().setHoldingSword(e.getEntity(), false);
    }
}
