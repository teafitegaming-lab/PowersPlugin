package com.powersplugin.listeners;

import com.powersplugin.PowersPlugin;
import com.powersplugin.powers.CustomPower;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    private final PowersPlugin plugin;

    public PlayerListener(PowersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        // Add player to boss bar if boss is alive
        plugin.getBossManager().addPlayerToBar(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        // Data is kept in memory; powers persist if player rejoins
    }

    // ─── Power Apple Consumption ───────────────────────────────────────
    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (!plugin.getCraftingManager().isPowerApple(item)) return;

        // Check count limit (max 2 per server)
        int crafted = plugin.getConfig().getInt("power-apple.crafted-count", 0);
        if (crafted <= 0) {
            e.setCancelled(true);
            player.sendMessage("§c[Powers] No Power Apples have been crafted yet!");
            return;
        }

        plugin.getPowerManager().applyPowerAppleBoost(player);
    }

    // ─── Storm Bringer: Lightning on jump attack ───────────────────────
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        // VOID WALKER: Shift+right-click = brief phase (speed + resistance for 3s)
        if (plugin.getPowerManager().hasPower(player, CustomPower.VOID_WALKER)) {
            if (e.getAction().name().contains("RIGHT") && player.isSneaking()) {
                if (!player.hasCooldown(Material.ENDER_PEARL)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 3));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 2));
                    player.setCooldown(Material.ENDER_PEARL, 200);
                    player.getWorld().spawnParticle(Particle.PORTAL,
                            player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.3);
                    player.sendMessage("§5[Void Walker] §7Phase Dash activated!");
                }
            }
        }
    }

    // ─── Shadow Reaper: Invisibility burst on sneak ───────────────────
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (!e.isSneaking()) return;

        if (plugin.getPowerManager().hasPower(player, CustomPower.SHADOW_REAPER)) {
            if (!player.hasCooldown(Material.COAL)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0));
                player.setCooldown(Material.COAL, 100); // 5 sec cooldown
                player.getWorld().spawnParticle(Particle.ASH,
                        player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.2);
                player.sendMessage("§0[Shadow Reaper] §7Shadow cloak activated!");
            }
        }
    }

    // ─── Storm Bringer: Lightning strike with high fall ────────────────
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        // FIRE LORD: immunity message on fire
        if (player.getFireTicks() > 0 && plugin.getPowerManager().hasPower(player, CustomPower.FIRE_LORD)) {
            player.setFireTicks(0);
        }
    }

    // ─── Crown pickup tracking ─────────────────────────────────────────
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        ItemStack item = e.getItem().getItemStack();

        if (plugin.getCrownManager().isCrown(item)) {
            new BukkitRunnable() {
                @Override public void run() {
                    // Check if player now has crown in inventory
                    for (ItemStack inv : player.getInventory().getContents()) {
                        if (plugin.getCrownManager().isCrown(inv)) {
                            plugin.getCrownManager().setCrownHolder(player);
                            break;
                        }
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    // ─── Crown equip as helmet ─────────────────────────────────────────
    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        // If they move crown to helmet slot, trigger
        new BukkitRunnable() {
            @Override public void run() {
                ItemStack helmet = player.getInventory().getHelmet();
                if (plugin.getCrownManager().isCrown(helmet)) {
                    if (!plugin.getCrownManager().hasCrown(player)) {
                        plugin.getCrownManager().setCrownHolder(player);
                    }
                }
            }
        }.runTaskLater(plugin, 1L);
    }
}
