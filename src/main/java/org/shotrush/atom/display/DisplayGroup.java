package org.shotrush.atom.display;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Transformation;
import java.util.*;

@Getter
public class DisplayGroup {
    private final UUID id;
    private final Location origin;
    private final List<Display> displays;
    private Display rootEntity;
    private Interaction hitbox;
    private final List<Interaction> partHitboxes;
    private final List<org.bukkit.block.Block> barrierBlocks;
    private final List<org.bukkit.entity.Shulker> collisionShulkers;
    private float currentYaw = 0f;
    private org.bukkit.scheduler.BukkitTask animationTask;
    
    public DisplayGroup(Location origin) {
        this.id = UUID.randomUUID();
        this.origin = origin;
        this.displays = new ArrayList<>();
        this.partHitboxes = new ArrayList<>();
        this.barrierBlocks = new ArrayList<>();
        this.collisionShulkers = new ArrayList<>();
    }
    
    public void addDisplay(Display display) {
        displays.add(display);
        if (rootEntity != null) {
            rootEntity.addPassenger(display);
        }
    }
    
    public void setRoot(Display root) {
        this.rootEntity = root;
        displays.forEach(root::addPassenger);
    }
    
    public void teleport(Location location) {
        if (rootEntity != null) {
            rootEntity.teleport(location);
        } else {
            Location offset = location.clone().subtract(origin);
            displays.forEach(d -> d.teleport(d.getLocation().add(offset)));
        }
    }
    
    public void remove() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        displays.forEach(Entity::remove);
        if (rootEntity != null) {
            rootEntity.remove();
        }
        displays.clear();
    }
    
    public void rotate(float yawDelta) {
        if (rootEntity != null) {
            currentYaw += yawDelta;
            
            org.joml.Quaternionf rotation = new org.joml.Quaternionf().rotateY((float) Math.toRadians(yawDelta));
            
            for (Display display : displays) {
                Transformation current = display.getTransformation();
                
                org.joml.Vector3f translation = new org.joml.Vector3f(current.getTranslation());
                org.joml.Vector3f rotatedTranslation = rotation.transform(translation);
                
                org.joml.Quaternionf newLeftRotation = new org.joml.Quaternionf(rotation).mul(current.getLeftRotation());
                
                Transformation newTransform = new Transformation(
                    rotatedTranslation,
                    newLeftRotation,
                    current.getScale(),
                    current.getRightRotation()
                );
                
                display.setTransformation(newTransform);
                display.setInterpolationDuration(2);
            }
            
            rootEntity.setRotation(currentYaw, rootEntity.getLocation().getPitch());
        }
    }
    
    public void startAnimation(float degreesPerSecond) {
        if (animationTask != null) {
            animationTask.cancel();
        }
        
        long intervalTicks = 2;
        float degreesPerTick = degreesPerSecond / 20.0f * intervalTicks;
        
        animationTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(
            org.shotrush.atom.Atom.getInstance(),
            () -> rotate(degreesPerTick),
            intervalTicks,
            intervalTicks
        );
    }
    
    public void stopAnimation() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
    }
    
    public void addPartHitbox(Display display, float width, float height) {
        if (display != null && rootEntity != null && rootEntity.getLocation() != null) {
            Transformation transform = display.getTransformation();
            org.joml.Vector3f translation = transform.getTranslation();
            
            Location hitboxLoc = rootEntity.getLocation().clone();
            hitboxLoc.add(translation.x, translation.y, translation.z);
            
            hitboxLoc.setX(Math.floor(hitboxLoc.getX()) + 0.5);
            hitboxLoc.setY(Math.floor(hitboxLoc.getY()) + 0.5);
            hitboxLoc.setZ(Math.floor(hitboxLoc.getZ()) + 0.5);
            
            Interaction partHitbox = display.getWorld().spawn(hitboxLoc, Interaction.class, interaction -> {
                interaction.setInteractionWidth(width);
                interaction.setInteractionHeight(height);
                interaction.setResponsive(false);
            });
            partHitboxes.add(partHitbox);
        }
    }
    
    public void scaleAll(float x, float y, float z, int durationTicks) {
        displays.forEach(d -> {
            var manager = org.shotrush.atom.Atom.getInstance().getDisplayManager();
            manager.scale(d, x, y, z, durationTicks);
        });
    }
}
