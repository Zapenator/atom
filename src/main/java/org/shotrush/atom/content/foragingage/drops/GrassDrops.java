package org.shotrush.atom.content.foragingage.drops;

import org.bukkit.Material;
import org.shotrush.atom.core.blocks.annotation.CustomBlockDrops;
import org.shotrush.atom.core.blocks.annotation.CustomBlockDrops.Drop;

@CustomBlockDrops(
    blocks = {
        Material.SHORT_GRASS,
        Material.TALL_GRASS,
        Material.FERN,
        Material.LARGE_FERN
    },
    drops = {
        @Drop(customItemId = "straw", chance = 0.3, min = 1, max = 1)
    },
    replaceVanillaDrops = false,
    ages = {"foraging_age"}
)
public class GrassDrops {
}
