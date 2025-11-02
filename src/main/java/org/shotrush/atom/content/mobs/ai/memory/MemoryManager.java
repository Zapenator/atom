package org.shotrush.atom.content.mobs.ai.memory;

import org.bukkit.Location;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.shotrush.atom.Atom;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryManager {
    
    private final Atom plugin;
    private final Map<UUID, AnimalMemory> memories;
    
    public MemoryManager(Atom plugin) {
        this.plugin = plugin;
        this.memories = new ConcurrentHashMap<>();
    }
    
    public AnimalMemory getMemory(Animals animal) {
        return memories.computeIfAbsent(animal.getUniqueId(), id -> new AnimalMemory(id));
    }
    
    public void recordDanger(Animals animal, Location location, AnimalMemory.DangerType type, int severity) {
        AnimalMemory memory = getMemory(animal);
        memory.rememberDanger(location, type, severity);
    }
    
    public void recordPlayerInteraction(Animals animal, Player player, PlayerMemory.PlayerInteraction interaction) {
        AnimalMemory memory = getMemory(animal);
        memory.rememberPlayer(player, interaction);
    }
    
    public boolean isDangerous(Animals animal, Location location) {
        AnimalMemory memory = memories.get(animal.getUniqueId());
        if (memory == null) return false;
        return memory.isDangerous(location);
    }
    
    public AnimalMemory.PlayerThreatLevel getThreatLevel(Animals animal, Player player) {
        AnimalMemory memory = memories.get(animal.getUniqueId());
        if (memory == null) return AnimalMemory.PlayerThreatLevel.NEUTRAL;
        return memory.getThreatLevel(player);
    }
    
    public void removeMemory(UUID animalId) {
        memories.remove(animalId);
    }
    
    public void cleanup() {
        plugin.getLogger().info("Cleaning up animal memories...");
        memories.values().forEach(memory -> {
        });
    }
}
