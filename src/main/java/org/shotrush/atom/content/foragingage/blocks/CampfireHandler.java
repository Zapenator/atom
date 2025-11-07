package org.shotrush.atom.content.foragingage.blocks;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.items.CustomItem;
import org.shotrush.atom.core.util.ActionBarManager;
import org.shotrush.atom.core.workstations.WorkstationHandler;
import org.shotrush.atom.core.api.annotation.RegisterSystem;

@RegisterSystem(
    id = "campfire_handler",
    priority = 5,
    toggleable = true,
    description = "Handles campfire lighting mechanics"
)
public class CampfireHandler extends WorkstationHandler<CampfireHandler.LightingProgress> {
    
    private static CampfireHandler instance;
    
    public CampfireHandler(Plugin plugin) {
        super();
        instance = this;
    }
    
    @Override
    protected boolean isValidTool(ItemStack item) {
        CustomItem pebble = Atom.getInstance().getItemRegistry().getItem("pebble");
        return pebble != null && pebble.isCustomItem(item);
    }
    
    @Override
    protected Sound getStrokeSound() {
        return Sound.BLOCK_STONE_HIT;
    }
    
    @Override
    protected void spawnStrokeParticles(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        Location particleLoc = location.clone().add(0, 0.5, 0);
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, particleLoc, 3, 0.1, 0.1, 0.1, 0.01);
        
        // Occasional spark
        if (Math.random() < 0.3) {
            world.spawnParticle(Particle.LAVA, particleLoc, 1, 0.1, 0.1, 0.1, 0);
        }
    }
    
    @Override
    protected String getStatusMessage() {
        return "§7Striking flint... Creating sparks";
    }
    
    static class LightingProgress extends WorkstationHandler.WorkProgress {
        Campfire campfire;
        
        LightingProgress(long startTime, Location location, Campfire campfire) {
            super(startTime, 8 + (int)(Math.random() * 5), location); // 8-12 strikes
            this.campfire = campfire;
        }
    }
    
    public static void startLighting(Player player, Campfire campfire) {
        if (instance == null) return;
        
        if (instance.isProcessing(player)) {
            return;
        }
        
        if (!campfire.hasFuel()) {
            ActionBarManager.send(player, "§cAdd straw first!");
            return;
        }
        
        if (campfire.isLit()) {
            ActionBarManager.send(player, "§cCampfire is already lit!");
            return;
        }
        
        Location campfireLocation = campfire.getSpawnLocation();
        LightingProgress progress = new LightingProgress(System.currentTimeMillis(), campfireLocation, campfire);
        
        instance.startProcessing(player, campfireLocation, progress, () -> {
            finishLighting(player, campfire);
        }, "§7Striking flint... Creating sparks");
    }
    
    public static boolean isLighting(Player player) {
        return instance != null && instance.isProcessing(player);
    }
    
    public static void cancelLighting(Player player) {
        if (instance != null) {
            instance.cancelProcessing(player);
        }
    }
    
    private static void finishLighting(Player player, Campfire campfire) {
        // Small chance to fail
        if (Math.random() < 0.15) {
            ActionBarManager.send(player, "§cThe sparks didn't catch! Try again.");
            ActionBarManager.clearStatus(player);
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 0.8f);
            return;
        }
        
        // Success!
        campfire.light();
        ActionBarManager.send(player, "§aThe campfire ignites!");
        ActionBarManager.clearStatus(player);
        player.playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
        
        // Spawn ignition particles
        World world = campfire.getSpawnLocation().getWorld();
        if (world != null) {
            Location particleLoc = campfire.getSpawnLocation().clone().add(0, 0.5, 0);
            world.spawnParticle(Particle.FLAME, particleLoc, 20, 0.2, 0.2, 0.2, 0.05);
            world.spawnParticle(Particle.LAVA, particleLoc, 5, 0.2, 0.2, 0.2, 0);
        }
    }
}
