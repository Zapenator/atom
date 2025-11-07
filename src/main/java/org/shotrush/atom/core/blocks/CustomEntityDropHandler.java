package org.shotrush.atom.core.blocks;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.shotrush.atom.Atom;
import org.shotrush.atom.content.foragingage.items.BoneItem;
import org.shotrush.atom.content.foragingage.items.LeatherItem;
import org.shotrush.atom.core.blocks.annotation.CustomEntityDrops;

import java.util.*;

import org.shotrush.atom.core.api.annotation.RegisterSystem;


@RegisterSystem(
    id = "custom_entity_drop_handler",
    priority = 8,
    toggleable = false,
    description = "Handles custom entity drops from @CustomEntityDrops annotation"
)
public class CustomEntityDropHandler implements Listener {
    
    private static final Map<EntityType, List<DropConfigWithAge>> customDrops = new HashMap<>();
    private static final Random random = new Random();
    
    public CustomEntityDropHandler(Plugin plugin) {
        scanForCustomDrops();
    }
    
    
    private void scanForCustomDrops() {
        try {
            Reflections reflections = new Reflections("org.shotrush.atom");
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(CustomEntityDrops.class);
            
            for (Class<?> clazz : annotatedClasses) {
                CustomEntityDrops annotation = clazz.getAnnotation(CustomEntityDrops.class);
                if (annotation != null) {
                    registerDrops(annotation);
                }
            }
            
            Atom.getInstance().getLogger().info("Loaded custom drops for " + customDrops.size() + " entity types");
        } catch (Exception e) {
            Atom.getInstance().getLogger().warning("Failed to scan for custom entity drops: " + e.getMessage());
        }
    }
    
    
    private void registerDrops(CustomEntityDrops annotation) {
        Set<String> allowedAges = new HashSet<>(Arrays.asList(annotation.ages()));
        Set<EntityType> targetEntities = new HashSet<>();
        
        // Add specific entities
        targetEntities.addAll(Arrays.asList(annotation.entities()));
        
        // Add entities from categories
        for (CustomEntityDrops.EntityCategory category : annotation.categories()) {
            targetEntities.addAll(getEntitiesFromCategory(category));
        }
        
        // Register drops for all target entities
        for (EntityType entityType : targetEntities) {
            List<DropConfigWithAge> drops = customDrops.computeIfAbsent(entityType, k -> new ArrayList<>());
            
            for (CustomEntityDrops.Drop drop : annotation.drops()) {
                drops.add(new DropConfigWithAge(
                    drop.customItemId(),
                    drop.minAmount(),
                    drop.maxAmount(),
                    drop.randomAmount(),
                    annotation.replaceVanillaDrops(),
                    allowedAges
                ));
            }
            
            String ageInfo = allowedAges.isEmpty() ? "all ages" : "ages: " + String.join(", ", allowedAges);
            Atom.getInstance().getLogger().info("Registered " + annotation.drops().length + 
                " custom drops for " + entityType.name() + " (" + ageInfo + ")");
        }
    }
    
    private Set<EntityType> getEntitiesFromCategory(CustomEntityDrops.EntityCategory category) {
        Set<EntityType> entities = new HashSet<>();
        
        for (EntityType type : EntityType.values()) {
            if (!type.isSpawnable() || !type.isAlive()) continue;
            
            Class<? extends org.bukkit.entity.Entity> entityClass = type.getEntityClass();
            if (entityClass == null) continue;
            
            boolean matches = switch (category) {
                case ANIMALS -> Animals.class.isAssignableFrom(entityClass);
                case MONSTERS -> Monster.class.isAssignableFrom(entityClass);
                case WATER_MOBS -> WaterMob.class.isAssignableFrom(entityClass);
                case AMBIENT -> Ambient.class.isAssignableFrom(entityClass);
            };
            
            if (matches) {
                entities.add(type);
            }
        }
        
        Atom.getInstance().getLogger().info("Category " + category + " matched " + entities.size() + " entity types");
        return entities;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();
        
        if (!customDrops.containsKey(entityType)) {
            return;
        }
        
        List<DropConfigWithAge> drops = customDrops.get(entityType);
        
        // Get current age
        String currentAge = Atom.getInstance().getAgeManager().getCurrentAge().getId();
        
        // Filter drops by age
        List<DropConfigWithAge> applicableDrops = new ArrayList<>();
        boolean shouldReplaceVanilla = false;
        
        for (DropConfigWithAge drop : drops) {
            if (drop.allowedAges.isEmpty() || drop.allowedAges.contains(currentAge)) {
                applicableDrops.add(drop);
                if (drop.replaceVanillaDrops) {
                    shouldReplaceVanilla = true;
                }
            }
        }
        
        if (applicableDrops.isEmpty()) {
            return;
        }
        
        // Replace vanilla drops if configured
        if (shouldReplaceVanilla) {
            event.getDrops().clear();
            event.setDroppedExp(random.nextInt(3) + 1);
        }
        
        // Add custom drops
        String animalName = getAnimalDisplayName(entityType);
        
        for (DropConfigWithAge drop : applicableDrops) {
            int amount = drop.minAmount;
            
            if (drop.randomAmount && drop.maxAmount > drop.minAmount) {
                amount = drop.minAmount + random.nextInt(drop.maxAmount - drop.minAmount + 1);
            }
            
            if (amount > 0) {
                ItemStack itemToDrop = Atom.getInstance().getItemRegistry().createItem(drop.customItemId);
                
                if (itemToDrop != null) {
                    itemToDrop.setAmount(amount);
                    
                    // Add animal source metadata for bone and leather
                    ItemMeta meta = itemToDrop.getItemMeta();
                    if (meta != null) {
                        if (drop.customItemId.equals("bone")) {
                            BoneItem.setAnimalSource(meta, animalName);
                            itemToDrop.setItemMeta(meta);
                        } else if (drop.customItemId.equals("uncured_leather")) {
                            LeatherItem.setAnimalSource(meta, animalName);
                            itemToDrop.setItemMeta(meta);
                        }
                    }
                    
                    event.getDrops().add(itemToDrop);
                } else {
                    Atom.getInstance().getLogger().warning("Custom item not found: " + drop.customItemId);
                }
            }
        }
    }
    
    private String getAnimalDisplayName(EntityType type) {
        return switch (type) {
            case COW -> "Cow";
            case PIG -> "Pig";
            case SHEEP -> "Sheep";
            case CHICKEN -> "Chicken";
            case RABBIT -> "Rabbit";
            case HORSE -> "Horse";
            case DONKEY -> "Donkey";
            case MULE -> "Mule";
            case LLAMA -> "Llama";
            case GOAT -> "Goat";
            case CAT -> "Cat";
            case WOLF -> "Wolf";
            case FOX -> "Fox";
            case PANDA -> "Panda";
            case POLAR_BEAR -> "Polar Bear";
            case OCELOT -> "Ocelot";
            case CAMEL -> "Camel";
            default -> type.name();
        };
    }
    
    
    private static class DropConfigWithAge {
        final String customItemId;
        final int minAmount;
        final int maxAmount;
        final boolean randomAmount;
        final boolean replaceVanillaDrops;
        final Set<String> allowedAges;
        
        DropConfigWithAge(String customItemId, int minAmount, int maxAmount, boolean randomAmount, 
                         boolean replaceVanillaDrops, Set<String> allowedAges) {
            this.customItemId = customItemId;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.randomAmount = randomAmount;
            this.replaceVanillaDrops = replaceVanillaDrops;
            this.allowedAges = allowedAges;
        }
    }
}
