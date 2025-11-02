package org.shotrush.atom.content.mobs.ai.needs;

import org.bukkit.entity.Animals;
import org.shotrush.atom.Atom;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NeedsManager {
    
    private final Atom plugin;
    private final Map<UUID, AnimalNeeds> needs;
    
    public NeedsManager(Atom plugin) {
        this.plugin = plugin;
        this.needs = new ConcurrentHashMap<>();
        startNeedsUpdateTask();
    }
    
    public AnimalNeeds getNeeds(Animals animal) {
        return needs.computeIfAbsent(animal.getUniqueId(), id -> new AnimalNeeds());
    }
    
    public void removeNeeds(UUID animalId) {
        needs.remove(animalId);
    }
    
    private void startNeedsUpdateTask() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            for (AnimalNeeds need : needs.values()) {
                need.update();
            }
        }, 20L, 20L);
    }
    
    public void drainFromCombat(Animals animal) {
        AnimalNeeds need = getNeeds(animal);
        need.drainFromActivity(0.5, 0.3, 1.0);
    }
    
    public void drainFromFleeing(Animals animal) {
        AnimalNeeds need = getNeeds(animal);
        need.drainFromActivity(0.2, 0.4, 0.8);
    }
    
    public void drainFromChasing(Animals animal) {
        AnimalNeeds need = getNeeds(animal);
        need.drainFromActivity(0.3, 0.3, 0.6);
    }
}
