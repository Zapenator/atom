package org.shotrush.atom.content.foragingage.blocks;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.shotrush.atom.Atom;
import org.shotrush.atom.core.blocks.CustomBlock;
import org.shotrush.atom.core.blocks.CustomBlockManager;
import org.shotrush.atom.core.blocks.annotation.AutoRegister;

@AutoRegister(priority = 30)
public class ClayMold extends CustomBlock {

    private boolean filled;
    private String oreType;

    public ClayMold(Location spawnLocation, Location blockLocation, BlockFace blockFace) {
        this(spawnLocation, blockLocation, blockFace, false, null);
    }

    public ClayMold(Location spawnLocation, Location blockLocation, BlockFace blockFace, boolean filled, String oreType) {
        super(spawnLocation, blockLocation, blockFace);
        this.filled = filled;
        this.oreType = oreType;
    }

    public ClayMold(Location spawnLocation, BlockFace blockFace) {
        this(spawnLocation, blockFace, false, null);
    }

    public ClayMold(Location spawnLocation, BlockFace blockFace, boolean filled, String oreType) {
        super(spawnLocation, blockFace);
        this.filled = filled;
        this.oreType = oreType;
    }

    public boolean isFilled() {
        return filled;
    }

    public String getOreType() {
        return oreType;
    }

    public void setFilled(boolean filled, String oreType) {
        this.filled = filled;
        this.oreType = oreType;

        if (spawnLocation.getWorld() != null) {
            spawn(Atom.getInstance());
        }
    }

    @Override
    public void spawn(Atom plugin, RegionAccessor accessor) {
        cleanupExistingEntities();
        ItemDisplay display = (ItemDisplay) accessor.spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);

        String modelName = filled ? "cast_full" : "cast";
        ItemStack moldItem = createItemWithCustomModel(Material.STONE_BUTTON, modelName);

        spawnDisplay(display, plugin, moldItem, new Vector3f(0, 0.5f, 0), new AxisAngle4f(),
                new Vector3f(1f, 1f, 1f), false, 1f, 0.5f);
    }

    @Override
    public void update(float globalAngle) {
    }

    @Override
    public String getIdentifier() {
        return "clay_mold";
    }

    @Override
    public String getDisplayName() {
        return filled ? "§6Clay Mold (Filled)" : "§6Clay Mold";
    }

    @Override
    public Material getItemMaterial() {
        return Material.STONE_BUTTON;
    }

    @Override
    public String[] getLore() {
        if (filled && oreType != null) {
            return new String[]{
                    "§7A clay mold filled with molten ore",
                    "§6Contains: Molten " + oreType,
                    "§8[Crafting Tool]"
            };
        } else if (filled) {
            return new String[]{
                    "§7A clay mold filled with molten ore",
                    "§8[Crafting Tool]"
            };
        } else {
            return new String[]{
                    "§7An empty clay mold",
                    "§7Used for shaping items",
                    "§8[Crafting Tool]"
            };
        }
    }

    @Override
    public ItemStack getDropItem() {
        Atom plugin = Atom.getInstance();
        CustomBlockManager manager = plugin.getBlockManager();

        ItemStack item = manager.createBlockItem("clay_mold");

        if (item == null) {
            plugin.getLogger().warning("Failed to create drop item for clay_mold");
        }

        return item;
    }

    @Override
    protected String serializeAdditionalData() {
        return filled + ";" + (oreType != null ? oreType : "");
    }

    @Override
    protected String deserializeAdditionalData(String[] parts, int startIndex) {
        if (parts.length > startIndex) {
            filled = Boolean.parseBoolean(parts[startIndex]);
        }
        if (parts.length > startIndex + 1 && !parts[startIndex + 1].isEmpty()) {
            oreType = parts[startIndex + 1];
        }
        return null;
    }

    @Override
    public CustomBlock deserialize(String data) {
        Object[] parsed = parseDeserializeData(data);
        if (parsed == null) return null;

        ClayMold mold = new ClayMold((Location) parsed[1], (BlockFace) parsed[2]);
        String[] parts = data.split(";");
        mold.deserializeAdditionalData(parts, 5);

        return mold;
    }
}
