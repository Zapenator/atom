package org.shotrush.atom.content.systems;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.systems.annotation.AutoRegisterSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@AutoRegisterSystem(priority = 3)
public class ItemHeatSystem implements Listener {
    
    private final Atom plugin;
    private static final NamespacedKey HEAT_KEY = new NamespacedKey("atom", "item_heat");
    private static final NamespacedKey HEAT_MODIFIER_KEY = new NamespacedKey("atom", "heat_modifier");
    private final Map<UUID, BossBar> heatBossBars = new HashMap<>();
    
    public ItemHeatSystem(Atom plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        startHeatTickForPlayer(player);
        createHeatBossBar(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BossBar bossBar = heatBossBars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }
    
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        
        updateHeatBossBar(player, item);
        
        if (item != null && item.getType() != Material.AIR) {
            applyHeatEffect(player, item);
        } else {
            removeHeatEffect(player);
        }
    }
    
    private void startHeatTickForPlayer(Player player) {
        player.getScheduler().runAtFixedRate(plugin, task -> {
            if (!player.isOnline()) {
                task.cancel();
                return;
            }
            
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (heldItem != null && heldItem.getType() != Material.AIR) {
                updateItemHeatFromEnvironment(player, heldItem);
                player.getInventory().setItemInMainHand(heldItem);
                applyHeatEffect(player, heldItem);
                updateHeatBossBar(player, heldItem);
            } else {
                updateHeatBossBar(player, null);
            }
        }, null, 1L, 20L);
    }
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item droppedItem = event.getItemDrop();
        ItemStack item = droppedItem.getItemStack();
        
        double heat = getItemHeat(item);
        
        
        if (heat >= 50) {
            ItemStack chestplate = player.getInventory().getChestplate();
            boolean hasProtection = chestplate != null && chestplate.getType() == Material.LEATHER_CHESTPLATE;
            
            if (!hasProtection) {
                player.setFireTicks(40); 
            }
        }
        
        startDroppedItemHeatTracking(droppedItem);
    }
    
    private void startDroppedItemHeatTracking(Item droppedItem) {
        droppedItem.getScheduler().runAtFixedRate(plugin, task -> {
            if (droppedItem.isDead() || !droppedItem.isValid()) {
                task.cancel();
                return;
            }
            
            ItemStack itemStack = droppedItem.getItemStack();
            double currentHeat = getItemHeat(itemStack);
            org.bukkit.Location loc = droppedItem.getLocation();
            
            double heatChange = org.shotrush.atom.core.api.EnvironmentalFactorAPI
                .getNearbyHeatSources(loc, 6);
            
            double ambientTemp = 20.0;
            if (currentHeat > ambientTemp) {
                heatChange -= 0.5;
            }
            
            double newHeat = Math.max(0, currentHeat + heatChange * 0.1);
            setItemHeat(itemStack, newHeat);
            droppedItem.setItemStack(itemStack);
            
            if (newHeat >= 200) {
                double fireChance = Math.min(0.5, (newHeat - 200) / 600);
                
                if (Math.random() < fireChance) {
                    org.bukkit.block.Block below = loc.getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
                    if (below.getType().isBurnable() || below.getType() == Material.AIR) {
                        loc.getBlock().setType(Material.FIRE);
                    }
                }
            }
        }, null, 20L, 20L);
    }
    
    private void updateItemHeatFromEnvironment(Player player, ItemStack item) {
        double currentHeat = getItemHeat(item);
        org.bukkit.Location loc = player.getLocation();
        
        double heatChange = org.shotrush.atom.core.api.EnvironmentalFactorAPI
            .calculateEnvironmentalTemperatureChange(player, loc, 0.1);
        
        double ambientTemp = 20.0;
        if (currentHeat > ambientTemp) {
            heatChange -= 0.5;
        } else if (currentHeat < ambientTemp) {
            heatChange += 0.3;
        }
        
        double newHeat = Math.max(-100, Math.min(1000, currentHeat + heatChange));
        
        if (Math.abs(newHeat - currentHeat) > 0.1) {
            setItemHeat(item, newHeat);
        }
    }
    
    private void applyHeatEffect(Player player, ItemStack item) {
        double heat = getItemHeat(item);
        boolean hasProtection = org.shotrush.atom.core.api.ArmorProtectionAPI.hasLeatherChestplate(player);
        
        if (heat != 0) {
            double speedModifier = -Math.abs(heat) * 0.001;
            org.shotrush.atom.core.api.AttributeModifierAPI.applyModifier(
                player, Attribute.MOVEMENT_SPEED, HEAT_MODIFIER_KEY,
                speedModifier, AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
        } else {
            org.shotrush.atom.core.api.AttributeModifierAPI.removeModifier(
                player, Attribute.MOVEMENT_SPEED, HEAT_MODIFIER_KEY
            );
        }
        
        org.shotrush.atom.core.api.TemperatureEffectsAPI.applyHeatDamage(player, heat, hasProtection);
        org.shotrush.atom.core.api.TemperatureEffectsAPI.applyColdDamage(player, heat, hasProtection);
    }
    
    private void removeHeatEffect(Player player) {
        org.shotrush.atom.core.api.AttributeModifierAPI.removeModifier(
            player, Attribute.MOVEMENT_SPEED, HEAT_MODIFIER_KEY
        );
    }
    
    public static double getItemHeat(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0.0;
        
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        
        return container.getOrDefault(HEAT_KEY, PersistentDataType.DOUBLE, 0.0);
    }
    
    public static void setItemHeat(ItemStack item, double heat) {
        if (item == null || item.getType() == Material.AIR) return;
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(HEAT_KEY, PersistentDataType.DOUBLE, heat);
        
        java.util.List<net.kyori.adventure.text.Component> lore = meta.lore();
        if (lore == null) {
            lore = new java.util.ArrayList<>();
        }
        
        lore.removeIf(line -> net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
            .serialize(line).contains("Heat:"));
        
        if (heat > 0) {
            lore.add(net.kyori.adventure.text.Component.text("§7Heat: §c" + (int)heat + "°C"));
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
    }
    
    private void createHeatBossBar(Player player) {
        BossBar bossBar = Bukkit.createBossBar(
            "Item Temperature",
            BarColor.BLUE,
            BarStyle.SOLID
        );
        bossBar.setVisible(false);
        bossBar.addPlayer(player);
        heatBossBars.put(player.getUniqueId(), bossBar);
    }
    
    private void updateHeatBossBar(Player player, ItemStack item) {
        BossBar bossBar = heatBossBars.get(player.getUniqueId());
        if (bossBar == null) {
            createHeatBossBar(player);
            bossBar = heatBossBars.get(player.getUniqueId());
        }
        
        if (item == null || item.getType() == Material.AIR) {
            bossBar.setVisible(false);
            return;
        }
        
        double heat = getItemHeat(item);
        if (heat <= 0) {
            bossBar.setVisible(false);
            return;
        }
        
        bossBar.setVisible(true);
        
        BarColor color;
        if (heat >= 200) {
            color = BarColor.RED;
        } else if (heat >= 100) {
            color = BarColor.YELLOW;
        } else if (heat >= 50) {
            color = BarColor.GREEN;
        } else {
            color = BarColor.WHITE;
        }
        
        bossBar.setTitle("Item Heat: " + (int)heat + "°C");
        bossBar.setColor(color);
        
        double progress = Math.min(1.0, Math.max(0.0, heat / 300.0));
        bossBar.setProgress(progress);
    }
}
