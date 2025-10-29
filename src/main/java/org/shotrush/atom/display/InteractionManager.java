package org.shotrush.atom.display;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class InteractionManager implements Listener {
    private final JavaPlugin plugin;
    @Getter private final Cache<UUID, InteractionData> interactions;
    
    public InteractionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.interactions = Caffeine.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();
    }
    
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public Interaction createInteraction(Location location, float width, float height) {
        Interaction interaction = (Interaction) location.getWorld().spawnEntity(location, EntityType.INTERACTION);
        interaction.setInteractionWidth(width);
        interaction.setInteractionHeight(height);
        interaction.setResponsive(true);
        return interaction;
    }
    
    public void attachToDisplay(Display display, float width, float height, BiConsumer<Player, Interaction> onClick) {
        Interaction interaction = createInteraction(display.getLocation(), width, height);
        display.addPassenger(interaction);
        
        InteractionData data = new InteractionData(interaction, display, onClick, null);
        interactions.put(interaction.getUniqueId(), data);
    }
    
    public void attachToDisplay(Display display, float width, float height, 
                                BiConsumer<Player, Interaction> onClick,
                                BiConsumer<Player, Interaction> onAttack) {
        Interaction interaction = createInteraction(display.getLocation(), width, height);
        display.addPassenger(interaction);
        
        InteractionData data = new InteractionData(interaction, display, onClick, onAttack);
        interactions.put(interaction.getUniqueId(), data);
    }
    
    public void attachToGroup(DisplayGroup group, float width, float height, BiConsumer<Player, Interaction> onClick) {
        group.getDisplays().forEach(display -> attachToDisplay(display, width, height, onClick));
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;
        
        InteractionData data = interactions.getIfPresent(interaction.getUniqueId());
        if (data == null) return;
        
        event.setCancelled(true);
        
        if (data.onClick != null) {
            data.onClick.accept(event.getPlayer(), interaction);
        }
    }
    
    public void processAttacks() {
        interactions.asMap().values().forEach(data -> {
            Interaction.PreviousInteraction attack = data.interaction.getLastAttack();
            if (attack != null && data.onAttack != null) {
                Player player = Bukkit.getPlayer(attack.getPlayer().getUniqueId());
                if (player != null) {
                    data.onAttack.accept(player, data.interaction);
                }
            }
        });
    }
    
    public void removeInteraction(UUID interactionId) {
        InteractionData data = interactions.getIfPresent(interactionId);
        if (data != null) {
            data.interaction.remove();
            interactions.invalidate(interactionId);
        }
    }
    
    public void shutdown() {
        interactions.asMap().values().forEach(data -> {
            if (data.interaction.isValid()) {
                org.shotrush.atom.Atom.getInstance().getSchedulerManager()
                    .runForEntity(data.interaction, () -> data.interaction.remove());
            }
        });
        interactions.invalidateAll();
    }
    
    public record InteractionData(
        Interaction interaction,
        Display display,
        BiConsumer<Player, Interaction> onClick,
        BiConsumer<Player, Interaction> onAttack
    ) {}
}
