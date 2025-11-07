package org.shotrush.atom.core.recipe;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.Atom;

import java.util.*;

import org.shotrush.atom.core.api.annotation.RegisterSystem;


@RegisterSystem(
    id = "recipe_unlock_handler",
    priority = 5,
    toggleable = true,
    description = "Auto-discovers recipes when players obtain ingredients"
)
public class RecipeUnlockHandler implements Listener {
    
    private static final Map<NamespacedKey, List<RecipeChoice>> recipeIngredients = new HashMap<>();
    
    public RecipeUnlockHandler(Plugin plugin) {
        // Extract recipe ingredients after server startup
        org.shotrush.atom.core.api.scheduler.SchedulerAPI.runGlobalTaskLater(this::extractRecipeIngredients, 40L);
    }
    
    
    private void extractRecipeIngredients() {
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            
            
            if (!(recipe instanceof Keyed keyedRecipe)) {
                continue;
            }
            
            NamespacedKey key = keyedRecipe.getKey();
            
            
            if (!key.getNamespace().equals(Atom.getInstance().getName().toLowerCase())) {
                continue;
            }
            
            List<RecipeChoice> ingredients = new ArrayList<>();
            
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                
                Map<Character, RecipeChoice> choiceMap = shapedRecipe.getChoiceMap();
                ingredients.addAll(choiceMap.values());
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                
                ingredients.addAll(shapelessRecipe.getChoiceList());
            }
            
            if (!ingredients.isEmpty()) {
                recipeIngredients.put(key, ingredients);
                Atom.getInstance().getLogger().info("Extracted " + ingredients.size() + 
                    " ingredients from recipe: " + key.getKey());
            }
        }
    }
    
    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTaskLater(player, () -> {
                checkAndUnlockRecipes(player);
            }, 1L);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTaskLater(player, () -> {
                checkAndUnlockRecipes(player);
            }, 1L);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTask(player, () -> {
                checkAndUnlockRecipes(player);
            });
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTaskLater(player, () -> {
            checkAndUnlockRecipes(player);
        }, 20L);
    }
    
    private void checkAndUnlockRecipes(Player player) {
        for (Map.Entry<NamespacedKey, List<RecipeChoice>> entry : recipeIngredients.entrySet()) {
            NamespacedKey key = entry.getKey();
            List<RecipeChoice> ingredients = entry.getValue();
            
            
            if (player.hasDiscoveredRecipe(key)) {
                continue;
            }
            
            
            if (hasAllIngredients(player, ingredients)) {
                player.discoverRecipe(key);
                player.sendMessage("§a§l✓ §aYou discovered a new recipe!");
            }
        }
    }
    
    private boolean hasAllIngredients(Player player, List<RecipeChoice> requiredIngredients) {
        ItemStack[] contents = player.getInventory().getContents();
        
        for (RecipeChoice choice : requiredIngredients) {
            
            if (choice == null) continue;
            
            boolean found = false;
            
            for (ItemStack item : contents) {
                if (item == null || item.getType() == Material.AIR) continue;
                
                if (choice.test(item)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                return false;
            }
        }
        
        return true;
    }
}
