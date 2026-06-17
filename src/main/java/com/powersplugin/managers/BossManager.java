package com.powersplugin.managers;

import com.powersplugin.PowersPlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BossManager {

    private final PowersPlugin plugin;
    private final NamespacedKey bossKey;
    private final NamespacedKey bossSummonItemKey;
    private BossBar bossBar;
    private UUID bossUUID = null;

    public BossManager(PowersPlugin plugin) {
        this.plugin = plugin;
        this.bossKey = new NamespacedKey(plugin, "ancient_guardian_boss");
        this.bossSummonItemKey = new NamespacedKey(plugin, "boss_summon_item");
    }

    // ─── Summon Items (crafted together to spawn boss) ───────────────────
    public ItemStack createSummonItem(int index) {
        Material[] mats = {Material.NETHERITE_INGOT, Material.NETHER_STAR, Material.DRAGON_EGG, Material.END_CRYSTAL};
        String[] names = {"&4Soul Fragment", "&5Void Essence", "&6Dragon's Tear", "&bCrystalline Core"};
        Material mat = mats[index % mats.length];

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(8000 + index);
        meta.displayName(LegacyComponentSerializer.legacyAmpersand()
                .deserialize(names[index % names.length])
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));

        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&8Boss Summon Component #" + (index + 1))
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        lore.add(LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&7Combine all 4 to summon the Ancient Guardian")
                .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        meta.lore(lore);
        meta.getPersistentDataContainer().set(bossSummonItemKey, PersistentDataType.INTEGER, index);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isBossSummonItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(bossSummonItemKey, PersistentDataType.INTEGER);
    }

    // ─── Spawn Boss ────────────────────────────────────────────────────
    public void spawnBoss(Location location) {
        if (bossUUID != null) {
            plugin.getServer().broadcast(LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&c&lThe Ancient Guardian is already alive!"));
            return;
        }

        Wither boss = (Wither) location.getWorld().spawnEntity(location, EntityType.WITHER);
        boss.setCustomName("§4§l⚔ ANCIENT GUARDIAN ⚔");
        boss.setCustomNameVisible(true);

        double maxHp = plugin.getConfig().getDouble("boss.health", 500.0);
        boss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHp);
        boss.setHealth(maxHp);

        boss.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 3));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
        boss.setInvulnerable(false);

        boss.getPersistentDataContainer().set(bossKey, PersistentDataType.BYTE, (byte) 1);
        bossUUID = boss.getUniqueId();

        // BossBar
        bossBar = plugin.getServer().createBossBar(
                "§4⚔ ANCIENT GUARDIAN ⚔", BarColor.RED, BarStyle.SEGMENTED_10);
        bossBar.setProgress(1.0);
        plugin.getServer().getOnlinePlayers().forEach(bossBar::addPlayer);
        bossBar.setVisible(true);

        // Health update task
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bossUUID == null) { cancel(); return; }
                Entity e = location.getWorld().getEntity(bossUUID);
                if (e == null || e.isDead()) { cancel(); bossBar.setVisible(false); return; }
                LivingEntity le = (LivingEntity) e;
                bossBar.setProgress(le.getHealth() / maxHp);
                bossBar.setTitle("§4⚔ ANCIENT GUARDIAN §7[" + (int) le.getHealth() + "/" + (int) maxHp + "] ⚔");

                // Particle aura
                location.getWorld().spawnParticle(Particle.SOUL, le.getLocation().add(0,1,0), 10, 1, 1, 1, 0.1);
                location.getWorld().spawnParticle(Particle.FLAME, le.getLocation().add(0,1,0), 5, 1, 1, 1, 0.05);
            }
        }.runTaskTimer(plugin, 0L, 10L);

        String msg = plugin.getConfig().getString("boss.spawn-message",
                "&c&lTHE ANCIENT GUARDIAN HAS AWOKEN!");
        plugin.getServer().broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
        location.getWorld().strikeLightning(location);
    }

    public void onBossDeath(Location loc) {
        bossUUID = null;
        if (bossBar != null) { bossBar.setVisible(false); bossBar.removeAll(); }

        // Drop Crown
        loc.getWorld().dropItemNaturally(loc, plugin.getCrownManager().createCrown());

        // Death effect
        for (int i = 0; i < 5; i++) {
            final int fi = i;
            new BukkitRunnable() {
                @Override public void run() {
                    loc.getWorld().spawnParticle(Particle.EXPLOSION, loc.clone().add(
                            (Math.random()-0.5)*4, Math.random()*3, (Math.random()-0.5)*4), 5, 0,0,0,0);
                    loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0,1,0), 30, 2,2,2,0.3);
                    loc.getWorld().strikeLightningEffect(loc);
                }
            }.runTaskLater(plugin, fi * 10L);
        }

        String msg = plugin.getConfig().getString("boss.death-message",
                "&a&lTHE ANCIENT GUARDIAN HAS BEEN DEFEATED! THE CROWN DROPS!");
        plugin.getServer().broadcast(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
    }

    public boolean isBoss(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        return entity.getPersistentDataContainer().has(bossKey, PersistentDataType.BYTE);
    }

    public NamespacedKey getBossKey() { return bossKey; }
    public NamespacedKey getBossSummonItemKey() { return bossSummonItemKey; }
    public UUID getBossUUID() { return bossUUID; }
    public BossBar getBossBar() { return bossBar; }
    public void addPlayerToBar(Player p) { if (bossBar != null) bossBar.addPlayer(p); }
}
