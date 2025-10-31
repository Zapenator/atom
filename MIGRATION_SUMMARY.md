# Atom Plugin Migration Summary

## ✅ Completed Tasks

### 1. Package Structure Reorganization
- **Core Block System**: `org.shotrush.atom.core.blocks`
  - BlockType.java (Interface)
  - CustomBlock.java (Abstract base)
  - CustomBlockRegistry.java
  - CustomBlockManager.java
  - CustomBlockDataManager.java

- **Content - Cog**: `org.shotrush.atom.content.cog`
  - Cog.java (Implementation)
  - CogBlockType.java (Factory)

- **Commands**: `org.shotrush.atom.commands`
  - CogCommand.java
  - WrenchCommand.java
  - RemoveCogsCommand.java
  - AgeCommand.java

- **Core Systems**: `org.shotrush.atom.core`
  - blocks/ (Block framework)
  - age/ (Age system)
  - storage/ (Data storage)

### 2. Folia Compatibility Updates

#### CustomBlockManager.java
- ❌ Removed: `BukkitRunnable` and `BukkitTask`
- ✅ Added: `ScheduledTask` from Folia API
- ✅ Updated: Global update timer now uses `Bukkit.getGlobalRegionScheduler().runAtFixedRate()`

**Before:**
```java
globalUpdateTask = new BukkitRunnable() {
    @Override
    public void run() {
        // update logic
    }
}.runTaskTimer(plugin, 0L, 1L);
```

**After:**
```java
globalUpdateTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
    // update logic
}, 1L, 1L);
```

### 3. Plugin Configuration
- ✅ Updated `plugin.yml` with:
  - `folia-supported: true`
  - Command definitions (cog, wrench, removecogs)

### 4. Main Plugin Class
- ✅ Updated imports to use new package structure
- ✅ Properly initializes CustomBlockManager
- ✅ Saves blocks on shutdown

## 🎯 System Features

### Custom Block System
- **Modular Design**: Easy to add new block types
- **Persistent Storage**: Blocks save/load from `blocks.yml`
- **Event-Driven**: Automatic placement and removal
- **Wrench Tool**: Interact with and remove blocks

### Cog System
- **Power Sources**: Right-click with wrench to toggle
- **Power Propagation**: Adjacent cogs receive power
- **Direction Logic**: Cogs rotate based on axis alignment
- **Global Updates**: Synchronized rotation via global angle

## 📝 Commands

- `/cog` - Get a cog item
- `/wrench` - Get a wrench tool
- `/removecogs` - Remove all cogs from the world

## 🔧 How It Works

1. **Place a Cog**: Use the cog item (barrier block with custom data)
2. **Toggle Power**: Right-click with wrench to make it a power source
3. **Power Propagation**: Adjacent cogs automatically receive power
4. **Remove**: Shift + Right-click with wrench

## ⚙️ Folia Compatibility

All schedulers now use Folia's region-based scheduling:
- Global updates use `GlobalRegionScheduler`
- Entity-specific tasks would use `EntityScheduler`
- Region-specific tasks would use `RegionScheduler`

## 📦 File Structure

```
org.shotrush.atom/
├── Atom.java (Main plugin class)
├── core/
│   ├── blocks/ (Block framework - reusable)
│   │   ├── BlockType.java
│   │   ├── CustomBlock.java
│   │   ├── CustomBlockRegistry.java
│   │   ├── CustomBlockManager.java
│   │   └── CustomBlockDataManager.java
│   ├── age/ (Age progression system)
│   └── storage/ (Data persistence)
├── content/
│   └── cog/ (Cog implementation)
│       ├── Cog.java
│       └── CogBlockType.java
└── commands/
    ├── AgeCommand.java
    ├── CogCommand.java
    ├── WrenchCommand.java
    └── RemoveCogsCommand.java
```

### Architecture Benefits
- **Core**: Reusable block framework
- **Content**: Specific block implementations (cogs, conveyors, etc.)
- **Separation**: Easy to add new block types without modifying core

## ✨ Ready to Build

The plugin is now:
- ✅ Properly organized
- ✅ Folia-compatible
- ✅ Using correct package structure
- ✅ Ready to compile and test
