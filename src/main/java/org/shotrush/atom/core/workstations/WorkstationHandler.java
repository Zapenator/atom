package org.shotrush.atom.core.workstations;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.util.ActionBarManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public abstract class WorkstationHandler<T extends WorkstationHandler.WorkProgress> implements Listener {
    
    protected final Map<UUID, T> activeProcessing = new HashMap<>();
    protected final Map<UUID, Boolean> activeDetectionTasks = new HashMap<>();
    protected final Atom plugin;
    
    public WorkstationHandler() {
        this.plugin = Atom.getInstance();
    }
    
    
    protected abstract boolean isValidTool(ItemStack item);
    
    
    protected abstract Sound getStrokeSound();
    
    
    protected abstract void spawnStrokeParticles(Location location);
    
    
    protected abstract String getStatusMessage();
    
    @EventHandler
    public void onPlayerItemHeld(org.bukkit.event.player.PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        
        if (newItem == null) {
            return;
        }
        
        if (isValidTool(newItem) && !activeDetectionTasks.containsKey(player.getUniqueId())) {
            startStrokeDetectionForPlayer(player);
        }
    }
    
    private void startStrokeDetectionForPlayer(Player player) {
        activeDetectionTasks.put(player.getUniqueId(), true);
        
        class StrokeDetectionTask implements Runnable {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    activeDetectionTasks.remove(player.getUniqueId());
                    return;
                }
                
                if (!isProcessing(player)) {
                    org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTaskLater(player, () -> run(), 1L);
                    return;
                }
                
                T progress = activeProcessing.get(player.getUniqueId());
                
                if (player.hasActiveItem()) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    
                    if (isValidTool(item) && player.getActiveItemUsedTime() >= 10) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - progress.lastStrokeTime > 300) {
                            progress.currentStrokes++;
                            progress.lastStrokeTime = currentTime;
                            player.playSound(player.getLocation(), getStrokeSound(), 1.0f, 0.8f + (float)(Math.random() * 0.4f));

                            if (progress.location != null) {
                                spawnStrokeParticles(progress.location);
                            }
                        }
                    }
                }
                
                org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTaskLater(player, () -> run(), 1L);
            }
        }
        
        org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTask(player, () -> new StrokeDetectionTask().run());
    }
    
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        activeDetectionTasks.remove(event.getPlayer().getUniqueId());
    }
    
    
    public void startProcessing(Player player, Location location, T progress, Runnable onComplete) {
        startProcessing(player, location, progress, onComplete, getStatusMessage());
    }
    
    public void startProcessing(Player player, Location location, T progress, Runnable onComplete, String statusMessage) {
        UUID playerId = player.getUniqueId();
        
        if (activeProcessing.containsKey(playerId)) {
            return;
        }
        
        class ProcessingTask implements Runnable {
            @Override
            public void run() {
                if (!player.isOnline() || !activeProcessing.containsKey(playerId)) {
                    activeProcessing.remove(playerId);
                    onProcessingCancelled(player);
                    return;
                }
                
                T currentProgress = activeProcessing.get(playerId);
                
                if (player.getLocation().distance(location) > 5.0) {
                    player.setLevel(0);
                    player.setExp(0);
                    activeProcessing.remove(playerId);
                    ActionBarManager.send(player, "§cYou moved too far away!");
                    onProcessingCancelled(player);
                    return;
                }
                
                if (currentProgress.currentStrokes >= currentProgress.requiredStrokes) {
                    player.setLevel(0);
                    player.setExp(0);
                    activeProcessing.remove(playerId);
                    onComplete.run();
                    return;
                }
                
                float progressPercent = (float) currentProgress.currentStrokes / currentProgress.requiredStrokes;
                player.setLevel(currentProgress.currentStrokes);
                player.setExp(progressPercent);
                
                org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTaskLater(player, () -> run(), 1L);
            }
        }
        
        activeProcessing.put(playerId, progress);
        
        player.setLevel(0);
        player.setExp(0);
        ActionBarManager.sendStatus(player, statusMessage);
        
        org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTask(player, () -> new ProcessingTask().run());
    }
    
    protected void onProcessingCancelled(Player player) {
        // Override in subclass if needed
    }
    
    public boolean isProcessing(Player player) {
        return activeProcessing.containsKey(player.getUniqueId());
    }
    
    public T getProgress(Player player) {
        return activeProcessing.get(player.getUniqueId());
    }
    
    public void cancelProcessing(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeProcessing.containsKey(playerId)) {
            player.setLevel(0);
            player.setExp(0);
            activeProcessing.remove(playerId);
            onProcessingCancelled(player);
        }
    }
    
    
    public static abstract class WorkProgress {
        public long startTime;
        public int requiredStrokes;
        public int currentStrokes = 0;
        public long lastStrokeTime = 0;
        public Location location;
        
        public WorkProgress(long startTime, int requiredStrokes, Location location) {
            this.startTime = startTime;
            this.requiredStrokes = requiredStrokes;
            this.lastStrokeTime = startTime;
            this.location = location;
        }
    }
}
