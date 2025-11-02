package org.shotrush.atom.content.foragingage.workstations.knappingstation;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.storage.DataStorage;
import org.shotrush.atom.core.util.MessageUtil;
import org.shotrush.atom.core.util.RightClickDetector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KnappingHandler {
    
    private static final Map<UUID, KnappingProgress> activeKnapping = new HashMap<>();
    
    private static class KnappingProgress {
        long startTime;
        int requiredStrikes;
        int currentStrikes = 0;
        long lastStrikeTime = 0;
        
        KnappingProgress(long startTime) {
            this.startTime = startTime;
            this.requiredStrikes = 15 + (int)(Math.random() * 11);
            this.lastStrikeTime = startTime;
        }
    }
    
    public static void startKnapping(Player player, Location dropLocation, Runnable onComplete) {
        UUID playerId = player.getUniqueId();
        
        if (activeKnapping.containsKey(playerId)) {
            return;
        }
        
        class KnappingTask implements Runnable {
            
            @Override
            public void run() {
                if (!player.isOnline() || !activeKnapping.containsKey(playerId)) {
                    activeKnapping.remove(playerId);
                }
                
                if (player.getLocation().distance(dropLocation) > 5.0) {
                    player.setLevel(0);
                    player.setExp(0);
                    activeKnapping.remove(playerId);
                    MessageUtil.send(player, "§cYou moved too far away!");
                    return;
                }
                
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() != Material.BRUSH) {
                    player.setLevel(0);
                    player.setExp(0);
                    activeKnapping.remove(playerId);
                    RightClickDetector.clear(playerId);
                    MessageUtil.send(player, "§cYou stopped knapping!");
                    return;
                }
                
                KnappingProgress progress = activeKnapping.get(playerId);
                long currentTime = System.currentTimeMillis();
                
                boolean isClicking = RightClickDetector.isRightClicking(playerId);
                long timeSinceLastStrike = currentTime - progress.lastStrikeTime;
                
                Atom.getInstance().getLogger().info("Knapping check - Clicking: " + isClicking + ", Time since last: " + timeSinceLastStrike);
                
                if (isClicking && timeSinceLastStrike > 300) {
                    progress.currentStrikes++;
                    progress.lastStrikeTime = currentTime;
                    player.playSound(player.getLocation(), Sound.BLOCK_STONE_HIT, 1.0f, 0.8f + (float)(Math.random() * 0.4f));
                    player.swingMainHand();
                    RightClickDetector.clear(playerId);
                    
                    Atom.getInstance().getLogger().info("Strike registered! Count: " + progress.currentStrikes + "/" + progress.requiredStrikes);
                    
                    if (progress.currentStrikes >= progress.requiredStrikes) {
                        finishKnapping(player, dropLocation, onComplete);
                        player.setLevel(0);
                        player.setExp(0);
                        activeKnapping.remove(playerId);
                        return;
                    }
                }
                
                float progressPercent = (float) progress.currentStrikes / progress.requiredStrikes;
                player.setLevel(progress.currentStrikes);
                player.setExp(progressPercent);
                
                player.getScheduler().runDelayed(Atom.getInstance(), task -> run(), null, 1L);
            }
        }
        
        activeKnapping.put(playerId, new KnappingProgress(System.currentTimeMillis()));
        
        player.setLevel(0);
        player.setExp(0);
        MessageUtil.send(player, "§7Started knapping! Strike the flint...");
        
        player.getScheduler().run(Atom.getInstance(), task -> new KnappingTask().run(), null);
    }
    
    private static void finishKnapping(Player player, Location dropLocation, Runnable onComplete) {
        DataStorage storage = Atom.getInstance().getDataStorage();
        UUID uuid = player.getUniqueId();
        YamlConfiguration config = storage.getPlayerData(uuid);
        
        int knapCount = config.getInt("knapping.count", 0);
        
        double failChance = Math.max(0.1, 0.5 - (knapCount * 0.02));
        
        if (Math.random() < failChance) {
            MessageUtil.send(player, "§cThe flint broke!");
            player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
            onComplete.run();
        } else {
            MessageUtil.send(player, "§aSuccessfully sharpened the flint!");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
            onComplete.run();
            
            ItemStack sharpenedFlint = Atom.getInstance().getItemRegistry().createItem("sharpened_flint");
            if (sharpenedFlint != null) {
                double temperature = org.shotrush.atom.content.systems.PlayerTemperatureSystem
                    .getInstance().getPlayerTemperature(player);
                
                org.shotrush.atom.core.items.ItemQuality quality = 
                    org.shotrush.atom.core.items.ItemQuality.fromTemperature(temperature);
                
                org.shotrush.atom.core.api.ItemQualityAPI.setQuality(sharpenedFlint, quality);
                
                player.getWorld().dropItemNaturally(dropLocation, sharpenedFlint);
            }
        }
        
        knapCount++;
        config.set("knapping.count", knapCount);
        storage.savePlayerData(uuid, config);
    }
    
    public static boolean isKnapping(Player player) {
        return activeKnapping.containsKey(player.getUniqueId());
    }
    
    public static void cancelKnapping(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeKnapping.containsKey(playerId)) {
            player.setLevel(0);
            player.setExp(0);
            activeKnapping.remove(playerId);
        }
    }
}
