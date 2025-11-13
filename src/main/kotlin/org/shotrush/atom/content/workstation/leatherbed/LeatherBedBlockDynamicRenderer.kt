package org.shotrush.atom.content.workstation.leatherbed

import it.unimi.dsi.fastutil.ints.IntList
import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData
import net.momirealms.craftengine.bukkit.nms.FastNMS
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer
import net.momirealms.craftengine.core.entity.Billboard
import net.momirealms.craftengine.core.entity.ItemDisplayContext
import net.momirealms.craftengine.core.entity.player.Player
import net.momirealms.craftengine.core.plugin.CraftEngine
import org.bukkit.inventory.ItemStack
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID

class LeatherBedBlockDynamicRenderer(val entity: LeatherBedBlockEntity) : DynamicBlockEntityRenderer {
    private var cachedSpawnPacket: Any? = null
    private var cachedDespawnPacket: Any? = null
    private var entityId = 0

    fun displayNewItem() {
        val pos = entity.pos()
        val entityId = CoreReflections.`instance$Entity$ENTITY_COUNTER`.incrementAndGet()
        this.cachedSpawnPacket = FastNMS.INSTANCE.`constructor$ClientboundAddEntityPacket`(
            entityId,
            UUID.randomUUID(),
            (pos.x().toDouble() + 0.5),
            (pos.y().toDouble() + 0.5),
            (pos.z().toDouble() + 0.5),
            0f,
            45f,
            MEntityTypes.ITEM_DISPLAY,
            0,
            CoreReflections.`instance$Vec3$Zero`,
            0.0
        )
        this.cachedDespawnPacket = FastNMS.INSTANCE.`constructor$ClientboundRemoveEntitiesPacket`(IntList.of(entityId))
        this.entityId = entityId
    }

    fun getDataValues(): List<Any> {
        val dataValues = mutableListOf<Any>()
        ItemDisplayEntityData.DisplayedItem.addEntityDataIfNotDefaultValue(
            CraftEngine.instance().itemManager<ItemStack>().wrap(entity.storedItem).literalObject,
            dataValues
        )
        ItemDisplayEntityData.Scale.addEntityDataIfNotDefaultValue(Vector3f(1f, 1f, 1f), dataValues)
        ItemDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(
            Quaternionf(
                AxisAngle4f(
                    (Math.PI / 2).toFloat(),
                    0f,
                    0f,
                    0f
                )
            ), dataValues
        )
        ItemDisplayEntityData.RotationRight.addEntityDataIfNotDefaultValue(
            Quaternionf(
                AxisAngle4f(
                    0f,
                    0f,
                    0f,
                    1f
                )
            ), dataValues
        )
        ItemDisplayEntityData.BillboardConstraints.addEntityDataIfNotDefaultValue(Billboard.FIXED.id(), dataValues)
        ItemDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(Vector3f(0f, 0f, 0f), dataValues)
        ItemDisplayEntityData.DisplayType.addEntityDataIfNotDefaultValue(ItemDisplayContext.NONE.id(), dataValues)
        ItemDisplayEntityData.ShadowRadius.addEntityDataIfNotDefaultValue(0.0f, dataValues)
        ItemDisplayEntityData.ShadowStrength.addEntityDataIfNotDefaultValue(1.0f, dataValues)
        ItemDisplayEntityData.ViewRange.addEntityDataIfNotDefaultValue(64f, dataValues)
        return dataValues
    }

    override fun show(player: Player) {
        val packet = cachedSpawnPacket ?: return
        player.sendPackets(
            listOf(
                packet,
                FastNMS.INSTANCE.`constructor$ClientboundSetEntityDataPacket`(
                    this.entityId,
                    getDataValues()
                )
            ), true
        )
    }

    override fun hide(player: Player) {
        if(this.cachedDespawnPacket == null) return
        player.sendPacket(this.cachedDespawnPacket, false)
    }

    override fun update(player: Player) {
        hide(player)
        displayNewItem()
        show(player)
    }

}