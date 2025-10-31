package org.shotrush.atom.core.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * Interface for defining custom block types
 */
public interface BlockType {
    
    /**
     * Gets the unique identifier for this block type
     */
    String getIdentifier();

    /**
     * Gets the display name
     */
    String getDisplayName();

    /**
     * Gets the base material for the item
     */
    Material getItemMaterial();

    /**
     * Gets the lore for the item
     */
    String[] getLore();

    /**
     * Creates a new block instance
     */
    CustomBlock createBlock(Location spawnLocation, Location blockLocation, BlockFace blockFace);

    /**
     * Deserializes a block from saved data
     */
    CustomBlock deserialize(String data);

    /**
     * Whether this block type needs tick updates
     */
    boolean requiresUpdate();
}
