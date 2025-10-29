package org.shotrush.atom.recipe;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CustomRecipeManager implements Listener {
    private final JavaPlugin plugin;
    @Getter private final Cache<String, CustomRecipe> recipes;
    
    public CustomRecipeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.recipes = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();
    }
    
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public ShapedRecipeBuilder shapedRecipe(String id, ItemStack result) {
        return new ShapedRecipeBuilder(id, result, this);
    }
    
    public ShapelessRecipeBuilder shapelessRecipe(String id, ItemStack result) {
        return new ShapelessRecipeBuilder(id, result, this);
    }
    
    public void register(CustomRecipe recipe) {
        recipes.put(recipe.id(), recipe);
        Bukkit.addRecipe(recipe.bukkitRecipe());
    }
    
    public void unregister(String id) {
        CustomRecipe recipe = recipes.getIfPresent(id);
        if (recipe != null) {
            Bukkit.removeRecipe(recipe.key());
            recipes.invalidate(id);
        }
    }
    
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe instanceof Keyed keyed) {
            String id = keyed.getKey().getKey();
            CustomRecipe custom = recipes.getIfPresent(id);
            if (custom != null && custom.onCraft() != null) {
                custom.onCraft().accept(event);
            }
        }
    }
    
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe instanceof Keyed keyed) {
            String id = keyed.getKey().getKey();
            CustomRecipe custom = recipes.getIfPresent(id);
            if (custom != null && custom.onPrepare() != null) {
                custom.onPrepare().accept(event);
            }
        }
    }
    
    public void shutdown() {
        recipes.asMap().keySet().forEach(this::unregister);
    }
    
    public record CustomRecipe(
        String id,
        NamespacedKey key,
        Recipe bukkitRecipe,
        java.util.function.Consumer<CraftItemEvent> onCraft,
        java.util.function.Consumer<PrepareItemCraftEvent> onPrepare
    ) {}
    
    public static class ShapedRecipeBuilder {
        private final String id;
        private final ItemStack result;
        private final CustomRecipeManager manager;
        private final Map<Character, RecipeChoice> ingredients = new HashMap<>();
        private String[] shape;
        private java.util.function.Consumer<CraftItemEvent> onCraft;
        private java.util.function.Consumer<PrepareItemCraftEvent> onPrepare;
        
        public ShapedRecipeBuilder(String id, ItemStack result, CustomRecipeManager manager) {
            this.id = id;
            this.result = result;
            this.manager = manager;
        }
        
        public ShapedRecipeBuilder shape(String... shape) {
            this.shape = shape;
            return this;
        }
        
        public ShapedRecipeBuilder ingredient(char key, Material material) {
            ingredients.put(key, new RecipeChoice.MaterialChoice(material));
            return this;
        }
        
        public ShapedRecipeBuilder ingredient(char key, ItemStack item) {
            ingredients.put(key, new RecipeChoice.ExactChoice(item));
            return this;
        }
        
        public ShapedRecipeBuilder ingredient(char key, RecipeChoice choice) {
            ingredients.put(key, choice);
            return this;
        }
        
        public ShapedRecipeBuilder onCraft(java.util.function.Consumer<CraftItemEvent> callback) {
            this.onCraft = callback;
            return this;
        }
        
        public ShapedRecipeBuilder onPrepare(java.util.function.Consumer<PrepareItemCraftEvent> callback) {
            this.onPrepare = callback;
            return this;
        }
        
        public CustomRecipe build() {
            NamespacedKey key = new NamespacedKey(manager.plugin, id);
            ShapedRecipe recipe = new ShapedRecipe(key, result);
            recipe.shape(shape);
            ingredients.forEach(recipe::setIngredient);
            
            CustomRecipe custom = new CustomRecipe(id, key, recipe, onCraft, onPrepare);
            manager.register(custom);
            return custom;
        }
    }
    
    public static class ShapelessRecipeBuilder {
        private final String id;
        private final ItemStack result;
        private final CustomRecipeManager manager;
        private final List<RecipeChoice> ingredients = new ArrayList<>();
        private java.util.function.Consumer<CraftItemEvent> onCraft;
        private java.util.function.Consumer<PrepareItemCraftEvent> onPrepare;
        
        public ShapelessRecipeBuilder(String id, ItemStack result, CustomRecipeManager manager) {
            this.id = id;
            this.result = result;
            this.manager = manager;
        }
        
        public ShapelessRecipeBuilder ingredient(Material material) {
            ingredients.add(new RecipeChoice.MaterialChoice(material));
            return this;
        }
        
        public ShapelessRecipeBuilder ingredient(ItemStack item) {
            ingredients.add(new RecipeChoice.ExactChoice(item));
            return this;
        }
        
        public ShapelessRecipeBuilder ingredient(RecipeChoice choice) {
            ingredients.add(choice);
            return this;
        }
        
        public ShapelessRecipeBuilder onCraft(java.util.function.Consumer<CraftItemEvent> callback) {
            this.onCraft = callback;
            return this;
        }
        
        public ShapelessRecipeBuilder onPrepare(java.util.function.Consumer<PrepareItemCraftEvent> callback) {
            this.onPrepare = callback;
            return this;
        }
        
        public CustomRecipe build() {
            NamespacedKey key = new NamespacedKey(manager.plugin, id);
            ShapelessRecipe recipe = new ShapelessRecipe(key, result);
            ingredients.forEach(recipe::addIngredient);
            
            CustomRecipe custom = new CustomRecipe(id, key, recipe, onCraft, onPrepare);
            manager.register(custom);
            return custom;
        }
    }
}
