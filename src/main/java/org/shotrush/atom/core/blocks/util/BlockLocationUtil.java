package org.shotrush.atom.core.blocks.util;

import org.bukkit.Location;
import org.shotrush.atom.core.blocks.CustomBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for block location operations
 */
public class BlockLocationUtil {
    
    /**
     * Gets all 6 adjacent locations (cardinal directions)
     */
    public static Location[] getAdjacentLocations(Location loc) {
        return new Location[] {
            loc.clone().add(1, 0, 0),   // East
            loc.clone().add(-1, 0, 0),  // West
            loc.clone().add(0, 1, 0),   // Up
            loc.clone().add(0, -1, 0),  // Down
            loc.clone().add(0, 0, 1),   // South
            loc.clone().add(0, 0, -1)   // North
        };
    }
    
    /**
     * Gets all 26 adjacent locations (including diagonals)
     */
    public static Location[] getAllAdjacentLocations(Location loc) {
        List<Location> locations = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    locations.add(loc.clone().add(x, y, z));
                }
            }
        }
        return locations.toArray(new Location[0]);
    }
    
    /**
     * Checks if two locations are at the same block position
     */
    public static boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        if (!loc1.getWorld().equals(loc2.getWorld())) return false;
        
        return loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }
    
    /**
     * Finds all blocks of a specific type adjacent to a location
     */
    public static <T extends CustomBlock> List<T> getAdjacentBlocks(
            Location location, 
            List<CustomBlock> allBlocks, 
            Class<T> blockType) {
        
        List<T> adjacent = new ArrayList<>();
        Location[] adjacentLocs = getAdjacentLocations(location);
        
        for (Location adjLoc : adjacentLocs) {
            for (CustomBlock block : allBlocks) {
                if (blockType.isInstance(block) && 
                    isSameBlock(block.getBlockLocation(), adjLoc)) {
                    adjacent.add(blockType.cast(block));
                    break;
                }
            }
        }
        
        return adjacent;
    }
    
    /**
     * Gets the Manhattan distance between two block locations
     */
    public static int getManhattanDistance(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return Integer.MAX_VALUE;
        }
        
        return Math.abs(loc1.getBlockX() - loc2.getBlockX()) +
               Math.abs(loc1.getBlockY() - loc2.getBlockY()) +
               Math.abs(loc1.getBlockZ() - loc2.getBlockZ());
    }
    
    /**
     * Checks if two locations are adjacent (distance of 1)
     */
    public static boolean isAdjacent(Location loc1, Location loc2) {
        return getManhattanDistance(loc1, loc2) == 1;
    }
}
