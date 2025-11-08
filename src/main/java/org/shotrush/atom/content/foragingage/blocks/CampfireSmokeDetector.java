package org.shotrush.atom.content.foragingage.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.Atom;
import org.shotrush.atom.content.systems.ItemHeatSystem;
import org.shotrush.atom.core.api.annotation.RegisterSystem;
import org.shotrush.atom.core.api.scheduler.SchedulerAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@RegisterSystem(
    id = "campfire_smoke_detector",
    priority = 10,
    toggleable = true,
    description = "Detects ground items below signal campfires"
)
public class CampfireSmokeDetector {
    
    private final Plugin plugin;
    private final NamespacedKey groundItemKey;
    private final Random random = new Random();
    
    
    private final Map<Location, SmeltingProgress> smeltingItems = new HashMap<>();
    
    
    private static final long MIN_SMELTING_TIME = 60000L;
    private static final long MAX_SMELTING_TIME = 120000L;
    
    
    private static final Map<Material, SmeltingRecipe> SMELTING_RECIPES = new HashMap<>();
    
    static {
        
        SMELTING_RECIPES.put(Material.RAW_IRON, new SmeltingRecipe(Material.IRON_INGOT, 300.0));
        SMELTING_RECIPES.put(Material.RAW_COPPER, new SmeltingRecipe(Material.COPPER_INGOT, 200.0));
        SMELTING_RECIPES.put(Material.RAW_GOLD, new SmeltingRecipe(Material.GOLD_INGOT, 400.0));
    }
    
    
    private static class SmeltingRecipe {
        final Material result;
        final double requiredTemp;
        
        SmeltingRecipe(Material result, double requiredTemp) {
            this.result = result;
            this.requiredTemp = requiredTemp;
        }
    }
    
    
    private static class SmeltingProgress {
        final long completionTime;
        final Material originalMaterial;
        final double temperature;
        
        SmeltingProgress(long completionTime, Material originalMaterial, double temperature) {
            this.completionTime = completionTime;
            this.originalMaterial = originalMaterial;
            this.temperature = temperature;
        }
    }
    
