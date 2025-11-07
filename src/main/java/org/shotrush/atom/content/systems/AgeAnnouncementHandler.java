package org.shotrush.atom.content.systems;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.api.annotation.RegisterSystem;
import org.shotrush.atom.core.api.player.PlayerDataAPI;

@RegisterSystem(
    id = "age_announcement_handler",
    priority = 10,
    toggleable = true,
    description = "Handles age switch announcements to players and server"
)
public class AgeAnnouncementHandler implements Listener {
    
    private static final String LAST_SEEN_AGE_KEY = "age_announcement.last_seen_age";
    private static AgeAnnouncementHandler instance;
    
    public AgeAnnouncementHandler(Plugin plugin) {
        instance = this;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Delay announcement slightly to ensure player is fully loaded
        org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTaskLater(player, () -> {
            String currentAge = getCurrentAge();
            String lastSeenAge = PlayerDataAPI.getString(player, LAST_SEEN_AGE_KEY, "");
            
            if (!currentAge.equals(lastSeenAge)) {
                announceAgeToPlayer(player, currentAge);
                PlayerDataAPI.setString(player, LAST_SEEN_AGE_KEY, currentAge);
            }
        }, 40L); // 2 seconds delay
    }
    
    /**
     * Announces an age switch to the entire server
     */
    public static void announceAgeSwitch(String previousAge, String newAge) {
        if (instance == null) return;
        
        String previousDisplayName = getAgeDisplayName(previousAge);
        String newDisplayName = getAgeDisplayName(newAge);
        
        // Send title to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Main title - elegant and royal
            player.sendTitle(
                "§f§lThe Progress of Humanity",
                "§7" + previousDisplayName + " §8→ §f" + newDisplayName,
                20, 120, 40
            );
            
            // Elegant sound progression
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BELL_USE, 1.0f, 0.8f);
            org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTaskLater(player, () -> {
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BELL_USE, 1.0f, 1.0f);
            }, 10L);
            org.shotrush.atom.core.api.scheduler.SchedulerAPI.runTaskLater(player, () -> {
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BELL_USE, 1.0f, 1.2f);
            }, 20L);
            
            PlayerDataAPI.setString(player, LAST_SEEN_AGE_KEY, newAge);
        }
    }
    
    /**
     * Announces the current age to a specific player
     */
    private void announceAgeToPlayer(Player player, String age) {
        String displayName = getAgeDisplayName(age);
        
        // Send title
        player.sendTitle(
            "§f§lThe Current Age",
            "§7" + displayName,
            20, 100, 30
        );
        
        // Elegant bell sound
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BELL_USE, 1.0f, 1.0f);
    }
    
    /**
     * Gets the current age from the server
     */
    private String getCurrentAge() {
        // This should integrate with your age system
        // For now, return a default value
        return PlayerDataAPI.getString(Bukkit.getOnlinePlayers().iterator().hasNext() ? 
            Bukkit.getOnlinePlayers().iterator().next() : null, "current_age", "foraging_age");
    }
    
    /**
     * Gets the display name for an age
     */
    private static String getAgeDisplayName(String age) {
        switch (age.toLowerCase()) {
            case "foraging_age":
            case "foraging":
                return "Foraging";
            case "wood_age":
            case "wood":
                return "Wood";
            case "stone_age":
            case "stone":
                return "Stone";
            case "bronze_age":
            case "bronze":
                return "Bronze";
            case "iron_age":
            case "iron":
                return "Iron";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Gets the description for an age
     */
    private static String getAgeDescription(String age) {
        switch (age.toLowerCase()) {
            case "foraging_age":
            case "foraging":
                return "Gather resources and craft basic tools";
            case "wood_age":
            case "wood":
                return "Harness the strength of timber and craft";
            case "stone_age":
            case "stone":
                return "Master stone tools and build shelters";
            case "bronze_age":
            case "bronze":
                return "Smelt metals and forge advanced equipment";
            case "iron_age":
            case "iron":
                return "Harness iron and create powerful weapons";
            default:
                return "A new era begins";
        }
    }
}
