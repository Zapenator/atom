package org.shotrush.atom.content.mobs.ai.goals;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.content.mobs.ai.lifecycle.FamilyRelationships;
import org.shotrush.atom.content.mobs.ai.needs.NeedsManager;
import org.shotrush.atom.content.mobs.herd.DominanceRank;
import org.shotrush.atom.content.mobs.herd.Herd;
import org.shotrush.atom.content.mobs.herd.HerdManager;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ShareFoodGoal implements Goal<Mob> {
    
    private final GoalKey<Mob> key;
    private final Mob mob;
    private final Plugin plugin;
    private final HerdManager herdManager;
    private final FamilyRelationships familyRelationships;
    private final NeedsManager needsManager;
    private Animals hungryFamilyMember;
    private static final double FAMILY_RANGE = 8.0;
    private static final double HUNGER_THRESHOLD = 0.4;
    
    public ShareFoodGoal(Mob mob, Plugin plugin, HerdManager herdManager, 
                        FamilyRelationships familyRelationships, NeedsManager needsManager) {
        this.mob = mob;
        this.plugin = plugin;
        this.herdManager = herdManager;
        this.familyRelationships = familyRelationships;
        this.needsManager = needsManager;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "share_food"));
    }
    
    @Override
    public boolean shouldActivate() {
        if (!(mob instanceof Animals animal)) return false;
        
        Optional<Herd> herdOpt = herdManager.getHerd(mob.getUniqueId());
        if (herdOpt.isEmpty()) return false;
        
        Herd herd = herdOpt.get();
        DominanceRank rank = herd.getDominanceHierarchy().getRank(mob.getUniqueId());
        
        if (rank != DominanceRank.ALPHA && rank != DominanceRank.BETA) {
            return false;
        }
        
        if (!hasFood()) return false;
        
        List<Animals> familyMembers = familyRelationships.findNearbyFamilyMembers(animal, FAMILY_RANGE);
        
        for (Animals familyMember : familyMembers) {
            var needs = needsManager.getNeeds(familyMember);
            if (needs.getHungerPercent() < HUNGER_THRESHOLD) {
                hungryFamilyMember = familyMember;
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean shouldStayActive() {
        return false;
    }
    
    @Override
    public void start() {
        if (hungryFamilyMember == null || !hungryFamilyMember.isValid()) return;
        
        dropFood();
    }
    
    @Override
    public void stop() {
        hungryFamilyMember = null;
    }
    
    @Override
    public void tick() {
    }
    
    private boolean hasFood() {
        Material foodType = switch (mob.getType()) {
            case COW, SHEEP -> Material.WHEAT;
            case PIG -> Material.CARROT;
            case CHICKEN -> Material.WHEAT_SEEDS;
            case HORSE -> Material.APPLE;
            case WOLF -> Material.BONE;
            default -> null;
        };
        
        return foodType != null;
    }
    
    private void dropFood() {
        if (hungryFamilyMember == null) return;
        
        Material foodType = switch (mob.getType()) {
            case COW, SHEEP -> Material.WHEAT;
            case PIG -> Material.CARROT;
            case CHICKEN -> Material.WHEAT_SEEDS;
            case HORSE -> Material.APPLE;
            case WOLF -> Material.BONE;
            default -> Material.WHEAT;
        };
        
        Location dropLocation = hungryFamilyMember.getLocation();
        if (dropLocation == null || dropLocation.getWorld() == null) return;
        
        ItemStack foodItem = new ItemStack(foodType, 1);
        dropLocation.getWorld().dropItemNaturally(dropLocation, foodItem);
        
        dropLocation.getWorld().spawnParticle(Particle.HEART, 
            dropLocation.clone().add(0, hungryFamilyMember.getHeight(), 0), 5, 0.5, 0.5, 0.5, 0.01);
        
        dropLocation.getWorld().spawnParticle(Particle.ITEM, 
            dropLocation.clone().add(0, 0.5, 0), 10, 0.3, 0.3, 0.3, 0.05, foodItem);
        
        if (!(mob instanceof Animals animal)) return;
        var needs = needsManager.getNeeds(animal);
        needs.eat(10.0);
    }
    
    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }
    
    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }
}
