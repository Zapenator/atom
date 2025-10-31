package org.shotrush.atom.core.blocks;

import org.bukkit.NamespacedKey;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.blocks.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for all custom block types
 */
public class CustomBlockRegistry {
    private final Atom plugin;
    private final Map<String, BlockType> blockTypes;
    private final Map<NamespacedKey, String> keyToIdentifier;

    public CustomBlockRegistry(Atom plugin) {
        this.plugin = plugin;
        this.blockTypes = new HashMap<>();
        this.keyToIdentifier = new HashMap<>();
    }

    /**
     * Registers a block type
     */
    public void register(BlockType blockType) {
        String id = blockType.getIdentifier();
        blockTypes.put(id, blockType);
        
        NamespacedKey key = new NamespacedKey(plugin, id + "_item");
        keyToIdentifier.put(key, id);
        
        plugin.getLogger().info("Registered block type: " + id);
    }

    /**
     * Gets a block type by identifier
     */
    public BlockType getBlockType(String identifier) {
        return blockTypes.get(identifier);
    }

    /**
     * Gets the identifier from a namespaced key
     */
    public String getIdentifier(NamespacedKey key) {
        return keyToIdentifier.get(key);
    }

    /**
     * Gets the namespaced key for a block type
     */
    public NamespacedKey getKey(String identifier) {
        return new NamespacedKey(plugin, identifier + "_item");
    }

    /**
     * Gets all registered block types
     */
    public Map<String, BlockType> getAllBlockTypes() {
        return new HashMap<>(blockTypes);
    }
}
