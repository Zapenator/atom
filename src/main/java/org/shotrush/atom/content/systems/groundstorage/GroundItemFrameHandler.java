package org.shotrush.atom.content.systems.groundstorage;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.core.api.annotation.RegisterSystem;

/**
 * Allows placing items on the ground as invisible item frames.
 * Shift + right-click on top of solid, non-interactable blocks to place.
 */
@RegisterSystem(
    id = "ground_item_frame_handler",
    priority = 15,
    description = "Allows placing items on the ground using invisible item frames",
    toggleable = true,
    enabledByDefault = true
)
public class GroundItemFrameHandler implements Listener {

    private final NamespacedKey groundItemKey;

    public GroundItemFrameHandler(Plugin plugin) {
        this.groundItemKey = new NamespacedKey(plugin, "ground_item_frame");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click on blocks
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;
        
        // Only allow placement on top face
        if (event.getBlockFace() != BlockFace.UP) return;
        
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.AIR) return;
        
        // Block must be solid and non-interactable
        Material blockType = block.getType();
        if (!blockType.isSolid()) return;
        if (blockType.isInteractable()) return;
        
        // Cancel the event to prevent other interactions
        event.setCancelled(true);
        
        // Create item frame location (1 block above, centered)
        org.bukkit.Location loc = block.getLocation().add(0.5, 1.0, 0.5);
        
        // Clone the item to preserve all metadata
        ItemStack itemToPlace = hand.clone();
        itemToPlace.setAmount(1);
        
        // Spawn the item frame
        ItemFrame frame = block.getWorld().spawn(loc, ItemFrame.class);
        frame.setFacingDirection(BlockFace.UP);
        frame.setVisible(false);
        frame.setFixed(false);
        frame.setItem(itemToPlace, false);
        frame.getPersistentDataContainer().set(groundItemKey, PersistentDataType.BYTE, (byte) 1);
        
        // Remove one item from player's hand
        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFrameBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        if (!frame.getPersistentDataContainer().has(groundItemKey, PersistentDataType.BYTE)) return;
        
        // Drop the item with all metadata
        ItemStack item = frame.getItem();
        if (item != null && item.getType() != Material.AIR) {
            frame.getWorld().dropItemNaturally(frame.getLocation(), item);
        }
        
        // Don't drop the frame itself - just remove it
        event.setCancelled(true);
        frame.remove();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onFrameDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        if (!frame.getPersistentDataContainer().has(groundItemKey, PersistentDataType.BYTE)) return;
        
        // Drop the item with all metadata
        ItemStack item = frame.getItem();
        if (item != null && item.getType() != Material.AIR) {
            frame.getWorld().dropItemNaturally(frame.getLocation(), item);
        }
        
        // Cancel damage and remove frame
        event.setCancelled(true);
        frame.remove();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onFrameInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame frame)) return;
        if (!frame.getPersistentDataContainer().has(groundItemKey, PersistentDataType.BYTE)) return;
        
        // Prevent rotation and item swapping
        event.setCancelled(true);
        
        // Pick up the item
        ItemStack item = frame.getItem();
        if (item != null && item.getType() != Material.AIR) {
            event.getPlayer().getInventory().addItem(item);
            frame.remove();
        }
    }
}
