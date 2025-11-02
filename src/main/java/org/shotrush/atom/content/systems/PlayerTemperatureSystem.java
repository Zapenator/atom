package org.shotrush.atom.content.systems;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.potion.PotionEffectType;
import org.shotrush.atom.core.systems.annotation.AutoRegisterSystem;

@AutoRegisterSystem(priority = 4)
public class PlayerTemperatureSystem implements Listener {
    
    @Getter
    private static PlayerTemperatureSystem instance;
    
    private final Plugin plugin;
    private final Map<UUID, Double> playerTemperatures = new HashMap<>();
    private final Map<UUID, Double> lastTemperatures = new HashMap<>();
    
    private static final double NORMAL_TEMP = 37.0;
    private static final double MAX_TEMP = 45.0;
    private static final double MIN_TEMP = 25.0;
    private static final double SHOCK_THRESHOLD = 3.0;
    
    public PlayerTemperatureSystem(Plugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        org.bukkit.configuration.file.YamlConfiguration config = 
            org.shotrush.atom.Atom.getInstance().getDataStorage().getPlayerData(playerId);
        double savedTemp = config.getDouble("temperature.body", NORMAL_TEMP);
        
        playerTemperatures.put(playerId, savedTemp);
        lastTemperatures.put(playerId, savedTemp);
        startTemperatureTickForPlayer(player);
    }
    
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        double temp = playerTemperatures.getOrDefault(playerId, NORMAL_TEMP);
        
        org.bukkit.configuration.file.YamlConfiguration config = 
            org.shotrush.atom.Atom.getInstance().getDataStorage().getPlayerData(playerId);
        config.set("temperature.body", temp);
        org.shotrush.atom.Atom.getInstance().getDataStorage().savePlayerData(playerId, config);
        
        playerTemperatures.remove(playerId);
        lastTemperatures.remove(playerId);
    }
    
    private void startTemperatureTickForPlayer(Player player) {
        player.getScheduler().runAtFixedRate(plugin, task -> {
            if (!player.isOnline()) {
                task.cancel();
                return;
            }
            updatePlayerTemperature(player);
        }, null, 1L, 20L);
    }
    
    private void updatePlayerTemperature(Player player) {
        UUID playerId = player.getUniqueId();
        double currentTemp = playerTemperatures.getOrDefault(playerId, NORMAL_TEMP);
        double lastTemp = lastTemperatures.getOrDefault(playerId, currentTemp);
        org.bukkit.Location loc = player.getLocation();
        
        double tempChange = org.shotrush.atom.core.api.EnvironmentalFactorAPI
            .calculateEnvironmentalTemperatureChange(player, loc, 0.002);
        
        double armorInsulation = org.shotrush.atom.core.api.ArmorProtectionAPI.getInsulationValue(player);
        tempChange *= (1.0 - armorInsulation);
        
        if (currentTemp > NORMAL_TEMP) {
            tempChange -= 0.003;
        } else if (currentTemp < NORMAL_TEMP) {
            tempChange += 0.002;
        }
        
        double newTemp = Math.max(MIN_TEMP, Math.min(MAX_TEMP, currentTemp + tempChange));
        
        double tempDelta = Math.abs(newTemp - lastTemp);
        if (tempDelta >= SHOCK_THRESHOLD) {
            applyTemperatureShock(player, tempDelta, newTemp > lastTemp);
            lastTemperatures.put(playerId, newTemp);
        }
        
        playerTemperatures.put(playerId, newTemp);
        
        applyTemperatureEffects(player, newTemp);
    }
    
    private void applyTemperatureShock(Player player, double delta, boolean heating) {
        if (heating) {
            if (delta >= 5.0) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.NAUSEA, 200, 1, false, false
                ));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.WEAKNESS, 200, 1, false, false
                ));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    PotionEffectType.NAUSEA, 100, 0, false, false
                ));
                player.damage(3.0);
            } else if (delta >= 3.0) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.NAUSEA, 100, 0, false, false
                ));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.WEAKNESS, 100, 0, false, false
                ));
                player.damage(1.5);
            }
        } else {
            if (delta >= 5.0) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS, 200, 2, false, false
                ));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.MINING_FATIGUE, 200, 1, false, false
                ));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.WEAKNESS, 200, 1, false, false
                ));
                player.setFreezeTicks(Math.min(player.getFreezeTicks() + 100, 140));
                player.damage(3.0);
            } else if (delta >= 3.0) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS, 100, 1, false, false
                ));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.WEAKNESS, 100, 0, false, false
                ));
                player.setFreezeTicks(Math.min(player.getFreezeTicks() + 40, 140));
                player.damage(1.5);
            }
        }
    }
    
    private void applyTemperatureEffects(Player player, double temp) {
        org.shotrush.atom.core.api.TemperatureEffectsAPI.applyBodyTemperatureEffects(player, temp);
        
        String tempDisplay = String.format("%.1f°C", temp);
        String color = org.shotrush.atom.core.api.TemperatureEffectsAPI.getBodyTempColor(temp);
        player.sendActionBar(net.kyori.adventure.text.Component.text("§7Body Temperature: " + color + tempDisplay));
    }
    
    public double getPlayerTemperature(Player player) {
        return playerTemperatures.getOrDefault(player.getUniqueId(), NORMAL_TEMP);
    }
}
