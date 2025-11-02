package org.shotrush.atom.content.mobs.herd;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.shotrush.atom.Atom;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class HerdManager {
    
    private final Atom plugin;
    private final Map<World, Map<EntityType, List<Herd>>> herdsByWorld;
    private final Map<UUID, Herd> animalToHerd;
    private static final double HERD_JOIN_RADIUS = 16.0;
    
    public HerdManager(Atom plugin) {
        this.plugin = plugin;
        this.herdsByWorld = new ConcurrentHashMap<>();
        this.animalToHerd = new ConcurrentHashMap<>();
    }
    
    public Herd getOrCreateHerd(Animals animal) {
        Herd existingHerd = animalToHerd.get(animal.getUniqueId());
        if (existingHerd != null) {
            return existingHerd;
        }
        
        World world = animal.getWorld();
        EntityType species = animal.getType();
        Location location = animal.getLocation();
        
        Herd nearbyHerd = findNearbyHerd(world, species, location);
        
        if (nearbyHerd != null) {
            joinHerd(animal, nearbyHerd);
            return nearbyHerd;
        }
        
        return createNewHerd(animal);
    }
    
    private Herd findNearbyHerd(World world, EntityType species, Location location) {
        Map<EntityType, List<Herd>> worldHerds = herdsByWorld.get(world);
        if (worldHerds == null) return null;
        
        List<Herd> speciesHerds = worldHerds.get(species);
        if (speciesHerds == null) return null;
        
        for (Herd herd : speciesHerds) {
            Animals leader = (Animals) Bukkit.getEntity(herd.leader());
            if (leader != null && leader.getLocation().distance(location) <= HERD_JOIN_RADIUS) {
                return herd;
            }
        }
        
        return null;
    }
    
    private Herd createNewHerd(Animals animal) {
        UUID herdId = UUID.randomUUID();
        Herd herd = new Herd(herdId, animal.getType(), animal.getWorld(), animal.getUniqueId());
        
        herdsByWorld
            .computeIfAbsent(animal.getWorld(), k -> new ConcurrentHashMap<>())
            .computeIfAbsent(animal.getType(), k -> new ArrayList<>())
            .add(herd);
        
        animalToHerd.put(animal.getUniqueId(), herd);
        
        plugin.getLogger().info("Created new herd " + herdId + " for " + animal.getType() + " with leader " + animal.getUniqueId());
        
        return herd;
    }
    
    private void joinHerd(Animals animal, Herd herd) {
        herd.addMember(animal.getUniqueId());
        animalToHerd.put(animal.getUniqueId(), herd);
        
        plugin.getLogger().info(animal.getType() + " " + animal.getUniqueId() + " joined herd " + herd.id());
    }
    
    public void leaveHerd(UUID animalId) {
        Herd herd = animalToHerd.remove(animalId);
        if (herd == null) return;
        
        herd.removeMember(animalId);
        
        plugin.getLogger().info("Animal " + animalId + " left herd " + herd.id());
        
        if (herd.isEmpty()) {
            removeHerd(herd);
            return;
        }
        
        if (herd.leader().equals(animalId)) {
            electNewLeader(herd);
        }
    }
    
    private void electNewLeader(Herd herd) {
        Animals newLeader = null;
        double highestScore = -1;
        
        for (UUID memberId : herd.members()) {
            Animals animal = (Animals) Bukkit.getEntity(memberId);
            if (animal == null || !animal.isValid()) continue;
            
            double health = animal.getHealth();
            double maxHealth = Objects.requireNonNull(animal.getAttribute(Attribute.MAX_HEALTH)).getValue();
            double healthRatio = health / maxHealth;
            
            long age = animal.getTicksLived();
            double score = healthRatio * 0.6 + (age / 100000.0) * 0.4;
            
            if (score > highestScore) {
                highestScore = score;
                newLeader = animal;
            }
        }
        
        if (newLeader != null) {
            herd.setLeader(newLeader.getUniqueId());
            plugin.getLogger().info("Elected new leader " + newLeader.getUniqueId() + " for herd " + herd.id());
        }
    }
    
    private void removeHerd(Herd herd) {
        Map<EntityType, List<Herd>> worldHerds = herdsByWorld.get(herd.world());
        if (worldHerds != null) {
            List<Herd> speciesHerds = worldHerds.get(herd.species());
            if (speciesHerds != null) {
                speciesHerds.remove(herd);
            }
        }
        
        plugin.getLogger().info("Removed empty herd " + herd.id());
    }
    
    public Optional<Herd> getHerd(UUID animalId) {
        return Optional.ofNullable(animalToHerd.get(animalId));
    }
    
    public HerdRole getRole(UUID animalId) {
        Herd herd = animalToHerd.get(animalId);
        if (herd == null) return HerdRole.FOLLOWER;
        return herd.leader().equals(animalId) ? HerdRole.LEADER : HerdRole.FOLLOWER;
    }
    
    public void broadcastPanic(Herd herd, Location threatLocation, long durationMs) {
        herd.setPanic(durationMs, threatLocation);
        plugin.getLogger().info("Herd " + herd.id() + " entering panic mode for " + durationMs + "ms");
    }
    
    public Location getHerdCentroid(Herd herd) {
        List<Location> locations = new ArrayList<>();
        
        for (UUID memberId : herd.members()) {
            Animals animal = (Animals) Bukkit.getEntity(memberId);
            if (animal != null && animal.isValid()) {
                locations.add(animal.getLocation());
            }
        }
        
        if (locations.isEmpty()) return null;
        
        double x = locations.stream().mapToDouble(Location::getX).average().orElse(0);
        double y = locations.stream().mapToDouble(Location::getY).average().orElse(0);
        double z = locations.stream().mapToDouble(Location::getZ).average().orElse(0);
        
        return new Location(herd.world(), x, y, z);
    }
}
