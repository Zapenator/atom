package org.shotrush.atom.content.mobs.ai.goals;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.content.mobs.AnimalDomestication;
import org.shotrush.atom.content.mobs.ai.config.SpeciesBehavior;

import java.util.EnumSet;

public class ChaseAndMeleeAttackGoal implements Goal<Mob> {
    
    private final GoalKey<Mob> key;
    private final Mob mob;
    private final SpeciesBehavior behavior;
    private int attackCooldown;
    private static final int ATTACK_INTERVAL = 20;
    private static final double ATTACK_RANGE = 2.0;
    
    public ChaseAndMeleeAttackGoal(Mob mob, Plugin plugin, SpeciesBehavior behavior) {
        this.mob = mob;
        this.behavior = behavior;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "chase_melee_attack"));
        this.attackCooldown = 0;
    }
    
    @Override
    public boolean shouldActivate() {
        return mob.getTarget() != null && mob.getTarget().isValid();
    }
    
    @Override
    public boolean shouldStayActive() {
        if (mob.getTarget() == null || !mob.getTarget().isValid()) return false;
        
        if (mob.hasMetadata("fleeing") && mob.getMetadata("fleeing").get(0).asBoolean()) {
            return false;
        }
        
        double distance = mob.getLocation().distance(mob.getTarget().getLocation());
        return distance < behavior.aggroRadius() * 2.5;
    }
    
    @Override
    public void start() {
        attackCooldown = 0;
    }
    
    @Override
    public void stop() {
    }
    
    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isValid()) return;
        
        Location current = mob.getLocation();
        if (current == null || current.getWorld() == null) return;
        
        Location targetLoc = target.getLocation();
        if (targetLoc == null) return;
        
        double distance = current.distance(targetLoc);
        
        if (distance > ATTACK_RANGE) {
            double domesticationFactor = AnimalDomestication.getDomesticationFactor((Animals) mob);
            double speed = behavior.getChaseSpeed(domesticationFactor);
            mob.getPathfinder().moveTo(targetLoc, speed);
        } else {
            mob.getPathfinder().stopPathfinding();
            
            if (attackCooldown <= 0) {
                performAttack(target);
                attackCooldown = ATTACK_INTERVAL;
            }
        }
        
        if (attackCooldown > 0) {
            attackCooldown--;
        }
    }
    
    private void performAttack(LivingEntity target) {
        mob.lookAt(target);
        mob.attack(target);
        
        mob.swingMainHand();
    }
    
    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }
    
    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }
}
