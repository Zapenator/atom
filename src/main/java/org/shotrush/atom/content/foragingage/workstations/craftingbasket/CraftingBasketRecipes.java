package org.shotrush.atom.content.foragingage.workstations.craftingbasket;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.items.ItemQuality;
import org.shotrush.atom.core.recipe.BukkitRecipeBuilder;

import org.shotrush.atom.core.api.annotation.RegisterSystem;


@RegisterSystem(
    id = "crafting_basket_recipes",
    priority = 200,
    toggleable = false,
    description = "Registers crafting basket recipes"
)
public class CraftingBasketRecipes {
    
    public CraftingBasketRecipes(Plugin plugin) {
        registerBukkitRecipes();
    }
    
    
    private void registerBukkitRecipes() {
        registerSpearBukkitRecipe();
        registerPressureFlakerBukkitRecipe();
        registerKnifeBukkitRecipe();
        registerWaterskinBukkitRecipe();
        registerCraftingBasketBukkitRecipe();
        registerKnappingStationBukkitRecipe();
        registerStrawBaleBukkitRecipe();
        registerCampfireBukkitRecipe();
        registerDripstoneRecipe();
        registerStrawCampfireRecipe();
    }
    
    private void registerSpearBukkitRecipe() {
        BukkitRecipeBuilder.shaped("spear")
            .result("wood_spear")
            .shape(
                "  F",
                " SV",
                "S  "
            )
            .setIngredient('F', "sharpened_flint")
            .setIngredient('S', Material.STICK)
            .setIngredientChoice('V', "stabilized_leather", Material.VINE)  
            .register();
    }
    
    private void registerPressureFlakerBukkitRecipe() {
        BukkitRecipeBuilder.shapeless("pressure_flaker")
            .result("pressure_flaker")
            .ingredient("bone")
            .register();
    }
    
    private void registerKnifeBukkitRecipe() {
        BukkitRecipeBuilder.shaped("knife")
            .result("knife")
            .shape(
                "  F",
                " SL"
            )
            .setIngredient('F', "sharpened_flint", ItemQuality.HIGH)
            .setIngredient('S', Material.STICK)
            .setIngredient('L', "stabilized_leather")
            .register();
    }
    
    private void registerWaterskinBukkitRecipe() {
        BukkitRecipeBuilder.shaped("waterskin")
            .result("waterskin")
            .shape(
                "L L",
                " L "
            )
            .setIngredient('L', "stabilized_leather")
            .register();
    }
    
    private void registerCraftingBasketBukkitRecipe() {
        ItemStack craftingBasketItem = Atom.getInstance().getBlockManager().createBlockItem("crafting_basket");
        if (craftingBasketItem != null) {
            BukkitRecipeBuilder.shapeless("crafting_basket_recipe")
                .result(craftingBasketItem)
                .ingredient(Material.LEAF_LITTER, 3)
                .ingredient(Material.VINE)
                .register();
        }
    }
    
    private void registerKnappingStationBukkitRecipe() {
        ItemStack knappingStationItem = Atom.getInstance().getBlockManager().createBlockItem("knapping_station");
        if (knappingStationItem != null) {
            BukkitRecipeBuilder.shapeless("knapping_station_recipe")
                .result(knappingStationItem)
                .ingredient(Material.HAY_BLOCK)
                .ingredient("uncured_leather")
                .register();
        }
    }
    
    private void registerStrawBaleBukkitRecipe() {
        ItemStack haybale = new ItemStack(Material.HAY_BLOCK, 1);
        BukkitRecipeBuilder.shaped("straw_bale_recipe")
            .result(haybale)
            .shape(
                "SSS",
                "SSS",
                "SSS"
            )
            .setIngredient('S', "straw")
            .register();
    }
    
    private void registerCampfireBukkitRecipe() {
        ItemStack campfireItem = Atom.getInstance().getBlockManager().createBlockItem("campfire");
        if (campfireItem != null) {
            BukkitRecipeBuilder.shapeless("campfire_recipe")
                .result(campfireItem)
                .ingredient(Material.STICK, 3)
                .ingredient("straw")
                .register();
        }
    }
    
    private void registerDripstoneRecipe() {
        ItemStack dripstone = new ItemStack(Material.POINTED_DRIPSTONE, 4);
        BukkitRecipeBuilder.shapeless("dripstone_recipe")
            .result(dripstone)
            .ingredient("sharpened_flint")
            .register();
    }
    
    private void registerStrawCampfireRecipe() {
        // Register a campfire recipe for straw so it can be placed on campfires
        ItemStack strawItem = Atom.getInstance().getItemRegistry().createItem("straw");
        if (strawItem != null) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(Atom.getInstance(), "straw_campfire");
            // 120000ms = 120 seconds = 2 minutes, convert to ticks (20 ticks/second)
            int cookingTimeTicks = (120000 / 1000) * 20; // 2400 ticks
            org.bukkit.inventory.CampfireRecipe recipe = new org.bukkit.inventory.CampfireRecipe(
                key,
                strawItem, // Result (same as input, no transformation)
                new org.bukkit.inventory.RecipeChoice.ExactChoice(strawItem),
                0.0f, // No experience
                cookingTimeTicks // 2 minutes = 2400 ticks
            );
            org.bukkit.Bukkit.addRecipe(recipe);
        }
    }
}
