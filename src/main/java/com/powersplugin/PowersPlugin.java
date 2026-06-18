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
    private CrownManager crownManager;
    private ParticleManager particleManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize Managers
        this.powerManager = new PowerManager(this);
        this.weaponManager = new WeaponManager(this);
        this.bossManager = new BossManager(this);
        this.craftingManager = new CraftingManager(this);
        this.crownManager = new CrownManager(this);
        this.particleManager = new ParticleManager(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new WeaponListener(this), this);
        getServer().getPluginManager().registerEvents(new BossListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftListener(this), this);
        getServer().getPluginManager().registerEvents(new CrownListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorListener(this), this);

        // Register Commands
        getCommand("powers").setExecutor(new PowersCommand(this));
        getCommand("givecustomweapon").setExecutor(new GiveWeaponCommand(this));
        getCommand("givecustomarmor").setExecutor(new GiveArmorCommand(this));
        getCommand("spawnboss").setExecutor(new SpawnBossCommand(this));
        getCommand("givecrown").setExecutor(new GiveCrownCommand(this));

        // Start particle task
        particleManager.startParticleTasks();

        getLogger().info("====================================");
        getLogger().info("  PowersPlugin v1.0.0 Enabled!");
        getLogger().info("  7 Powers | 7 Weapons | God Armor");
        getLogger().info("  Boss | Crown | Power Apples");
        getLogger().info("====================================");
    }

    @Override
    public void onDisable() {
        if (particleManager != null) {
            particleManager.stopAllTasks();
        }
        getLogger().info("PowersPlugin Disabled!");
    }

    public static PowersPlugin getInstance() { return instance; }
    public PowerManager getPowerManager() { return powerManager; }
    public WeaponManager getWeaponManager() { return weaponManager; }
    public BossManager getBossManager() { return bossManager; }
    public CraftingManager getCraftingManager() { return craftingManager; }
    public CrownManager getCrownManager() { return crownManager; }
    public ParticleManager getParticleManager() { return particleManager; }
}
