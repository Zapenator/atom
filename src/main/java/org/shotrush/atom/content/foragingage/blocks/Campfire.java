package org.shotrush.atom.content.foragingage.blocks;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.RegionAccessor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.blocks.CustomBlock;
import org.shotrush.atom.core.blocks.annotation.AutoRegister;
import org.shotrush.atom.core.items.CustomItem;
import org.shotrush.atom.core.util.ActionBarManager;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@AutoRegister(priority = 30)
public class Campfire extends CustomBlock {
    
    
    private static final Duration FUEL_BURN_DURATION = Duration.ofMinutes(2);
    private static final String LIGHTING_ITEM = "pebble";
    private static final String FUEL_ITEM = "straw";
    private static final double LIGHTING_FAILURE_CHANCE = 0.15;
    private static final int MAX_FUEL_SLOTS = 4; 
    
    
    private final List<FuelItem> fuelInventory = new ArrayList<>();
    
    @Getter
    private boolean isLit;

    
        private record FuelItem(Instant addedTime) {

        boolean isBurnedOut() {
                return Duration.between(addedTime, Instant.now()).compareTo(FUEL_BURN_DURATION) > 0;
            }
        }
    
    public Campfire(Location spawnLocation, Location blockLocation, BlockFace blockFace) {
        super(spawnLocation, blockLocation, blockFace);
        this.isLit = false;
    }
    
