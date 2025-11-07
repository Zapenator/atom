package org.shotrush.atom.content.foragingage.items;

import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.shotrush.atom.core.items.CustomItem;
import org.shotrush.atom.core.items.annotation.AutoRegister;

import java.util.Arrays;
import java.util.List;

@AutoRegister(priority = 4)
public class StrawItem extends CustomItem {

    public StrawItem(Plugin plugin) {
        super(plugin);
    }

    @Override
    public String getIdentifier() {
        return "straw";
    }

    @Override
    public Material getMaterial() {
        return Material.WHEAT;
    }

    @Override
    public String getDisplayName() {
        return "§eStraw";
    }

    @Override
    public List<String> getLore() {
        return Arrays.asList(
                "§7Dried grass strands",
                "§7Useful for crafting",
                "§8• Material",
                "§8[Foraging Age Material]"
        );
    }

    @Override
    protected void applyCustomMeta(ItemMeta meta) {
        org.shotrush.atom.core.util.ItemUtil.setCustomModelName(meta, "straw");
    }
}
