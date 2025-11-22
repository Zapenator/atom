package org.shotrush.atom.content

import org.bukkit.NamespacedKey
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.plugin.Plugin
import org.shotrush.atom.core.api.AtomAPI
import org.shotrush.atom.item.MoldShape
import org.shotrush.atom.item.MoldType
import org.shotrush.atom.item.Molds

object MoldRecipes {
    fun register(plugin: Plugin) {
        AtomAPI.Scheduler.runGlobalTask {
                val key = NamespacedKey(plugin, "campfire_firing_generic_clay_mold")
                val result = Molds.getMold(MoldShape.Ingot, MoldType.Fired).buildItemStack() // Default fallback
                val recipe = CampfireRecipe(
                    key,
                    result,
                    org.bukkit.Material.CLAY_BALL,
                    0.35f,
                    1200
                )
                
                plugin.server.addRecipe(recipe)
                plugin.logger.info("Registered generic campfire firing recipe for clay molds.")
                return@runGlobalTask
        }
    }
}
