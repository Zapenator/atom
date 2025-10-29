package org.shotrush.atom.recipe;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CustomItemManager {
    private final JavaPlugin plugin;
    @Getter private final Cache<String, CustomItem> items;
    private final NamespacedKey itemIdKey;
    
    public CustomItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.items = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();
        this.itemIdKey = new NamespacedKey(plugin, "custom_item_id");
    }
    
    public CustomItemBuilder create(String id, Material material) {
        return new CustomItemBuilder(id, material, this);
    }
    
    public void register(CustomItem item) {
        items.put(item.id(), item);
    }
    
    public Optional<CustomItem> getItem(String id) {
        return Optional.ofNullable(items.getIfPresent(id));
    }
    
    public Optional<CustomItem> getItem(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return Optional.empty();
        
        ItemMeta meta = itemStack.getItemMeta();
        String id = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        
        return id != null ? getItem(id) : Optional.empty();
    }
    
    public ItemStack createItemStack(String id) {
        return getItem(id).map(CustomItem::create).orElse(null);
    }
    
    public boolean isCustomItem(ItemStack itemStack) {
        return getItem(itemStack).isPresent();
    }
    
    public record CustomItem(
        String id,
        Material material,
        Component name,
        List<Component> lore,
        int customModelData,
        boolean unbreakable,
        Map<String, Object> customData
    ) {
        public ItemStack create() {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            
            if (name != null) {
                meta.displayName(name.decoration(TextDecoration.ITALIC, false));
            }
            
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream()
                    .map(line -> line.decoration(TextDecoration.ITALIC, false))
                    .toList());
            }
            
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            
            meta.setUnbreakable(unbreakable);
            
            NamespacedKey key = new NamespacedKey(org.shotrush.atom.Atom.getInstance(), "custom_item_id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            
            customData.forEach((k, v) -> {
                NamespacedKey dataKey = new NamespacedKey(org.shotrush.atom.Atom.getInstance(), k);
                if (v instanceof String s) {
                    meta.getPersistentDataContainer().set(dataKey, PersistentDataType.STRING, s);
                } else if (v instanceof Integer i) {
                    meta.getPersistentDataContainer().set(dataKey, PersistentDataType.INTEGER, i);
                } else if (v instanceof Double d) {
                    meta.getPersistentDataContainer().set(dataKey, PersistentDataType.DOUBLE, d);
                } else if (v instanceof Boolean b) {
                    meta.getPersistentDataContainer().set(dataKey, PersistentDataType.BOOLEAN, b);
                }
            });
            
            item.setItemMeta(meta);
            return item;
        }
    }
    
    public static class CustomItemBuilder {
        private final String id;
        private final Material material;
        private final CustomItemManager manager;
        private Component name;
        private List<Component> lore = new ArrayList<>();
        private int customModelData = 0;
        private boolean unbreakable = false;
        private Map<String, Object> customData = new HashMap<>();
        
        public CustomItemBuilder(String id, Material material, CustomItemManager manager) {
            this.id = id;
            this.material = material;
            this.manager = manager;
        }
        
        public CustomItemBuilder name(Component name) {
            this.name = name;
            return this;
        }
        
        public CustomItemBuilder lore(Component... lore) {
            this.lore = Arrays.asList(lore);
            return this;
        }
        
        public CustomItemBuilder lore(List<Component> lore) {
            this.lore = lore;
            return this;
        }
        
        public CustomItemBuilder customModelData(int data) {
            this.customModelData = data;
            return this;
        }
        
        public CustomItemBuilder unbreakable(boolean unbreakable) {
            this.unbreakable = unbreakable;
            return this;
        }
        
        public CustomItemBuilder data(String key, Object value) {
            this.customData.put(key, value);
            return this;
        }
        
        public CustomItem build() {
            CustomItem item = new CustomItem(id, material, name, lore, customModelData, unbreakable, customData);
            manager.register(item);
            return item;
        }
    }
}
