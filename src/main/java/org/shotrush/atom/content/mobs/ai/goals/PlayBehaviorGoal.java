package org.shotrush.atom.content.mobs.ai.goals;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.shotrush.atom.content.mobs.ai.lifecycle.FamilyRelationships;
import org.shotrush.atom.content.mobs.ai.lifecycle.LifeCycleManager;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayBehaviorGoal implements Goal<Mob> {
    
    private final GoalKey<Mob> key;
    private final Mob mob;
    private final Plugin plugin;
    private final LifeCycleManager lifeCycleManager;
    private final FamilyRelationships familyRelationships;
    private Animals playmate;
    private int playTicks;
    private PlayMode currentMode;
    private static final int MAX_PLAY_DURATION = 400;
    private static final double PLAYMATE_RANGE = 10.0;
    
    public PlayBehaviorGoal(Mob mob, Plugin plugin, LifeCycleManager lifeCycleManager, 
                           FamilyRelationships familyRelationships) {
        this.mob = mob;
        this.plugin = plugin;
        this.lifeCycleManager = lifeCycleManager;
        this.familyRelationships = familyRelationships;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "play_behavior"));
        this.playTicks = 0;
        this.currentMode = PlayMode.CHASE;
    }
    
    @Override
    public boolean shouldActivate() {
        if (!(mob instanceof Animals animal)) return false;
        if (lifeCycleManager.getStage(animal) != LifeCycleManager.AgeStage.BABY) {
            return false;
        }
        
        if (Math.random() > 0.05) return false;
        
        Set<UUID> siblings = familyRelationships.getSiblings(animal);
        if (siblings.isEmpty()) return false;
        
        Location mobLoc = mob.getLocation();
        if (mobLoc == null || mobLoc.getWorld() == null) return false;
        
        for (org.bukkit.entity.Entity nearby : mobLoc.getWorld().getNearbyEntities(mobLoc, 
                PLAYMATE_RANGE, PLAYMATE_RANGE, PLAYMATE_RANGE)) {
            
            if (nearby instanceof Animals potentialPlaymate && 
                siblings.contains(potentialPlaymate.getUniqueId()) &&
                lifeCycleManager.getStage(potentialPlaymate) == LifeCycleManager.AgeStage.BABY) {
                
                playmate = potentialPlaymate;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean shouldStayActive() {
        if (!mob.isValid() || mob.isDead()) return false;
        if (mob.getTarget() != null) return false;
        if (playmate == null || !playmate.isValid()) return false;
        
        if (!(mob instanceof Animals animal)) return false;
        if (lifeCycleManager.getStage(animal) != LifeCycleManager.AgeStage.BABY) {
            return false;
        }
        
        double distance = mob.getLocation().distance(playmate.getLocation());
        if (distance > PLAYMATE_RANGE * 1.5) return false;
        
        return playTicks < MAX_PLAY_DURATION;
    }
    
    @Override
    public void start() {
        playTicks = 0;
        currentMode = Math.random() < 0.5 ? PlayMode.CHASE : PlayMode.HOP;
    }
    
    @Override
    public void stop() {
        playmate = null;
        playTicks = 0;
    }
    
    @Override
    public void tick() {
        playTicks++;
        
        if (playTicks % 100 == 0) {
            currentMode = Math.random() < 0.5 ? PlayMode.CHASE : PlayMode.HOP;
        }
        
        switch (currentMode) {
            case CHASE -> performChase();
            case HOP -> performHop();
        }
        
        if (playTicks % 20 == 0) {
            Location loc = mob.getLocation();
            if (loc != null && loc.getWorld() != null) {
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, 
                    loc.clone().add(0, mob.getHeight() / 2, 0), 3, 0.3, 0.3, 0.3, 0.01);
            }
        }
    }
    
    private void performChase() {
        if (playmate == null) return;
        
        Location playmateLocation = playmate.getLocation();
        if (playmateLocation == null) return;
        
        if (playTicks % 60 < 30) {
            mob.getPathfinder().moveTo(playmateLocation, 1.3);
        } else {
            Vector direction = mob.getLocation().toVector().subtract(playmateLocation.toVector()).normalize();
            Location runAwayLocation = mob.getLocation().add(direction.multiply(3));
            mob.getPathfinder().moveTo(runAwayLocation, 1.2);
        }
    }
    
    private void performHop() {
        if (playTicks % 30 == 0) {
            Vector velocity = mob.getVelocity();
            velocity.setY(0.4);
            mob.setVelocity(velocity);
        }
        
        if (Math.random() < 0.1) {
            Location randomNearby = mob.getLocation().clone().add(
                (Math.random() - 0.5) * 4,
                0,
                (Math.random() - 0.5) * 4
            );
            mob.getPathfinder().moveTo(randomNearby, 1.0);
        }
    }
    
    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }
    
    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.JUMP);
    }
    
    private enum PlayMode {
        CHASE,
        HOP
    }
}
