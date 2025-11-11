package org.shotrush.atom.item

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import io.papermc.paper.datacomponent.item.TooltipDisplay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import net.momirealms.craftengine.core.item.CustomItem
import net.momirealms.craftengine.core.util.Key
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

object Molds {
    fun getMold(tool: MoldShape, variant: MoldType): CustomItem<ItemStack> {
        return CraftEngineItems.byId(Key.of("atom", "${variant.id}_mold_${tool.id}"))!!
    }

    fun getToolHead(tool: MoldShape, material: Material): CustomItem<ItemStack> {
        return CraftEngineItems.byId(Key.of("atom", "${material.id}_${tool.id}_head"))!!
    }

    fun getFilledMold(shape: MoldShape, variant: MoldType, material: Material): ItemStack {
        if (variant != MoldType.Wax && variant != MoldType.Fired) throw IllegalArgumentException("Only Wax and Fired molds can be filled!")
        val item = CraftEngineItems.byId(Key.of("atom", "filled_${variant.id}_mold_${shape.mold}"))!!
        val stack = item.buildItemStack()
        val lore = stack.lore() ?: mutableListOf()
        val loreCopy = lore.toMutableList()
        // replace lore at index 0
        loreCopy[0] = Component.text("Filled with: ").style {
            it.decoration(TextDecoration.ITALIC, false).color(
                NamedTextColor.GRAY
            )
        }.append(
            Component.translatable(
                "material.${material.id}.name",
                TextColor.color(material.rgb.asRGB())
            )
        )
        stack.lore(loreCopy)
        stack.setData(
            DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(
                material.rgb
            )
        )
        stack.setData(
            DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .hiddenComponents(setOf(DataComponentTypes.DYED_COLOR))
                .build()
        )
        stack.editPersistentDataContainer {
            it.set(NamespacedKey("atom", "mold_type"), PersistentDataType.STRING, variant.id)
            it.set(NamespacedKey("atom", "mold_shape"), PersistentDataType.STRING, shape.id)
            it.set(NamespacedKey("atom", "mold_fill"), PersistentDataType.STRING, material.id)
        }
        return stack
    }

    fun emptyMold(stack: ItemStack): Pair<ItemStack, ItemStack> {
        val moldTypeId =
            stack.persistentDataContainer.get(NamespacedKey("atom", "mold_type"), PersistentDataType.STRING)!!
        val moldShapeId =
            stack.persistentDataContainer.get(NamespacedKey("atom", "mold_shape"), PersistentDataType.STRING)!!
        val materialId =
            stack.persistentDataContainer.get(NamespacedKey("atom", "mold_fill"), PersistentDataType.STRING)!!

        val moldType = MoldType.byId(moldTypeId)
        val moldShape = MoldShape.byId(moldShapeId)
        val material = Material.byId(materialId)

        val emptyMold = getMold(moldShape, moldType).buildItemStack()
        val toolHead = getToolHead(moldShape, material).buildItemStack()
        return Pair(emptyMold, toolHead)
    }
}