    public Campfire(Location spawnLocation, BlockFace blockFace) {
        super(spawnLocation, blockFace);
        this.isLit = false;
    }

    
    public void light() {
        if (hasFuel()) return;
        this.isLit = true;
        updateBlockState(true);
        updateVisualFuel(); 
    }
    
    
    private void extinguish() {
        this.isLit = false;
        updateBlockState(false);
    }
    
    
    public boolean addFuel() {
        if (fuelInventory.size() >= MAX_FUEL_SLOTS) {
            return false; 
        }
        fuelInventory.add(new FuelItem(Instant.now()));
        updateVisualFuel();
        return true;
    }
    
    
    public boolean hasFuel() {
        cleanupBurnedFuel();
        return fuelInventory.isEmpty();
    }
    
    
    private void cleanupBurnedFuel() {
        boolean removed = false;
        Iterator<FuelItem> iterator = fuelInventory.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isBurnedOut()) {
                iterator.remove();
                removed = true;
            }
        }
        if (removed) {
            updateVisualFuel();
        }
    }
    
    
    public int getFuelCount() {
        cleanupBurnedFuel();
        return fuelInventory.size();
    }
    
    
    private void updateBlockState(boolean lit) {
        if (blockLocation == null) return;
        
        Block block = blockLocation.getBlock();
        if (block.getType() != Material.CAMPFIRE) return;
        
        if (block.getBlockData() instanceof org.bukkit.block.data.type.Campfire campfireData) {
            campfireData.setLit(lit);
            
            
            Block blockBelow = blockLocation.clone().subtract(0, 1, 0).getBlock();
            boolean hasHayBale = blockBelow.getType() == Material.HAY_BLOCK;
            campfireData.setSignalFire(hasHayBale);
            
            block.setBlockData(campfireData, false);
        }
    }
    
    
    private void updateVisualFuel() {
        if (blockLocation == null) return;
        
        Block block = blockLocation.getBlock();
        if (block.getType() != Material.CAMPFIRE) return;
        
        if (block.getState() instanceof org.bukkit.block.Campfire campfireState) {
            
            CustomItem strawItem = Atom.getInstance().getItemRegistry().getItem(FUEL_ITEM);
            ItemStack strawStack = strawItem != null ? strawItem.create() : new ItemStack(Material.WHEAT);
            
            
            for (int i = 0; i < MAX_FUEL_SLOTS; i++) {
                if (i < fuelInventory.size()) {
                    
                    campfireState.setItem(i, strawStack);
                    campfireState.stopCooking(i); 
                } else {
                    
                    campfireState.setItem(i, null);
                }
            }
            
            campfireState.update(true, false);
        }
    }
    
    @Override
    public boolean onWrenchInteract(Player player, boolean sneaking) {
        return false; 
    }
    
    @Override
    public boolean onInteract(Player player, boolean sneaking) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) return false;
        
        CustomItem customItem = Atom.getInstance().getItemRegistry().getCustomItem(heldItem);
        if (customItem == null) return false;
        
        
        if (customItem.getIdentifier().equals(FUEL_ITEM)) {
            if (addFuel()) {
                player.swingMainHand();
                heldItem.setAmount(heldItem.getAmount() - 1);
                ActionBarManager.send(player, "§aAdded straw to campfire (" + getFuelCount() + "/" + MAX_FUEL_SLOTS + " fuel)");
            } else {
                ActionBarManager.send(player, "§cCampfire is full!");
            }
            return false; 
        }
        
        
        if (customItem.getIdentifier().equals(LIGHTING_ITEM)) {
            if (isLit()) {
                ActionBarManager.send(player, "§cCampfire is already lit!");
                return false; 
            }
            
            if (hasFuel()) {
                ActionBarManager.send(player, "§cAdd straw first!");
                return false; 
            }
            
            if (!CampfireHandler.isLighting(player)) {
                CampfireHandler.startLighting(player, this);
            }
            return false; 
        }

        return false;
    }
    
    @Override
    public void spawn(Atom plugin, RegionAccessor accessor) {
        cleanupExistingEntities();
        if (blockLocation == null) return;
        
        Block block = blockLocation.getBlock();
        block.setType(Material.CAMPFIRE, false);
        
        if (block.getBlockData() instanceof org.bukkit.block.data.type.Campfire campfireData) {
            campfireData.setLit(isLit());
            
            
            Block blockBelow = blockLocation.clone().subtract(0, 1, 0).getBlock();
            boolean hasHayBale = blockBelow.getType() == Material.HAY_BLOCK;
            campfireData.setSignalFire(hasHayBale);
            
            campfireData.setWaterlogged(false);
            block.setBlockData(campfireData, false);
        }
        
        
        updateVisualFuel();
    }
    
    @Override
    protected void cleanupExistingEntities() {
        super.cleanupExistingEntities();
        if (blockLocation != null) {
            Block block = blockLocation.getBlock();
            if (block.getType() == Material.CAMPFIRE) {
                block.setType(Material.AIR, false);
            }
        }
    }
    
    @Override
    public void update(float globalAngle) {
        
        if (isLit && hasFuel()) {
            extinguish();
        }
    }
    
    @Override
    public String getIdentifier() {
        return "campfire";
    }
    
    @Override
    public String getDisplayName() {
        return "Campfire";
    }
    
    @Override
    public Material getItemMaterial() {
        return Material.CAMPFIRE;
    }
    
    @Override
    public String[] getLore() {
        long minutes = FUEL_BURN_DURATION.toMinutes();
        return new String[]{
            "§7A campfire for warmth and cooking",
            "§7Each straw burns for " + minutes + " minute" + (minutes != 1 ? "s" : ""),
            "§7Add straw, then light with " + LIGHTING_ITEM
        };
    }
    
    @Override
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(isLit);
        
        
        for (FuelItem fuel : fuelInventory) {
            sb.append(",").append(fuel.addedTime.toEpochMilli());
        }
        
        return sb.toString();
    }
    
    @Override
    public CustomBlock deserialize(String data) {
        if (data == null || data.isEmpty()) return this;
        
        String[] parts = data.split(",");
        if (parts.length >= 1) {
            this.isLit = Boolean.parseBoolean(parts[0]);
        }
        
        
        fuelInventory.clear();
        for (int i = 1; i < parts.length; i++) {
            try {
                long timestamp = Long.parseLong(parts[i]);
                fuelInventory.add(new FuelItem(Instant.ofEpochMilli(timestamp)));
            } catch (NumberFormatException e) {
                
            }
        }
        
        if (spawnLocation.getWorld() != null) {
            spawn(Atom.getInstance());
        }
        return this;
    }
    
    
    public static double getLightingFailureChance() {
        return LIGHTING_FAILURE_CHANCE;
    }
    
}

