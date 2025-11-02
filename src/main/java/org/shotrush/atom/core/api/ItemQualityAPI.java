package org.shotrush.atom.core.api;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.shotrush.atom.core.items.ItemQuality;

import java.util.ArrayList;
import java.util.List;

public class ItemQualityAPI {
    
    private static final NamespacedKey QUALITY_KEY = new NamespacedKey("atom", "item_quality");
    
    public static ItemQuality getQuality(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        String qualityName = container.get(QUALITY_KEY, PersistentDataType.STRING);
        if (qualityName == null) return null;
        
        try {
            return ItemQuality.valueOf(qualityName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static void setQuality(ItemStack item, ItemQuality quality) {
        if (item == null || quality == null) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(QUALITY_KEY, PersistentDataType.STRING, quality.name());
        
        updateLore(meta, quality);
        item.setItemMeta(meta);
    }
    
    public static boolean hasQuality(ItemStack item) {
        return getQuality(item) != null;
    }
    
    public static void removeQuality(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.remove(QUALITY_KEY);
        
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.removeIf(line -> line.contains("Quality"));
        meta.setLore(lore);
        
        item.setItemMeta(meta);
    }
    
    private static void updateLore(ItemMeta meta, ItemQuality quality) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        
        lore.removeIf(line -> line.contains("Quality"));
        
        lore.add("");
        lore.add(quality.getDisplayName());
        
        meta.setLore(lore);
    }
    
    public static double getEfficiencyMultiplier(ItemStack item) {
        ItemQuality quality = getQuality(item);
        return quality != null ? quality.getEfficiencyMultiplier() : 1.0;
    }
    
    public static double getDurabilityMultiplier(ItemStack item) {
        ItemQuality quality = getQuality(item);
        return quality != null ? quality.getDurabilityMultiplier() : 1.0;
    }
    
    public static double getProcessingCostMultiplier(ItemStack item) {
        ItemQuality quality = getQuality(item);
        return quality != null ? quality.getProcessingCostMultiplier() : 1.0;
    }
}
