package org.shotrush.atom.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.shotrush.atom.Atom;
import org.shotrush.atom.display.DisplayParser;
import java.util.ArrayList;
import java.util.List;

public class AtomCommand implements CommandExecutor, TabCompleter {
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "info" -> sendInfo(sender);
            case "reload" -> reload(sender);
            case "test" -> runTests(sender);
            case "stats" -> showStats(sender);
            case "parse" -> {
                if (sender instanceof Player player && args.length > 1) {
                    String summonCmd = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    DisplayParser.parseCommand(summonCmd, player.getLocation());
                    player.sendMessage(Component.text("Display parsed and spawned!", NamedTextColor.GREEN));
                }
            }
            default -> sendHelp(sender);
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Atom Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/atom info - Plugin information", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/atom reload - Reload configuration", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/atom test - Run self-tests", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/atom stats - Show statistics", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/atom parse <command> - Parse display command", NamedTextColor.GRAY));
    }
    
    private void sendInfo(CommandSender sender) {
        var atom = Atom.getInstance();
        sender.sendMessage(Component.text("=== Atom Plugin ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: 5.0-ALPHA", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Server: " + (atom.getSchedulerManager().isFolia() ? "Folia" : "Paper"), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Display Groups: " + atom.getDisplayManager().getDisplayGroups().estimatedSize(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Animations: " + atom.getDisplayManager().getAnimations().estimatedSize(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Interactions: " + atom.getInteractionManager().getInteractions().estimatedSize(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Custom Items: " + atom.getItemManager().getItems().estimatedSize(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Custom Recipes: " + atom.getRecipeManager().getRecipes().estimatedSize(), NamedTextColor.GRAY));
    }
    
    private void reload(CommandSender sender) {
        sender.sendMessage(Component.text("Reloading Atom...", NamedTextColor.YELLOW));
        Atom.getInstance().getTestManager().runTests();
        sender.sendMessage(Component.text("Reload complete!", NamedTextColor.GREEN));
    }
    
    private void runTests(CommandSender sender) {
        sender.sendMessage(Component.text("Running tests...", NamedTextColor.YELLOW));
        Atom.getInstance().getTestManager().runTests();
        sender.sendMessage(Component.text("Tests complete! Check console for results.", NamedTextColor.GREEN));
    }
    
    private void showStats(CommandSender sender) {
        var atom = Atom.getInstance();
        sender.sendMessage(Component.text("=== Statistics ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Display Groups: " + atom.getDisplayManager().getDisplayGroups().estimatedSize(), NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Active Animations: " + atom.getDisplayManager().getAnimations().estimatedSize(), NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Interactions: " + atom.getInteractionManager().getInteractions().estimatedSize(), NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Custom Items: " + atom.getItemManager().getItems().estimatedSize(), NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Custom Recipes: " + atom.getRecipeManager().getRecipes().estimatedSize(), NamedTextColor.AQUA));
    }
    
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("info");
            completions.add("reload");
            completions.add("test");
            completions.add("stats");
            completions.add("parse");
        }
        
        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .toList();
    }
}
