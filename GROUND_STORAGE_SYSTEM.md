# Ground Item Storage System

## Overview
A flexible item storage system that allows players to place up to 2 items on any non-interactable block surface (like dirt, stone, grass, etc.). Items are displayed as 3D models on the ground and persist across server restarts.

## Features
- **Place items on any solid block**: Right-click with an item to place it on the ground
- **Store up to 2 items per block**: Each block can hold 2 items side by side
- **Visual display**: Items appear as small 3D models on the block surface
- **Persistent storage**: Items survive server restarts (uses the existing CustomBlock persistence system)
- **Easy retrieval**: Shift + Right-click to remove items one at a time
- **Automatic cleanup**: Storage removes itself when all items are taken

## How to Use

### Placing Items
1. Hold any placeable item (diamonds, sticks, tools, etc.)
2. Right-click on a solid, non-interactable block (dirt, stone, grass, etc.)
3. The item will be placed on the ground as a 3D display
4. Right-click again with another item to place a second item (max 2 per block)

### Removing Items
- **Shift + Right-click** on the storage to remove the last placed item
- The item will be added to your inventory
- When all items are removed, the storage automatically cleans up

### With Wrench
- **Shift + Right-click with wrench** to instantly remove all items and the storage

## Valid Blocks
Any solid block that is NOT interactable in vanilla Minecraft:
- ✅ Dirt, Grass, Stone, Cobblestone, Wood Planks, etc.
- ❌ Chests, Furnaces, Crafting Tables, Anvils, etc.

## Valid Items
Any item that is NOT a vanilla interactable block:
- ✅ Diamonds, Emeralds, Sticks, Tools, Food, etc.
- ❌ Chests, Furnaces, Crafting Tables (as items)

## Technical Details

### Files Created
1. **GroundItemStorage.java** - The custom block that manages item storage
   - Extends `InteractiveSurface` for item placement functionality
   - Stores up to 2 items with position and rotation data
   - Handles serialization/deserialization for persistence
   - Auto-registered with priority 10

2. **GroundItemStorageHandler.java** - Event handler for creating storage
   - Listens for player right-click events
   - Creates storage when clicking valid blocks with items
   - Registered as a system with `@RegisterSystem` annotation
   - Priority 15, toggleable, enabled by default

3. **GroundStorageCommand.java** - Test command for the system
   - `/groundstorage` or `/gs` - Shows usage instructions and gives test items
   - Auto-registered command with priority 40

### Integration with Existing Systems
- Uses the existing `InteractiveSurface` base class (same as KnappingStation and AnvilSurface)
- Leverages the `CustomBlockManager` for persistence and entity management
- Follows the same UUID update pattern for entity persistence after restarts
- Compatible with the item heat tracking system
- Works with the quality inheritance system

### Item Display
- Items are displayed at 40% scale
- Random rotation for visual variety
- Positioned side by side when 2 items are present
- Slight elevation (0.05 blocks) above the surface
- No collision or physics

### Persistence
- Saves item data, positions, and rotations
- Survives server restarts
- Uses the existing CustomBlock serialization system
- Automatically updates entity UUIDs on restart (from memory of previous fixes)

## Testing
Run `/groundstorage` to get test items and instructions.

## Configuration
The system can be toggled on/off through the system registry:
- System ID: `ground_item_storage_handler`
- Toggleable: Yes
- Enabled by default: Yes

## Comparison with Similar Systems

### vs Knapping Station
- **Knapping Station**: Specialized workstation, 1 item, requires pebble to interact
- **Ground Storage**: General storage, 2 items, direct interaction

### vs Anvil Surface
- **Anvil Surface**: Placeable block item, 2 items, wrench required
- **Ground Storage**: Created on-demand, 2 items, no wrench needed for basic use

### vs Crafting Basket
- **Crafting Basket**: Recipe checking, GUI mode, multiple items
- **Ground Storage**: Simple storage, no recipes, 2 items max
