package org.shotrush.atom.content.foragingage.workstations.knappingstation;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.api.item.ItemQualityAPI;
import org.shotrush.atom.core.items.CustomItem;
import org.shotrush.atom.core.items.ItemQuality;
import org.shotrush.atom.core.util.ActionBarManager;
import org.shotrush.atom.core.workstations.WorkstationHandler;
import org.shotrush.atom.core.api.annotation.RegisterSystem;

@RegisterSystem(
    id = "knapping_handler",
    priority = 5,
    toggleable = true,
    description = "Handles flint knapping and pressure flaking mechanics"
)
public class KnappingHandler extends WorkstationHandler<KnappingHandler.KnappingProgress> {
    
    private static KnappingHandler instance;
    
    public KnappingHandler(Plugin plugin) {
        super();
        instance = this;
    }
    
    @Override
    protected boolean isValidTool(ItemStack item) {
        CustomItem pebble = Atom.getInstance().getItemRegistry().getItem("pebble");
        CustomItem pressureFlaker = Atom.getInstance().getItemRegistry().getItem("pressure_flaker");
        
        boolean isPebble = pebble != null && pebble.isCustomItem(item);
        boolean isPressureFlaker = pressureFlaker != null && pressureFlaker.isCustomItem(item);
        
        return isPebble || isPressureFlaker;
    }
    
    @Override
    protected Sound getStrokeSound() {
        return Sound.BLOCK_STONE_HIT;
    }
    
    @Override
    protected void spawnStrokeParticles(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        Location particleLoc = location.clone().add(0, 1, 0);
        Particle.DustOptions dustOptions = new Particle.DustOptions(
            org.bukkit.Color.fromRGB(128, 128, 128), 
            1.0f
        );
        world.spawnParticle(Particle.DUST, particleLoc, 10, 0.2, 0.2, 0.2, 0, dustOptions);
    }
    
    @Override
    protected String getStatusMessage() {
        return "§7Knapping... Strike the flint";
    }
    
    static class KnappingProgress extends WorkstationHandler.WorkProgress {
        boolean isPressureFlaking;
        ItemStack inputFlint;
        
        KnappingProgress(long startTime, Location location, boolean isPressureFlaking, ItemStack inputFlint) {
            super(startTime, isPressureFlaking ? 25 + (int)(Math.random() * 11) : 15 + (int)(Math.random() * 11), location);
            this.isPressureFlaking = isPressureFlaking;
            this.inputFlint = inputFlint;
        }
    }
    
    public static void startKnapping(Player player, Location dropLocation, Runnable onComplete) {
        if (instance == null) return;
        
        if (instance.isProcessing(player)) {
            return;
        }
        
        KnappingProgress progress = new KnappingProgress(System.currentTimeMillis(), dropLocation, false, null);
        
        instance.startProcessing(player, dropLocation, progress, () -> {
            finishKnapping(player, dropLocation, onComplete);
        }, "§7Knapping... Strike the flint");
    }
    
    public static void startPressureFlaking(Player player, Location dropLocation, ItemStack inputFlint, Runnable onComplete) {
        if (instance == null) return;
        
        if (instance.isProcessing(player)) {
            return;
        }
        
        KnappingProgress progress = new KnappingProgress(System.currentTimeMillis(), dropLocation, true, inputFlint);
        
        instance.startProcessing(player, dropLocation, progress, () -> {
            finishPressureFlaking(player, dropLocation, inputFlint, onComplete);
        }, "§7Pressure flaking... Carefully work the flint");
    }
    
    public static boolean isKnapping(Player player) {
        return instance != null && instance.isProcessing(player);
    }
    
    public static void cancelKnapping(Player player) {
        if (instance != null) {
            instance.cancelProcessing(player);
        }
    }
    
    private static void finishKnapping(Player player, Location dropLocation, Runnable onComplete) {
        int knapCount = org.shotrush.atom.core.api.player.PlayerDataAPI.getInt(player, "knapping.count", 0);
        
        double failChance = Math.max(0.1, 0.5 - (knapCount * 0.02));
        
        if (Math.random() < failChance) {
            ActionBarManager.send(player, "§cThe flint broke!");
            ActionBarManager.clearStatus(player);
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
            onComplete.run();
        } else {
            ActionBarManager.send(player, "§aSuccessfully sharpened the flint!");
            ActionBarManager.clearStatus(player);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
            onComplete.run();
            
            ItemStack sharpenedFlint = Atom.getInstance().getItemRegistry().createItem("sharpened_flint");
            if (sharpenedFlint != null) {
                double temperature = org.shotrush.atom.content.systems.PlayerTemperatureSystem
                    .getInstance().getPlayerTemperature(player);
                
                ItemQuality quality = ItemQuality.fromTemperature(temperature);
                ItemQualityAPI.setQuality(sharpenedFlint, quality);
                
                player.getWorld().dropItemNaturally(dropLocation, sharpenedFlint);
            }
        }
        
        org.shotrush.atom.core.api.player.PlayerDataAPI.incrementInt(player, "knapping.count", 0);
    }
    
    private static void finishPressureFlaking(Player player, Location dropLocation, ItemStack inputFlint, Runnable onComplete) {
        int knapCount = org.shotrush.atom.core.api.player.PlayerDataAPI.getInt(player, "pressure_flaking.count", 0);
        
        double failChance = Math.max(0.2, 0.7 - (knapCount * 0.03));
        
        double itemHeat = org.shotrush.atom.content.systems.ItemHeatSystem.getItemHeat(inputFlint);
        boolean hasHeatBonus = itemHeat >= 50 && itemHeat <= 150;
        if (hasHeatBonus) {
            failChance *= 0.8;
        }
        
        if (Math.random() < failChance) {
            ActionBarManager.send(player, "§cThe flint shattered during pressure flaking!");
            ActionBarManager.clearStatus(player);
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.8f);
            onComplete.run();
        } else {
            ActionBarManager.send(player, "§aSuccessfully created high quality sharpened flint!");
            ActionBarManager.clearStatus(player);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.8f);
            onComplete.run();
            
            ItemStack sharpenedFlint = Atom.getInstance().getItemRegistry().createItem("sharpened_flint");
            if (sharpenedFlint != null) {
                ItemQualityAPI.setQuality(sharpenedFlint, ItemQuality.HIGH);
                
                if (hasHeatBonus) {
                    ActionBarManager.send(player, "§6The heat treatment improved the quality!", 4);
                }
                
                player.getWorld().dropItemNaturally(dropLocation, sharpenedFlint);
            }
        }
        
        org.shotrush.atom.core.api.player.PlayerDataAPI.incrementInt(player, "pressure_flaking.count", 0);
    }
}
