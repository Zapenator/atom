package org.shotrush.atom.world;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public class WorldModificationManager implements Listener {
    private final JavaPlugin plugin;
    private final Set<Material> allowedDrops;
    private final Map<Material, List<org.bukkit.inventory.ItemStack>> customDrops;
    
    public WorldModificationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.allowedDrops = new HashSet<>();
        this.customDrops = new HashMap<>();
    }
    
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        org.shotrush.atom.Atom.getInstance().getSchedulerManager().runGlobal(() -> {
            for (World world : Bukkit.getWorlds()) {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
                world.setGameRule(GameRule.KEEP_INVENTORY, false);
                world.setGameRule(GameRule.NATURAL_REGENERATION, false);
            }
            
            removeAllRecipes();
        });
    }
    
    private void removeAllRecipes() {
        Bukkit.getServer().clearRecipes();
    }
    
    public void allowBlockDrop(Material material) {
        allowedDrops.add(material);
    }
    
    public void setCustomDrop(Material block, org.bukkit.inventory.ItemStack... drops) {
        customDrops.put(block, Arrays.asList(drops));
    }
    
    public void removeCustomDrop(Material block) {
        customDrops.remove(block);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Block block = event.getBlock();
        Material type = block.getType();
        
        if (!allowedDrops.contains(type) && !customDrops.containsKey(type)) {
            event.setDropItems(false);
            event.setExpToDrop(0);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDrop(BlockDropItemEvent event) {
        if (event.isCancelled()) return;
        
        Block block = event.getBlock();
        Material type = block.getType();
        
        if (customDrops.containsKey(type)) {
            event.getItems().clear();
            Location loc = block.getLocation().add(0.5, 0.5, 0.5);
            customDrops.get(type).forEach(item -> 
                block.getWorld().dropItemNaturally(loc, item)
            );
        } else if (!allowedDrops.contains(type)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSprint(PlayerToggleSprintEvent event) {
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        if (event.getCause() == EntityDamageEvent.DamageCause.STARVATION) {
            event.setCancelled(true);
        }
    }
    
    public void disableNaturalRegeneration() {
        Bukkit.getWorlds().forEach(world -> 
            world.setGameRule(GameRule.NATURAL_REGENERATION, false)
        );
    }
    
    public void shutdown() {
    }
}
