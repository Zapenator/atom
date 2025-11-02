package org.shotrush.atom.content.mobs.herd;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Animals;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public final class HerdPersistence {
    
    private final NamespacedKey herdIdKey;
    private final NamespacedKey isLeaderKey;
    private final NamespacedKey isAggressiveKey;
    private final NamespacedKey maxStaminaKey;
    private final NamespacedKey staminaKey;
    
    public HerdPersistence(Plugin plugin) {
        this.herdIdKey = new NamespacedKey(plugin, "herd_id");
        this.isLeaderKey = new NamespacedKey(plugin, "is_leader");
        this.isAggressiveKey = new NamespacedKey(plugin, "is_aggressive");
        this.maxStaminaKey = new NamespacedKey(plugin, "max_stamina");
        this.staminaKey = new NamespacedKey(plugin, "stamina");
    }
    
    public void saveHerdData(Animals animal, UUID herdId, boolean isLeader, boolean isAggressive, double maxStamina, double stamina) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        
        pdc.set(herdIdKey, PersistentDataType.STRING, herdId.toString());
        pdc.set(isLeaderKey, PersistentDataType.BYTE, (byte) (isLeader ? 1 : 0));
        pdc.set(isAggressiveKey, PersistentDataType.BYTE, (byte) (isAggressive ? 1 : 0));
        pdc.set(maxStaminaKey, PersistentDataType.DOUBLE, maxStamina);
        pdc.set(staminaKey, PersistentDataType.DOUBLE, stamina);
    }
    
    public boolean hasHerdData(Animals animal) {
        return animal.getPersistentDataContainer().has(herdIdKey, PersistentDataType.STRING);
    }
    
    public UUID getHerdId(Animals animal) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        if (!pdc.has(herdIdKey, PersistentDataType.STRING)) return null;
        
        String idString = pdc.get(herdIdKey, PersistentDataType.STRING);
        if (idString == null) return null;
        
        try {
            return UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public boolean isLeader(Animals animal) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        if (!pdc.has(isLeaderKey, PersistentDataType.BYTE)) return false;
        
        Byte value = pdc.get(isLeaderKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }
    
    public boolean isAggressive(Animals animal) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        if (!pdc.has(isAggressiveKey, PersistentDataType.BYTE)) return false;
        
        Byte value = pdc.get(isAggressiveKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }
    
    public double getMaxStamina(Animals animal, double defaultValue) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        if (!pdc.has(maxStaminaKey, PersistentDataType.DOUBLE)) return defaultValue;
        
        Double value = pdc.get(maxStaminaKey, PersistentDataType.DOUBLE);
        return value != null ? value : defaultValue;
    }
    
    public double getStamina(Animals animal, double defaultValue) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        if (!pdc.has(staminaKey, PersistentDataType.DOUBLE)) return defaultValue;
        
        Double value = pdc.get(staminaKey, PersistentDataType.DOUBLE);
        return value != null ? value : defaultValue;
    }
    
    public void updateStamina(Animals animal, double stamina) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        pdc.set(staminaKey, PersistentDataType.DOUBLE, stamina);
    }
    
    public void clearHerdData(Animals animal) {
        PersistentDataContainer pdc = animal.getPersistentDataContainer();
        pdc.remove(herdIdKey);
        pdc.remove(isLeaderKey);
        pdc.remove(isAggressiveKey);
        pdc.remove(maxStaminaKey);
        pdc.remove(staminaKey);
    }
}
