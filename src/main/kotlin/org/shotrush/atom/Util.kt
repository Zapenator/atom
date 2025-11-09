package org.shotrush.atom

import net.momirealms.craftengine.bukkit.api.CraftEngineItems
import net.momirealms.craftengine.core.item.CustomItem
import net.momirealms.craftengine.core.util.Key
import org.bukkit.inventory.ItemStack
import org.shotrush.atom.item.isItem

fun ItemStack.matches(key: Key) = CraftEngineItems.getCustomItemId(this) == key
fun ItemStack.matches(key: String) = matches(Key.of(key))
fun ItemStack.matches(namespace: String, path: String) = matches(Key.of(namespace, path))
fun ItemStack.matches(item: CustomItem<ItemStack>) = item.isItem(this)