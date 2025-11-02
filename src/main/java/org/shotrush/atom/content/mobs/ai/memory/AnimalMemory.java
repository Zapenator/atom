package org.shotrush.atom.content.mobs.ai.memory;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnimalMemory {
    
    private final UUID animalId;
    private final Map<Location, DangerMemory> dangerLocations;
    private final Map<UUID, PlayerMemory> playerMemories;
    private final List<Location> safeRoutes;
    private Location lastThreatLocation;
    private long lastThreatTime;
    
    private static final long DANGER_MEMORY_DURATION_MS = 600000;
    private static final int MAX_DANGER_LOCATIONS = 10;
    private static final int MAX_PLAYER_MEMORIES = 20;
    
    public AnimalMemory(UUID animalId) {
        this.animalId = animalId;
        this.dangerLocations = new ConcurrentHashMap<>();
        this.playerMemories = new ConcurrentHashMap<>();
        this.safeRoutes = new ArrayList<>();
    }
    
    public void rememberDanger(Location location, DangerType type, int severity) {
        Location roundedLoc = roundLocation(location);
        
        DangerMemory memory = new DangerMemory(
            roundedLoc,
            type,
            severity,
            System.currentTimeMillis()
        );
        
        dangerLocations.put(roundedLoc, memory);
        
        if (dangerLocations.size() > MAX_DANGER_LOCATIONS) {
            removeOldestDangerMemory();
        }
        
        lastThreatLocation = location.clone();
        lastThreatTime = System.currentTimeMillis();
    }
    
    public void rememberPlayer(Player player, PlayerMemory.PlayerInteraction interaction) {
        UUID playerId = player.getUniqueId();
        PlayerMemory memory = playerMemories.getOrDefault(playerId, new PlayerMemory(playerId));
        
        memory.addInteraction(interaction, System.currentTimeMillis());
        playerMemories.put(playerId, memory);
        
        if (playerMemories.size() > MAX_PLAYER_MEMORIES) {
            removeOldestPlayerMemory();
        }
    }
    
    public boolean isDangerous(Location location) {
        cleanupOldMemories();
        
        Location roundedLoc = roundLocation(location);
        DangerMemory memory = dangerLocations.get(roundedLoc);
        
        if (memory == null) return false;
        
        long age = System.currentTimeMillis() - memory.timestamp();
        return age < DANGER_MEMORY_DURATION_MS;
    }
    
    public int getDangerSeverity(Location location) {
        Location roundedLoc = roundLocation(location);
        DangerMemory memory = dangerLocations.get(roundedLoc);
        
        if (memory == null) return 0;
        
        long age = System.currentTimeMillis() - memory.timestamp();
        if (age >= DANGER_MEMORY_DURATION_MS) return 0;
        
        double fadeMultiplier = 1.0 - ((double) age / DANGER_MEMORY_DURATION_MS);
        return (int) (memory.severity() * fadeMultiplier);
    }
    
    public PlayerThreatLevel getThreatLevel(Player player) {
        PlayerMemory memory = playerMemories.get(player.getUniqueId());
        if (memory == null) return PlayerThreatLevel.NEUTRAL;
        
        return memory.calculateThreatLevel();
    }
    
    public void addSafeRoute(Location waypoint) {
        safeRoutes.add(waypoint.clone());
        
        if (safeRoutes.size() > 50) {
            safeRoutes.remove(0);
        }
    }
    
    public Optional<Location> getRecentThreat() {
        if (lastThreatLocation == null) return Optional.empty();
        
        long age = System.currentTimeMillis() - lastThreatTime;
        if (age > 30000) return Optional.empty();
        
        return Optional.of(lastThreatLocation);
    }
    
    public List<Location> getNearbyDangerZones(Location center, double radius) {
        cleanupOldMemories();
        
        List<Location> nearbyDangers = new ArrayList<>();
        
        for (DangerMemory memory : dangerLocations.values()) {
            if (memory.location().distance(center) <= radius) {
                long age = System.currentTimeMillis() - memory.timestamp();
                if (age < DANGER_MEMORY_DURATION_MS) {
                    nearbyDangers.add(memory.location());
                }
            }
        }
        
        return nearbyDangers;
    }
    
    private Location roundLocation(Location loc) {
        return new Location(
            loc.getWorld(),
            Math.floor(loc.getX() / 5) * 5,
            Math.floor(loc.getY() / 5) * 5,
            Math.floor(loc.getZ() / 5) * 5
        );
    }
    
    private void cleanupOldMemories() {
        long now = System.currentTimeMillis();
        
        dangerLocations.entrySet().removeIf(entry -> 
            now - entry.getValue().timestamp() > DANGER_MEMORY_DURATION_MS
        );
        
        playerMemories.values().forEach(PlayerMemory::cleanupOldInteractions);
    }
    
    private void removeOldestDangerMemory() {
        DangerMemory oldest = null;
        Location oldestKey = null;
        
        for (Map.Entry<Location, DangerMemory> entry : dangerLocations.entrySet()) {
            if (oldest == null || entry.getValue().timestamp() < oldest.timestamp()) {
                oldest = entry.getValue();
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            dangerLocations.remove(oldestKey);
        }
    }
    
    private void removeOldestPlayerMemory() {
        PlayerMemory oldest = null;
        UUID oldestKey = null;
        
        for (Map.Entry<UUID, PlayerMemory> entry : playerMemories.entrySet()) {
            if (oldest == null || entry.getValue().getOldestInteractionTime() < oldest.getOldestInteractionTime()) {
                oldest = entry.getValue();
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            playerMemories.remove(oldestKey);
        }
    }
    
    public enum DangerType {
        ATTACKED,
        HERD_PANIC,
        PREDATOR_SIGHTING,
        LOUD_NOISE,
        FIRE
    }
    
    public enum PlayerThreatLevel {
        FRIENDLY,
        NEUTRAL,
        CAUTIOUS,
        HOSTILE,
        MORTAL_ENEMY
    }
    
    public record DangerMemory(Location location, DangerType type, int severity, long timestamp) {}
}
