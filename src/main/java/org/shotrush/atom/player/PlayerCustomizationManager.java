package org.shotrush.atom.player;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.shotrush.atom.Atom;
import java.util.UUID;

public class PlayerCustomizationManager implements Listener {
    private final JavaPlugin plugin;
    private static final String BLACK_SKIN_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYwNzE4MTg5MzE4MSwKICAicHJvZmlsZUlkIiA6ICI0ZWQ4MjMzNzFhMmU0YmI3YTVlYTZmYmE4NjFhYjQ2NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJGaXJlYnlyZDg4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2QzZDQ3ZDZmZjE3ZTI5YTU0ZGRjZGQzMzNhZGU4NWU0ZjJhNTM2ZDM5YzY4YzU5YjU0YzRhNGY4ZjQ3ZGFhYjgiCiAgICB9CiAgfQp9";
    private static final String BLACK_SKIN_SIGNATURE = "signature";
    
    public PlayerCustomizationManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        Bukkit.getOnlinePlayers().forEach(this::customizePlayer);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        Atom.getInstance().getSchedulerManager().runForEntityDelayed(player, () -> {
            customizePlayer(player);
        }, 5);
    }
    
    private void customizePlayer(Player player) {
        removeNameTag(player);
        applyBlackSkin(player);
    }
    
    private void removeNameTag(Player player) {
        player.setCustomNameVisible(false);
        player.setPlayerListName("");
    }
    
    private void applyBlackSkin(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        profile.getProperties().removeIf(prop -> prop.getName().equals("textures"));
        profile.setProperty(new ProfileProperty("textures", BLACK_SKIN_TEXTURE, BLACK_SKIN_SIGNATURE));
        player.setPlayerProfile(profile);
        
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.hidePlayer(plugin, player);
            Atom.getInstance().getSchedulerManager().runForEntityDelayed(p, () -> {
                p.showPlayer(plugin, player);
            }, 2);
        });
    }
    
    public void restorePlayer(Player player) {
        player.setCustomNameVisible(true);
        player.setPlayerListName(player.getName());
    }
    
    public void shutdown() {
        Bukkit.getOnlinePlayers().forEach(this::restorePlayer);
    }
}
