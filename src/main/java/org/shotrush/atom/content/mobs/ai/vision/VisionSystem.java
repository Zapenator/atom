package org.shotrush.atom.content.mobs.ai.vision;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class VisionSystem {
    
    private static final double VISION_ANGLE_FRONT = 170.0;
    private static final double VISION_ANGLE_SIDE = 240.0;
    private static final double VISION_ANGLE_BACK = 300.0;
    
    private static final double DETECTION_RANGE_FRONT = 24.0;
    private static final double DETECTION_RANGE_SIDE = 16.0;
    private static final double DETECTION_RANGE_BACK = 8.0;
    
    public static boolean canSee(Mob observer, Entity target) {
        Location observerLoc = observer.getEyeLocation();
        Location targetLoc = target.getLocation().add(0, target.getHeight() / 2, 0);
        
        double distance = observerLoc.distance(targetLoc);
        Vector toTarget = targetLoc.toVector().subtract(observerLoc.toVector()).normalize();
        Vector facing = observerLoc.getDirection().normalize();
        
        double angle = Math.toDegrees(facing.angle(toTarget));
        
        double maxRange = getMaxRangeForAngle(angle);
        if (distance > maxRange) {
            return false;
        }
        
        return hasLineOfSight(observerLoc, targetLoc);
    }
    
    public static boolean canSeeWithModifier(Mob observer, Entity target, double rangeMultiplier) {
        Location observerLoc = observer.getEyeLocation();
        Location targetLoc = target.getLocation().add(0, target.getHeight() / 2, 0);
        
        double distance = observerLoc.distance(targetLoc);
        Vector toTarget = targetLoc.toVector().subtract(observerLoc.toVector()).normalize();
        Vector facing = observerLoc.getDirection().normalize();
        
        double angle = Math.toDegrees(facing.angle(toTarget));
        
        double maxRange = getMaxRangeForAngle(angle) * rangeMultiplier;
        if (distance > maxRange) {
            return false;
        }
        
        return hasLineOfSight(observerLoc, targetLoc);
    }
    
    public static boolean hasLineOfSight(Location from, Location to) {
        if (from.getWorld() == null || to.getWorld() == null) return false;
        if (!from.getWorld().equals(to.getWorld())) return false;
        
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        
        RayTraceResult result = from.getWorld().rayTraceBlocks(
            from,
            direction.normalize(),
            distance,
            FluidCollisionMode.NEVER,
            true
        );
        
        return result == null || result.getHitBlock() == null;
    }
    
    public static DetectionType getDetectionType(Mob observer, Entity target) {
        Location observerLoc = observer.getEyeLocation();
        Location targetLoc = target.getLocation();
        
        Vector toTarget = targetLoc.toVector().subtract(observerLoc.toVector()).normalize();
        Vector facing = observerLoc.getDirection().normalize();
        
        double angle = Math.toDegrees(facing.angle(toTarget));
        
        if (angle <= VISION_ANGLE_FRONT / 2) {
            return DetectionType.FRONT_VISION;
        } else if (angle <= VISION_ANGLE_SIDE / 2) {
            return DetectionType.PERIPHERAL_VISION;
        } else if (angle <= VISION_ANGLE_BACK / 2) {
            return DetectionType.REAR_PERIPHERAL;
        } else {
            return DetectionType.BLIND_SPOT;
        }
    }
    
    public static double getDetectionChance(Mob observer, LivingEntity target) {
        DetectionType type = getDetectionType(observer, target);
        double distance = observer.getLocation().distance(target.getLocation());
        
        double baseChance = switch (type) {
            case FRONT_VISION -> 1.0;
            case PERIPHERAL_VISION -> 0.7;
            case REAR_PERIPHERAL -> 0.3;
            case BLIND_SPOT -> 0.0;
        };
        
        if (target.isSneaking()) {
            baseChance *= 0.3;
        }
        
        if (target instanceof Player player && player.isSprinting()) {
            baseChance *= 1.5;
        }
        
        double distancePenalty = Math.min(1.0, distance / DETECTION_RANGE_FRONT);
        baseChance *= (1.0 - distancePenalty * 0.5);
        
        return Math.max(0.0, Math.min(1.0, baseChance));
    }
    
    private static double getMaxRangeForAngle(double angle) {
        if (angle <= VISION_ANGLE_FRONT / 2) {
            return DETECTION_RANGE_FRONT;
        } else if (angle <= VISION_ANGLE_SIDE / 2) {
            return DETECTION_RANGE_SIDE;
        } else if (angle <= VISION_ANGLE_BACK / 2) {
            return DETECTION_RANGE_BACK;
        } else {
            return 0.0;
        }
    }
    
    public static boolean isInFrontArc(Mob observer, Entity target, double arcAngle) {
        Location observerLoc = observer.getEyeLocation();
        Location targetLoc = target.getLocation();
        
        Vector toTarget = targetLoc.toVector().subtract(observerLoc.toVector()).normalize();
        Vector facing = observerLoc.getDirection().normalize();
        
        double angle = Math.toDegrees(facing.angle(toTarget));
        return angle <= arcAngle / 2;
    }
    
    public static boolean isInBehindArc(Mob observer, Entity target, double arcAngle) {
        Location observerLoc = observer.getEyeLocation();
        Location targetLoc = target.getLocation();
        
        Vector toTarget = targetLoc.toVector().subtract(observerLoc.toVector()).normalize();
        Vector facing = observerLoc.getDirection().normalize();
        
        double angle = Math.toDegrees(facing.angle(toTarget));
        return angle >= (180.0 - arcAngle / 2);
    }
    
    public enum DetectionType {
        FRONT_VISION,
        PERIPHERAL_VISION,
        REAR_PERIPHERAL,
        BLIND_SPOT
    }
}
