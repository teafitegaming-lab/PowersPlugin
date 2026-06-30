package com.powersplugin;

import com.powersplugin.commands.*;
import com.powersplugin.listeners.*;
import com.powersplugin.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class PowersPlugin extends JavaPlugin {

    private static PowersPlugin instance;
    private PowerManager powerManager;
    private WeaponManager weaponManager;
    private BossManager bossManager;
    private CraftingManager craftingManager;
    private ParticleManager particleManager;
    private ArmorListener armorListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.powerManager    = new PowerManager(this);
        this.weaponManager   = new WeaponManager(this);
        this.bossManager     = new BossManager(this);
        this.craftingManager = new CraftingManager(this);
        this.particleManager = new ParticleManager(this);
        this.armorListener   = new ArmorListener(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WeaponListener(this), this);
        getServer().getPluginManager().registerEvents(new BossListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftListener(this), this);
        getServer().getPluginManager().registerEvents(armorListener, this);

        getCommand("powers").setExecutor(new PowersCommand(this));
        getCommand("givecustomweapon").setExecutor(new GiveWeaponCommand(this));
        getCommand("givecustomarmor").setExecutor(new GiveArmorCommand(this));
        getCommand("spawnboss").setExecutor(new SpawnBossCommand(this));
        getCommand("resetcooldown").setExecutor(new ResetCooldownCommand(this));
        getCommand("toggleparticles").setExecutor(new ToggleParticlesCommand(this));
        getCommand("resetarmorcooldown").setExecutor(new ResetArmorCooldownCommand(armorListener));

        particleManager.startParticleTasks();

        getLogger().info("====================================");
        getLogger().info("  PowersPlugin v2.0 Enabled!");
        getLogger().info("  Celestial Judgment | God Armor");
        getLogger().info("  Sonic Boom | Resistance V");
        getLogger().info("====================================");
    }

    @Override
    public void onDisable() {
        if (particleManager != null) particleManager.stopAllTasks();
        if (armorListener  != null) armorListener.stopTasks();
    }

    public static PowersPlugin getInstance()      { return instance; }
    public PowerManager    getPowerManager()       { return powerManager; }
    public WeaponManager   getWeaponManager()      { return weaponManager; }
    public BossManager     getBossManager()        { return bossManager; }
    public CraftingManager getCraftingManager()    { return craftingManager; }
    public ParticleManager getParticleManager()    { return particleManager; }
    public ArmorListener   getArmorListener()      { return armorListener; }
}
