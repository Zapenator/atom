package org.shotrush.atom.core.blocks;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.blocks.*;

import java.util.UUID;

/**
 * Abstract base class for all custom blocks
 */
public abstract class CustomBlock {
    protected final Location spawnLocation;
    protected final Location blockLocation;
    protected final BlockFace blockFace;
    protected UUID interactionUUID;
    protected UUID displayUUID;

    /**
     * Constructor for initial placement
     */
    public CustomBlock(Location spawnLocation, Location blockLocation, BlockFace blockFace) {
        this.spawnLocation = spawnLocation.clone();
        this.blockLocation = blockLocation.clone();
        this.blockFace = blockFace;
    }

    /**
     * Constructor for loading from file
     */
    public CustomBlock(Location spawnLocation, BlockFace blockFace) {
        this.spawnLocation = spawnLocation.clone();
        this.blockLocation = new Location(
            spawnLocation.getWorld(),
            spawnLocation.getBlockX(),
            spawnLocation.getBlockY(),
            spawnLocation.getBlockZ()
        );
        this.blockFace = blockFace;
    }

    /**
     * Spawns the block entities in the world
     */
    public abstract void spawn(Atom plugin);

    /**
     * Updates the block (called every tick if needed)
     */
    public abstract void update(float globalAngle);

    /**
     * Removes the block from the world
     */
    public abstract void remove();

    /**
     * Checks if the block's entities are still valid
     */
    public abstract boolean isValid();
    
    /**
     * Called when a player interacts with this block using a wrench
     * @param player The player interacting
     * @param sneaking Whether the player is sneaking
     * @return true if the interaction was handled
     */
    public boolean onWrenchInteract(org.bukkit.entity.Player player, boolean sneaking) {
        return false;
    }
    
    /**
     * Called after this block is placed
     */
    public void onPlaced() {
    }
    
    /**
     * Called after this block is removed
     */
    public void onRemoved() {
    }

    /**
     * Gets the block type identifier
     */
    public abstract String getBlockType();

    /**
     * Serializes the block for saving
     */
    public abstract String serialize();

    // Common getters
    public Location getLocation() {
        return spawnLocation;
    }

    public Location getBlockLocation() {
        return blockLocation;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public UUID getInteractionUUID() {
        return interactionUUID;
    }

    public UUID getDisplayUUID() {
        return displayUUID;
    }
}
