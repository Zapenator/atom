package org.shotrush.atom.content.foragingage.blocks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.RegionAccessor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.blocks.CustomBlock;
import org.shotrush.atom.core.blocks.annotation.AutoRegister;
import org.shotrush.atom.core.items.CustomItem;
import org.shotrush.atom.core.util.ActionBarManager;

@AutoRegister(priority = 30)
public class Campfire extends CustomBlock implements Listener {
    
    private boolean lit;
    private long lastFuelTime;
    private static final long BURN_DURATION = 120000; // 2 minutes in milliseconds
    private static final String LIGHTING_ITEM = "pebble";
    
    // Static listener registration
    static {
        Atom plugin = Atom.getInstance();
        if (plugin != null) {
            plugin.getServer().getPluginManager().registerEvents(new CampfireInteractionListener(), plugin);
        }
    }
    
    public Campfire(Location spawnLocation, Location blockLocation, BlockFace blockFace) {
        super(spawnLocation, blockLocation, blockFace);
        this.lit = false;
        this.lastFuelTime = System.currentTimeMillis(); // Start with fuel (straw included in craft)
    }
    
    public Campfire(Location spawnLocation, BlockFace blockFace) {
        super(spawnLocation, blockFace);
        this.lit = false;
        this.lastFuelTime = System.currentTimeMillis(); // Start with fuel (straw included in craft)
    }
    
    public boolean isLit() {
        return lit && !hasBurnedOut();
    }
    
    public void light() {
        this.lit = true;
        updateVisual();
    }
    
    public boolean hasFuel() {
        return lastFuelTime > 0 && !hasBurnedOut();
    }
    
    private boolean hasBurnedOut() {
        if (lastFuelTime == 0) return true;
        return System.currentTimeMillis() - lastFuelTime > BURN_DURATION;
    }
    
    private void addFuel() {
        lastFuelTime = System.currentTimeMillis();
    }
    
    private void updateVisual() {
        if (blockLocation != null && blockLocation.getBlock().getType() == Material.CAMPFIRE) {
            org.bukkit.block.data.type.Campfire campfireData = 
                (org.bukkit.block.data.type.Campfire) blockLocation.getBlock().getBlockData();
            campfireData.setLit(isLit());
            blockLocation.getBlock().setBlockData(campfireData, false);
        }
    }
    
    @Override
    public boolean onWrenchInteract(Player player, boolean sneaking) {
        if (sneaking) {
            return false; // Allow default removal behavior
        }
        return false; // Don't handle wrench interaction
    }
    
    @Override
    public boolean onInteract(Player player, boolean sneaking) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) return false;
        
        if (lit && hasBurnedOut()) {
            lit = false;
            updateVisual();
            ActionBarManager.send(player, "§7The campfire has burned out");
        }
        
        // Check for straw to add fuel
        CustomItem straw = Atom.getInstance().getItemRegistry().getItem("straw");
        if (straw != null && straw.isCustomItem(heldItem)) {
            if (!sneaking) {
                addFuel();
                heldItem.setAmount(heldItem.getAmount() - 1);
                
                long remainingTime = BURN_DURATION - (System.currentTimeMillis() - lastFuelTime);
                int remainingMinutes = (int) (remainingTime / 60000);
                int remainingSeconds = (int) ((remainingTime % 60000) / 1000);
                
                ActionBarManager.send(player, "§aFuel added! Burns for " + remainingMinutes + "m " + remainingSeconds + "s");
                return true;
            }
        }
        
        // Check for lighting item - start the lighting process
        CustomItem lightingItem = Atom.getInstance().getItemRegistry().getItem(LIGHTING_ITEM);
        if (lightingItem != null && lightingItem.isCustomItem(heldItem)) {
            Atom.getInstance().getLogger().info("Pebble detected, starting lighting for " + player.getName());
            if (!CampfireHandler.isLighting(player)) {
                Atom.getInstance().getLogger().info("Starting campfire lighting process");
                CampfireHandler.startLighting(player, this);
            } else {
                Atom.getInstance().getLogger().info("Player is already lighting");
            }
            return true;
        }

        return false;
    }
    
    @Override
    public void spawn(Atom plugin, RegionAccessor accessor) {
        cleanupExistingEntities();
        if (blockLocation != null) {
            blockLocation.getBlock().setType(Material.CAMPFIRE, false);
            org.bukkit.block.data.type.Campfire campfireData = 
                (org.bukkit.block.data.type.Campfire) blockLocation.getBlock().getBlockData();
            campfireData.setLit(isLit());
            campfireData.setSignalFire(false);
            campfireData.setWaterlogged(false);
            blockLocation.getBlock().setBlockData(campfireData, false);
        }
    }
    
    @Override
    protected void cleanupExistingEntities() {
        super.cleanupExistingEntities();
        if (blockLocation != null && blockLocation.getBlock().getType() == Material.CAMPFIRE) {
            blockLocation.getBlock().setType(Material.AIR, false);
        }
    }
    
    @Override
    public void update(float globalAngle) {
        if (lit && hasBurnedOut()) {
            lit = false;
            updateVisual();
        }
    }
    
    @Override
    public String getIdentifier() {
        return "campfire";
    }
    
    @Override
    public String getDisplayName() {
        return "Campfire";
    }
    
    @Override
    public Material getItemMaterial() {
        return Material.CAMPFIRE;
    }
    
    @Override
    public String[] getLore() {
        return new String[]{
            "§7A campfire for warmth and cooking",
            "§7Add straw for fuel",
            "§7Light with a pebble"
        };
    }
    
    @Override
    public String serialize() {
        return lit + "," + lastFuelTime;
    }
    
    @Override
    public CustomBlock deserialize(String data) {
        if (data == null || data.isEmpty()) return this;
        String[] parts = data.split(",");
        if (parts.length >= 2) {
            this.lit = Boolean.parseBoolean(parts[0]);
            this.lastFuelTime = Long.parseLong(parts[1]);
        }
        if (spawnLocation.getWorld() != null) {
            spawn(Atom.getInstance());
        }
        return this;
    }
    
    // Static listener class for campfire interactions
    private static class CampfireInteractionListener implements Listener {
        @EventHandler
        public void onCampfireInteract(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            if (event.getClickedBlock() == null) return;
            if (event.getClickedBlock().getType() != Material.CAMPFIRE) return;
            
            // Find if this is our custom campfire
            Location clickedLoc = event.getClickedBlock().getLocation();
            Campfire campfire = Atom.getInstance().getBlockManager().getBlocks().stream()
                .filter(b -> b instanceof Campfire)
                .map(b -> (Campfire) b)
                .filter(c -> c.getBlockLocation() != null && c.getBlockLocation().equals(clickedLoc))
                .findFirst()
                .orElse(null);
            
            if (campfire == null) return;
            
            Atom.getInstance().getLogger().info("Found custom campfire at " + clickedLoc);
            
            // Handle the interaction
            Player player = event.getPlayer();
            campfire.onInteract(player, player.isSneaking());
        }
    }
}

