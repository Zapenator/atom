package org.shotrush.atom.content.mobs.ai.memory;

import java.util.*;

public class PlayerMemory {
    
    private final UUID playerId;
    private final List<InteractionRecord> interactions;
    private static final long INTERACTION_MEMORY_DURATION_MS = 1200000;
    private static final int MAX_INTERACTIONS = 50;
    
    public PlayerMemory(UUID playerId) {
        this.playerId = playerId;
        this.interactions = new ArrayList<>();
    }
    
    public void addInteraction(PlayerInteraction type, long timestamp) {
        interactions.add(new InteractionRecord(type, timestamp));
        
        if (interactions.size() > MAX_INTERACTIONS) {
            interactions.remove(0);
        }
    }
    
    public AnimalMemory.PlayerThreatLevel calculateThreatLevel() {
        cleanupOldInteractions();
        
        if (interactions.isEmpty()) {
            return AnimalMemory.PlayerThreatLevel.NEUTRAL;
        }
        
        int hostileScore = 0;
        int friendlyScore = 0;
        
        for (InteractionRecord record : interactions) {
            switch (record.type()) {
                case ATTACKED:
                    hostileScore += 10;
                    break;
                case KILLED_HERD_MEMBER:
                    hostileScore += 20;
                    break;
                case CHASED:
                    hostileScore += 3;
                    break;
                case FED:
                    friendlyScore += 8;
                    break;
                case BRED:
                    friendlyScore += 15;
                    break;
                case HEALED:
                    friendlyScore += 12;
                    break;
                case NEARBY_PEACEFUL:
                    friendlyScore += 1;
                    break;
            }
        }
        
        int netScore = friendlyScore - hostileScore;
        
        if (netScore > 30) return AnimalMemory.PlayerThreatLevel.FRIENDLY;
        if (netScore > 10) return AnimalMemory.PlayerThreatLevel.NEUTRAL;
        if (netScore > -10) return AnimalMemory.PlayerThreatLevel.CAUTIOUS;
        if (netScore > -30) return AnimalMemory.PlayerThreatLevel.HOSTILE;
        return AnimalMemory.PlayerThreatLevel.MORTAL_ENEMY;
    }
    
    public void cleanupOldInteractions() {
        long now = System.currentTimeMillis();
        interactions.removeIf(record -> 
            now - record.timestamp() > INTERACTION_MEMORY_DURATION_MS
        );
    }
    
    public long getOldestInteractionTime() {
        if (interactions.isEmpty()) return System.currentTimeMillis();
        return interactions.get(0).timestamp();
    }
    
    public int getRecentAttackCount(long withinMs) {
        long cutoff = System.currentTimeMillis() - withinMs;
        return (int) interactions.stream()
            .filter(r -> r.timestamp() > cutoff)
            .filter(r -> r.type() == PlayerInteraction.ATTACKED || r.type() == PlayerInteraction.KILLED_HERD_MEMBER)
            .count();
    }
    
    public enum PlayerInteraction {
        ATTACKED,
        KILLED_HERD_MEMBER,
        CHASED,
        FED,
        BRED,
        HEALED,
        NEARBY_PEACEFUL
    }
    
    public record InteractionRecord(PlayerInteraction type, long timestamp) {}
}