    public CampfireSmokeDetector(Plugin plugin) {
        this.plugin = plugin;
        this.groundItemKey = new NamespacedKey(plugin, "ground_item_frame");
        startDetectionTask();
    }
    
    
    private void startDetectionTask() {
        
        SchedulerAPI.runGlobalTaskTimer(this::scheduleCampfireScans, 40L, 40L);
    }
    
    
    private void scheduleCampfireScans() {
        Atom atom = Atom.getInstance();
        if (atom == null || atom.getBlockManager() == null) return;
        
        
        long litCampfires = atom.getBlockManager().getBlocks().stream()
            .filter(block -> block instanceof Campfire)
            .map(block -> (Campfire) block)
            .filter(Campfire::isLit)
            .count();
        
        if (litCampfires > 0) {
            plugin.getLogger().info("[CampfireSmokeDetector] Scanning " + litCampfires + " lit campfire(s)");
        }
        
        atom.getBlockManager().getBlocks().stream()
            .filter(block -> block instanceof Campfire)
            .map(block -> (Campfire) block)
            .filter(Campfire::isLit) 
            .forEach(campfire -> {
                Location location = campfire.getBlockLocation();
                if (location != null && location.getWorld() != null) {
                    
                    SchedulerAPI.runTask(location, () -> checkCampfireForGroundItem(campfire));
                }
            });
    }
    
    
    private void checkCampfireForGroundItem(Campfire campfire) {
        Location campfireLocation = campfire.getBlockLocation();
        if (campfireLocation == null || campfireLocation.getWorld() == null) {
            plugin.getLogger().warning("[CampfireSmokeDetector] Campfire has null location or world");
            return;
        }
        
        Block campfireBlock = campfireLocation.getBlock();
        if (campfireBlock.getType() != Material.CAMPFIRE) {
            plugin.getLogger().warning("[CampfireSmokeDetector] Block is not a campfire: " + campfireBlock.getType());
            return;
        }
        
        
        if (!(campfireBlock.getBlockData() instanceof org.bukkit.block.data.type.Campfire campfireData)) {
            plugin.getLogger().warning("[CampfireSmokeDetector] Block data is not Campfire type");
            return;
        }
        
        plugin.getLogger().info("[CampfireSmokeDetector] Checking campfire at " + formatLocation(campfireLocation));
        plugin.getLogger().info("  - Is signal fire: " + campfireData.isSignalFire());
        
        if (!campfireData.isSignalFire()) {
            plugin.getLogger().info("  - Not a signal fire, skipping");
            return;
        }
        
        
        Block firstHayBale = campfireLocation.clone().subtract(0, 1, 0).getBlock();
        Block secondHayBale = campfireLocation.clone().subtract(0, 2, 0).getBlock();
        
        plugin.getLogger().info("  - Block below (Y-1): " + firstHayBale.getType());
        plugin.getLogger().info("  - Block below (Y-2): " + secondHayBale.getType());
        
        if (firstHayBale.getType() != Material.HAY_BLOCK || secondHayBale.getType() != Material.HAY_BLOCK) {
            plugin.getLogger().info("  - Missing required 2 hay bales, skipping");
            return;
        }
        
        plugin.getLogger().info("  - Valid signal campfire! Starting ground item scan...");
        
        
        
        Location scanStart = campfireLocation.clone().subtract(0, 2, 0);
        detectGroundItemBelow(campfireLocation, scanStart, 2); 
    }
    
    
    private void detectGroundItemBelow(Location campfireLocation, Location startLocation, int initialHayBaleCount) {
        List<String> blocksInWay = new ArrayList<>();
        int hayBaleCount = initialHayBaleCount; 
        int airGapCount = 0;
        Location currentLocation = startLocation.clone();
        
        plugin.getLogger().info("  - Scanning from Y=" + startLocation.getBlockY() + " downward...");
        plugin.getLogger().info("  - Starting with " + initialHayBaleCount + " hay bales already counted");
        
        
        for (int i = 0; i < 64; i++) {
            currentLocation.subtract(0, 1, 0);
            Block block = currentLocation.getBlock();
            
            
            ItemFrame groundItem = findGroundItemAt(currentLocation);
            if (groundItem != null) {
                
                
                double temperature = calculateTemperature(hayBaleCount, airGapCount);
                applyTemperatureToGroundItem(groundItem, temperature);
                
                
                boolean smelted = trySmeltItem(groundItem, temperature);
                
                logGroundItemDetection(campfireLocation, currentLocation, groundItem, blocksInWay, 
                                      hayBaleCount, airGapCount, temperature, smelted);
                return;
            }
            
            
            if (block.getType() == Material.HAY_BLOCK) {
                hayBaleCount++;
            } else if (block.getType() == Material.AIR) {
                airGapCount++;
            }
            
            
            if (block.getType() != Material.AIR) {
                blocksInWay.add(block.getType().name() + " at Y=" + block.getY());
            }
            
            
            if (block.getType() == Material.BEDROCK || currentLocation.getY() < -64) {
                plugin.getLogger().info("  - Reached bedrock/void at Y=" + currentLocation.getBlockY() + ", stopping scan");
                break;
            }
        }
        
        plugin.getLogger().info("  - No ground item found in 64 block scan");
    }
    
    
    private ItemFrame findGroundItemAt(Location location) {
        if (location.getWorld() == null) return null;
        
        
        for (Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {
            if (entity instanceof ItemFrame frame) {
                
                if (frame.getPersistentDataContainer().has(groundItemKey, PersistentDataType.BYTE)) {
                    return frame;
                }
            }
        }
        
        return null;
    }
    
    
    private double calculateTemperature(int hayBaleCount, int airGapCount) {
        
        double baseTemp = hayBaleCount * 200.0;
        
        
        if (airGapCount > 0) {
            
            double efficiency = Math.max(0.1, 1.0 - (airGapCount * 0.1));
            baseTemp *= efficiency;
        }
        
        return baseTemp;
    }
    
    
    private void applyTemperatureToGroundItem(ItemFrame groundItem, double temperature) {
        ItemStack item = groundItem.getItem();
        if (item != null && item.getType() != Material.AIR) {
            ItemHeatSystem.setItemHeat(item, temperature);
            groundItem.setItem(item, false);
        }
    }
    
    
    private boolean trySmeltItem(ItemFrame groundItem, double temperature) {
        ItemStack item = groundItem.getItem();
        if (item == null || item.getType() == Material.AIR) {
            
            smeltingItems.remove(groundItem.getLocation());
            return false;
        }
        
        
        SmeltingRecipe recipe = SMELTING_RECIPES.get(item.getType());
        if (recipe == null) {
            smeltingItems.remove(groundItem.getLocation());
            return false;
        }
        
        
        if (temperature < recipe.requiredTemp) {
            smeltingItems.remove(groundItem.getLocation());
            return false;
        }
        
        Location itemLocation = groundItem.getLocation();
        long currentTime = System.currentTimeMillis();
        
        
        if (!smeltingItems.containsKey(itemLocation)) {
            
            long smeltingTime = MIN_SMELTING_TIME + random.nextInt((int)(MAX_SMELTING_TIME - MIN_SMELTING_TIME));
            long completionTime = currentTime + smeltingTime;
            
            smeltingItems.put(itemLocation, new SmeltingProgress(completionTime, item.getType(), temperature));
            
            plugin.getLogger().info("  - Started smelting timer: " + (smeltingTime / 1000) + " seconds");
            return false; 
        }
        
        
        SmeltingProgress progress = smeltingItems.get(itemLocation);
        
        
        if (progress.originalMaterial != item.getType()) {
            
            smeltingItems.remove(itemLocation);
            return false;
        }
        
        if (currentTime >= progress.completionTime) {
            
            ItemStack result = new ItemStack(recipe.result, item.getAmount());
            
            
            ItemHeatSystem.setItemHeat(result, temperature);
            
            
            groundItem.setItem(result, false);
            
            
            smeltingItems.remove(itemLocation);
            
            return true;
        }
        
        
        long remainingTime = (progress.completionTime - currentTime) / 1000;
        plugin.getLogger().info("  - Smelting in progress: " + remainingTime + " seconds remaining");
        return false;
    }
    
    
    private void logGroundItemDetection(Location campfireLocation, Location groundItemLocation, 
                                       ItemFrame groundItem, List<String> blocksInWay,
                                       int hayBaleCount, int airGapCount, double temperature, boolean smelted) {
        
        int distance = campfireLocation.getBlockY() - groundItemLocation.getBlockY();
        String itemName = groundItem.getItem().getType().name();
        
        plugin.getLogger().info("═══════════════════════════════════════════════");
        plugin.getLogger().info("CAMPFIRE SMOKE SIGNAL DETECTED!");
        plugin.getLogger().info("═══════════════════════════════════════════════");
        plugin.getLogger().info("Campfire Location: " + formatLocation(campfireLocation));
        plugin.getLogger().info("Ground Item Location: " + formatLocation(groundItemLocation));
        plugin.getLogger().info("Vertical Distance: " + distance + " blocks");
        plugin.getLogger().info("Ground Item: " + itemName + " (x" + groundItem.getItem().getAmount() + ")");
        plugin.getLogger().info("-----------------------------------------------");
        plugin.getLogger().info("Temperature Calculation:");
        plugin.getLogger().info("  - Hay Bales: " + hayBaleCount + " (" + (hayBaleCount * 200) + "°C base)");
        plugin.getLogger().info("  - Air Gaps: " + airGapCount + " (" + (airGapCount > 0 ? "-" + (airGapCount * 10) + "% efficiency" : "no reduction") + ")");
        plugin.getLogger().info("  - Final Temperature: " + String.format("%.1f", temperature) + "°C");
        
        if (smelted) {
            plugin.getLogger().info("  - ✓ ITEM SMELTED!");
        }
        
        plugin.getLogger().info("-----------------------------------------------");
        
        if (blocksInWay.isEmpty()) {
            plugin.getLogger().info("Blocks in Way: NONE (Clear path)");
        } else {
            plugin.getLogger().info("Blocks in Way (" + blocksInWay.size() + "):");
            for (String blockInfo : blocksInWay) {
                plugin.getLogger().info("  - " + blockInfo);
            }
        }
        
        plugin.getLogger().info("═══════════════════════════════════════════════");
    }
    
    
    private String formatLocation(Location location) {
        return String.format("(%d, %d, %d) in %s", 
            location.getBlockX(), 
            location.getBlockY(), 
            location.getBlockZ(),
            location.getWorld() != null ? location.getWorld().getName() : "unknown");
    }
}
