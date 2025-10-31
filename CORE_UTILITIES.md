# Core Block Utilities

Generalized utilities extracted from the cog system for reuse across all custom blocks.

## 📦 Utilities

### 1. BlockLocationUtil
**Purpose**: Location-based operations for blocks

**Key Methods:**
- `getAdjacentLocations(Location)` - Get 6 cardinal adjacent locations
- `getAllAdjacentLocations(Location)` - Get all 26 adjacent locations (including diagonals)
- `isSameBlock(Location, Location)` - Check if two locations are the same block
- `getAdjacentBlocks(Location, List<CustomBlock>, Class<T>)` - Find adjacent blocks of a specific type
- `getManhattanDistance(Location, Location)` - Calculate block distance
- `isAdjacent(Location, Location)` - Check if locations are adjacent

**Example Usage:**
```java
// Find all adjacent cogs
List<Cog> adjacentCogs = BlockLocationUtil.getAdjacentBlocks(
    cogLocation, 
    allBlocks, 
    Cog.class
);

// Check if two blocks are adjacent
if (BlockLocationUtil.isAdjacent(loc1, loc2)) {
    // They're neighbors
}
```

---

### 2. BlockNetworkUtil
**Purpose**: Network/graph operations for connected blocks

**Key Methods:**
- `breadthFirstSearch(...)` - BFS traversal with depth tracking
- `breadthFirstSearchWithState(...)` - BFS with custom state propagation
- `filterBlocks(List<CustomBlock>, Class<T>)` - Filter blocks by type
- `findConnectedNetwork(T, List<CustomBlock>, Class<T>)` - Find all connected blocks

**Example Usage:**
```java
// Filter all cogs from blocks
List<Cog> cogs = BlockNetworkUtil.filterBlocks(allBlocks, Cog.class);

// Find all cogs in a connected network
Set<Cog> network = BlockNetworkUtil.findConnectedNetwork(
    startCog, 
    allBlocks, 
    Cog.class
);

// BFS with custom logic
BlockNetworkUtil.breadthFirstSearch(
    allBlocks,
    Cog.class,
    cog -> cog.isPowerSource(),  // Source predicate
    (cog, depth) -> {             // Visit callback
        cog.setPowerLevel(depth);
    }
);
```

---

### 3. BlockAxisUtil
**Purpose**: Axis-based operations for directional blocks

**Key Methods:**
- `getAxis(BlockFace)` - Get axis from block face (X, Y, Z)
- `isSameAxis(BlockFace, BlockFace)` - Check if faces share an axis
- `isConnectedAlongAxis(Location, Location, Axis)` - Check if locations are aligned on an axis
- `getPrimaryAxis(Location, Location)` - Get the main axis between two locations
- `getAxisOffset(Location, Location, Axis)` - Get offset along an axis

**Example Usage:**
```java
// Check if two cogs are on the same axis
if (BlockAxisUtil.isSameAxis(cog1.getBlockFace(), cog2.getBlockFace())) {
    // Same axis
}

// Check if connected along Y axis
if (BlockAxisUtil.isConnectedAlongAxis(loc1, loc2, Axis.Y)) {
    // Vertically aligned
}
```

---

## 🎯 Benefits

### Before (Cog-Specific):
```java
// Hard-coded in CogManager
private List<Cog> getAdjacentCogs(Cog cog, List<Cog> allCogs) {
    List<Cog> adjacent = new ArrayList<>();
    Location loc = cog.getBlockLocation();
    
    Location[] adjacentLocations = {
        loc.clone().add(1, 0, 0),
        loc.clone().add(-1, 0, 0),
        // ... manual implementation
    };
    // ... manual location comparison
}
```

### After (Generic):
```java
// Reusable across all block types
List<Cog> adjacent = BlockLocationUtil.getAdjacentBlocks(
    cog.getBlockLocation(), 
    allBlocks, 
    Cog.class
);
```

---

## 📝 Use Cases

### Power Networks (Cogs)
- Find adjacent cogs
- Propagate power through BFS
- Calculate rotation based on axis alignment

### Conveyor Systems
- Find connected conveyors
- Determine item flow direction
- Check axis alignment for turns

### Pipe Networks
- Find connected pipes
- Calculate fluid flow
- Detect network loops

### Machine Multiblocks
- Validate structure
- Find all parts
- Check proper orientation

---

## 🔧 Implementation Example

**Before (CogManager - 118 lines):**
- Custom adjacent finding
- Manual BFS implementation
- Hard-coded location comparison

**After (CogManager - 96 lines):**
```java
public void recalculatePower(List<CustomBlock> allBlocks) {
    List<Cog> cogs = BlockNetworkUtil.filterBlocks(allBlocks, Cog.class);
    
    // ... reset logic ...
    
    List<Cog> adjacent = BlockLocationUtil.getAdjacentBlocks(
        currentCog.getBlockLocation(), 
        allBlocks, 
        Cog.class
    );
    
    // ... power propagation ...
}
```

**Reduction:** 22 lines removed, cleaner code, reusable utilities!

---

## 🚀 Future Extensions

These utilities can support:
- **Redstone-like systems**: Signal propagation
- **Fluid networks**: Flow calculation
- **Item transport**: Routing logic
- **Energy grids**: Power distribution
- **Multiblock structures**: Validation and assembly
