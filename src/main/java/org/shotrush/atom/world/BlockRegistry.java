package org.shotrush.atom.world;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockRegistry implements Listener {
    private final JavaPlugin plugin;
    private final Map<Material, BlockBehavior> behaviors;
    private final Set<Material> disabledBlocks;
    
    public BlockRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.behaviors = new HashMap<>();
        this.disabledBlocks = new HashSet<>();
    }
    
    public void registerBehavior(Material material, BlockBehavior behavior) {
        behaviors.put(material, behavior);
    }
    
    public void disableBlock(Material material) {
        disabledBlocks.add(material);
    }
    
    public void enableBlock(Material material) {
        disabledBlocks.remove(material);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Material type = event.getBlock().getType();
        
        if (disabledBlocks.contains(type)) {
            event.setCancelled(true);
            return;
        }
        
        BlockBehavior behavior = behaviors.get(type);
        if (behavior != null && behavior.onPlace != null) {
            behavior.onPlace.accept(event);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        
        Block block = event.getClickedBlock();
        Material type = block.getType();
        
        BlockBehavior behavior = behaviors.get(type);
        if (behavior != null && behavior.onInteract != null) {
            behavior.onInteract.accept(event.getPlayer(), block);
        }
    }
    
    public static class BlockBehavior {
        private Consumer<BlockPlaceEvent> onPlace;
        private BiConsumer<org.bukkit.entity.Player, Block> onInteract;
        private Consumer<org.bukkit.event.block.BlockBreakEvent> onBreak;
        
        public BlockBehavior onPlace(Consumer<BlockPlaceEvent> handler) {
            this.onPlace = handler;
            return this;
        }
        
        public BlockBehavior onInteract(BiConsumer<org.bukkit.entity.Player, Block> handler) {
            this.onInteract = handler;
            return this;
        }
        
        public BlockBehavior onBreak(Consumer<org.bukkit.event.block.BlockBreakEvent> handler) {
            this.onBreak = handler;
            return this;
        }
    }
}
