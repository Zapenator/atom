package org.shotrush.atom.content.mobs.ai.goals;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.content.mobs.ai.lifecycle.FamilyRelationships;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class MotherProtectionGoal implements Goal<Mob> {
    
    private final GoalKey<Mob> key;
    private final Mob mob;
    private final Plugin plugin;
    private final FamilyRelationships familyRelationships;
    private boolean enraged;
    private static final double BABY_PROTECTION_RADIUS = 12.0;
    private static final double ENRAGE_DAMAGE_MULTIPLIER = 2.0;
    private static final NamespacedKey ENRAGE_KEY = new NamespacedKey("atom", "mother_protection_enrage");
    
    public MotherProtectionGoal(Mob mob, Plugin plugin, FamilyRelationships familyRelationships) {
        this.mob = mob;
        this.plugin = plugin;
        this.familyRelationships = familyRelationships;
        this.key = GoalKey.of(Mob.class, new NamespacedKey(plugin, "mother_protection"));
        this.enraged = false;
    }
    
    @Override
    public boolean shouldActivate() {
        if (!(mob instanceof Animals mother)) return false;
        
        Set<UUID> children = familyRelationships.getChildren(mother);
        if (children.isEmpty()) return false;
        
        Location motherLoc = mob.getLocation();
        if (motherLoc == null || motherLoc.getWorld() == null) return false;
        
        for (Entity nearby : motherLoc.getWorld().getNearbyEntities(motherLoc, 
                BABY_PROTECTION_RADIUS, BABY_PROTECTION_RADIUS, BABY_PROTECTION_RADIUS)) {
            
            if (nearby instanceof Animals baby && children.contains(baby.getUniqueId())) {
                if (baby.getLastDamageCause() != null && 
                    baby.getLastDamageCause().getEntity() instanceof LivingEntity attacker &&
                    attacker.getUniqueId() != mob.getUniqueId()) {
                    
                    mob.setTarget(attacker);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean shouldStayActive() {
        if (!enraged) return false;
        
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isValid()) return false;
        
        return true;
    }
    
    @Override
    public void start() {
        enraged = true;
        applyEnrageBonus();
        
        Location mobLoc = mob.getLocation();
        if (mobLoc != null && mobLoc.getWorld() != null) {
            mobLoc.getWorld().playSound(mobLoc, Sound.ENTITY_WOLF_GROWL, 2.0f, 0.8f);
            mobLoc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, 
                mobLoc.clone().add(0, mob.getHeight(), 0), 8, 0.5, 0.5, 0.5);
        }
    }
    
    @Override
    public void stop() {
        enraged = false;
        removeEnrageBonus();
    }
    
    @Override
    public void tick() {
        if (!enraged) return;
        
        LivingEntity target = mob.getTarget();
        if (target != null && target.isValid()) {
            mob.getPathfinder().moveTo(target.getLocation(), 1.5);
        }
        
        if (mob.getTicksLived() % 40 == 0) {
            Location mobLoc = mob.getLocation();
            if (mobLoc != null && mobLoc.getWorld() != null) {
                mobLoc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, 
                    mobLoc.clone().add(0, mob.getHeight(), 0), 2);
            }
        }
    }
    
    private void applyEnrageBonus() {
        AttributeInstance attackDamage = mob.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage == null) return;
        
        double baseValue = attackDamage.getBaseValue();
        AttributeModifier modifier = new AttributeModifier(
            ENRAGE_KEY,
            baseValue * (ENRAGE_DAMAGE_MULTIPLIER - 1.0),
            AttributeModifier.Operation.ADD_NUMBER
        );
        
        attackDamage.addModifier(modifier);
    }
    
    private void removeEnrageBonus() {
        AttributeInstance attackDamage = mob.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attackDamage == null) return;
        
        attackDamage.getModifiers().stream()
            .filter(mod -> mod.getKey().equals(ENRAGE_KEY))
            .forEach(attackDamage::removeModifier);
    }
    
    @Override
    public GoalKey<Mob> getKey() {
        return key;
    }
    
    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET, GoalType.MOVE);
    }
}
