package org.shotrush.atom.example;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.shotrush.atom.Atom;
import org.shotrush.atom.display.AnimationBuilder;
import org.shotrush.atom.display.DisplayGroup;

public class ExampleUsage {
    
    public static void createAnimatedDisplay(Location location) {
        var manager = Atom.getInstance().getDisplayManager();
        
        BlockDisplay display = manager.createBlockDisplay(location, Material.COPPER_BLOCK);
        
        manager.animate(display)
            .rotate(0, 360, 0, 40)
            .scale(1.5f, 1.5f, 1.5f, 20)
            .wait(10)
            .scale(1f, 1f, 1f, 20)
            .easing(AnimationBuilder.EasingFunction.EASE_IN_OUT)
            .loop();
    }
    
    public static void createComplexStructure(Location location) {
        var manager = Atom.getInstance().getDisplayManager();
        var interactionManager = Atom.getInstance().getInteractionManager();
        
        DisplayGroup group = manager.createDisplayGroup(location);
        
        BlockDisplay base = manager.createBlockDisplay(location, Material.COPPER_BLOCK);
        BlockDisplay top = manager.createBlockDisplay(location.clone().add(0, 1, 0), Material.CUT_COPPER);
        
        group.addDisplay(base);
        group.addDisplay(top);
        
        interactionManager.attachToGroup(group, 1.0f, 2.0f, (player, interaction) -> {
            player.sendMessage(Component.text("You clicked the structure!", NamedTextColor.GREEN));
        });

        group.rotate(45);
    }
    
    public static void createCustomItem() {
        var itemManager = Atom.getInstance().getItemManager();
        
        itemManager.create("magic_sword", Material.DIAMOND_SWORD)
            .name(Component.text("Magic Sword", NamedTextColor.GOLD))
            .lore(
                Component.text("A legendary weapon", NamedTextColor.GRAY),
                Component.text("Damage: +10", NamedTextColor.RED)
            )
            .customModelData(1001)
            .unbreakable(true)
            .data("damage_bonus", 10)
            .data("magic_type", "fire")
            .build();
    }
    
    public static void createCustomRecipe() {
        var recipeManager = Atom.getInstance().getRecipeManager();
        var itemManager = Atom.getInstance().getItemManager();
        
        ItemStack result = itemManager.createItemStack("magic_sword");
        
        recipeManager.shapedRecipe("magic_sword_recipe", result)
            .shape("  D", " S ", "S  ")
            .ingredient('D', Material.DIAMOND)
            .ingredient('S', Material.STICK)
            .onCraft(event -> {
                Player player = (Player) event.getWhoClicked();
                player.sendMessage(Component.text("You crafted a Magic Sword!", NamedTextColor.GOLD));
            })
            .build();
    }
    
    public static void setupWorldRules() {
        var worldManager = Atom.getInstance().getWorldManager();
        
        worldManager.allowBlockDrop(Material.STONE);
        worldManager.allowBlockDrop(Material.DIRT);
        
        worldManager.setCustomDrop(Material.DIAMOND_ORE, 
            new ItemStack(Material.DIAMOND, 2),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 1)
        );
    }
}
