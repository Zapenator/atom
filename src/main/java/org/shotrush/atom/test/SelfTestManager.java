package org.shotrush.atom.test;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.shotrush.atom.Atom;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SelfTestManager {
    private final JavaPlugin plugin;
    private final List<Test> tests;
    
    public SelfTestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.tests = new ArrayList<>();
        registerTests();
    }
    
    private void registerTests() {
        tests.add(new Test("Scheduler Manager", () -> {
            var scheduler = Atom.getInstance().getSchedulerManager();
            if (scheduler == null) throw new AssertionError("Scheduler not initialized");
            return true;
        }));
        
        tests.add(new Test("Display Manager", () -> {
            var display = Atom.getInstance().getDisplayManager();
            if (display == null) throw new AssertionError("Display manager not initialized");
            if (display.getDisplayGroups() == null) throw new AssertionError("Display groups cache null");
            if (display.getAnimations() == null) throw new AssertionError("Animations cache null");
            return true;
        }));
        
        tests.add(new Test("Interaction Manager", () -> {
            var interaction = Atom.getInstance().getInteractionManager();
            if (interaction == null) throw new AssertionError("Interaction manager not initialized");
            if (interaction.getInteractions() == null) throw new AssertionError("Interactions cache null");
            return true;
        }));
        
        tests.add(new Test("World Manager", () -> {
            var world = Atom.getInstance().getWorldManager();
            if (world == null) throw new AssertionError("World manager not initialized");
            return true;
        }));
        
        tests.add(new Test("Player Manager", () -> {
            var player = Atom.getInstance().getPlayerManager();
            if (player == null) throw new AssertionError("Player manager not initialized");
            return true;
        }));
        
        tests.add(new Test("Item Manager", () -> {
            var item = Atom.getInstance().getItemManager();
            if (item == null) throw new AssertionError("Item manager not initialized");
            if (item.getItems() == null) throw new AssertionError("Items cache null");
            return true;
        }));
        
        tests.add(new Test("Recipe Manager", () -> {
            var recipe = Atom.getInstance().getRecipeManager();
            if (recipe == null) throw new AssertionError("Recipe manager not initialized");
            if (recipe.getRecipes() == null) throw new AssertionError("Recipes cache null");
            return true;
        }));
        
        tests.add(new Test("Folia Detection", () -> {
            var scheduler = Atom.getInstance().getSchedulerManager();
            boolean isFolia = scheduler.isFolia();
            plugin.getLogger().info("Running on " + (isFolia ? "Folia" : "Paper"));
            return true;
        }));
        
        tests.add(new Test("Recipe Clearing", () -> {
            int recipeCount = 0;
            Iterator<org.bukkit.inventory.Recipe> it = Bukkit.recipeIterator();
            while (it.hasNext()) {
                it.next();
                recipeCount++;
            }
            if (recipeCount > 0) {
                plugin.getLogger().warning("Found " + recipeCount + " recipes still registered");
            }
            return true;
        }));
    }
    
    public void runTests() {
        plugin.getLogger().info("Running self-tests...");
        
        AtomicInteger passed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        
        tests.forEach(test -> {
            try {
                if (test.run()) {
                    passed.incrementAndGet();
                } else {
                    failed.incrementAndGet();
                    plugin.getLogger().severe("Test failed: " + test.name);
                }
            } catch (Exception e) {
                failed.incrementAndGet();
                plugin.getLogger().severe("Test error: " + test.name + " - " + e.getMessage());
            }
        });
        
        plugin.getLogger().info("Tests: " + passed.get() + " passed, " + failed.get() + " failed");
        
        if (failed.get() > 0) {
            plugin.getLogger().warning("Some tests failed! Plugin may not work correctly.");
        }
    }
    
    public void addTest(String name, TestRunnable test) {
        tests.add(new Test(name, test));
    }
    
    private record Test(String name, TestRunnable test) {
        boolean run() throws Exception {
            return test.run();
        }
    }
    
    @FunctionalInterface
    public interface TestRunnable {
        boolean run() throws Exception;
    }
}
