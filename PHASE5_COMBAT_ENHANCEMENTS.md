# Phase 5: Combat Enhancement Systems - Implementation Summary

## Overview
Implemented realistic combat enhancements including injury tracking, morale systems, fatigue mechanics, and advanced hunting behaviors.

## Created Combat Systems (3 Systems)

### 1. InjurySystem.java
**Location:** `src/main/java/org/shotrush/atom/content/mobs/ai/combat/InjurySystem.java`

**Features:**
- Three injury levels based on health percentage:
  - HEALTHY (>70% health)
  - WOUNDED (30-70% health)
  - CRITICALLY_INJURED (<30% health)
- Speed debuffs:
  - Wounded: 0.7x speed
  - Critical: 0.5x speed
- Blood trail particles (DUST with dark red color)
- Limping effect with irregular movement
- Metadata tracking for injury state

**Key Methods:**
- `getInjuryLevel(Mob)` - Determines current injury state
- `applyInjuryEffects(Mob)` - Applies speed and movement debuffs
- `spawnBloodTrail(Mob)` - Creates blood particle effects
- `getSpeedMultiplier(Mob)` - Returns injury-based speed modifier

---

### 2. MoraleSystem.java
**Location:** `src/main/java/org/shotrush/atom/content/mobs/ai/combat/MoraleSystem.java`

**Features:**
- Herd/pack morale tracking within 20 block radius
- Morale breaks when >50% of nearby allies are dead/fleeing
- Broken morale forces panic/flee behavior
- Automatic morale restoration after regrouping (100 ticks)
- Cascading morale break affects all nearby pack members

**Key Methods:**
- `checkMorale(Mob)` - Evaluates current morale state
- `isMoraleBroken(Mob)` - Returns morale status
- `breakMorale(Mob)` - Triggers morale break for mob and nearby allies
- `restoreMorale(Mob)` - Restores morale after regrouping

---

### 3. FatigueSystem.java
**Location:** `src/main/java/org/shotrush/atom/content/mobs/ai/combat/FatigueSystem.java`

**Features:**
- Combat drains stamina 3x faster than normal movement
- Tracks consecutive combat ticks
- Fatigue sets in after 100 combat ticks
- Fatigued effects:
  - 60% damage output
  - 70% attack speed
- Recovery period of 200 ticks when not in combat
- SWEEP_ATTACK particle effects when fatigued

**Key Methods:**
- `trackCombat(Mob)` - Increments combat tick counter
- `resetCombat(Mob)` - Handles recovery when out of combat
- `isFatigued(Mob)` - Returns fatigue state
- `applyFatigueDebuff(Mob)` - Applies combat penalties
- `getDamageMultiplier(Mob)` - Returns damage modifier
- `getAttackSpeedMultiplier(Mob)` - Returns attack speed modifier

---

## Created Combat Goals (3 Goals)

### 1. StalkPreyGoal.java
**Location:** `src/main/java/org/shotrush/atom/content/mobs/ai/goals/StalkPreyGoal.java`

**Features:**
- Stealth approach before attacking (wolves/foxes)
- Activates when prey is 5-20 blocks away
- Crouch/sneak movement (0.5x speed)
- Attempts to stay in prey's blind spot (120° behind arc)
- Transitions to pounce when within 5 blocks
- Detection chance: prey can spot stalker if in line of sight

**Priority:** 3

---

### 2. TrackWoundedPreyGoal.java
**Location:** `src/main/java/org/shotrush/atom/content/mobs/ai/goals/TrackWoundedPreyGoal.java`

**Features:**
- Detects wounded animals using InjurySystem
- 32 block detection radius
- 1.5x aggro range multiplier for wounded targets
- Prioritizes critically injured over wounded (0.7x distance modifier)
- Automatic targeting when wounded prey detected
- Attack when within 3 blocks

**Priority:** 2 (higher than normal hunting)

---

### 3. FlankAndSurroundGoal.java
**Location:** `src/main/java/org/shotrush/atom/content/mobs/ai/goals/FlankAndSurroundGoal.java`

**Features:**
- Enhanced pack hunting tactics for wolves
- Requires minimum 3 pack members
- Circular surround formation (8 block radius)
- Role assignment:
  - ATTACKER: Attacks from behind/sides (blind spots)
  - DISTRACTOR: Stays in front to draw attention
- Coordinated position rotation every 40 ticks
- Uses VisionSystem to exploit target's blind spots
- CRIT particles on successful attacks

**Priority:** 2

---

## Enhanced Existing Goals (2 Goals)

### 1. ChaseAndMeleeAttackGoal.java (Enhanced)
**Integrations:**
- FatigueSystem: Tracks combat ticks, applies attack speed debuffs
- InjurySystem: Applies speed multipliers to movement
- MoraleSystem: Stops pursuit if morale broken
- Dynamic attack interval based on fatigue state
- Speed modifiers applied during chase

---

### 2. HerdPanicGoal.java (Enhanced)
**Integrations:**
- MoraleSystem: Forces panic when morale broken
- Checks morale before other panic triggers
- Integrates with existing health-based panic
- Maintains compatibility with herd panic mechanics

---

## Technical Implementation Details

### Metadata Usage
All systems use Bukkit metadata for state tracking:
- `injury_level` - Current injury state (String)
- `injury_speed_multiplier` - Speed modifier (Double)
- `morale_broken` - Morale state (Boolean)
- `morale_break_timer` - Recovery timer (Int)
- `combat_ticks` - Combat duration tracking (Int)
- `fatigued` - Fatigue state (Boolean)
- `stamina` - Current stamina value (Double)

### Thread Safety
- All systems are Folia-safe
- Uses entity-specific metadata (no shared state)
- Entity scheduler used for periodic tasks
- No main thread assumptions

### Particle Effects
- **Blood Trail:** DUST particles with dark red (RGB: 139, 0, 0)
- **Fatigue:** SWEEP_ATTACK particles every 40 ticks
- **Combat:** CRIT particles on flanking attacks
- **Coordination:** ANGRY_VILLAGER for pack communication

### Integration Points
Combat systems are instantiated in `AnimalBehaviorNew.java`:
```java
this.injurySystem = new InjurySystem(plugin);
this.fatigueSystem = new FatigueSystem(plugin);
this.moraleSystem = new MoraleSystem(plugin, herdManager);
```

Systems are passed to goals during registration:
```java
new ChaseAndMeleeAttackGoal(mob, plugin, behavior, fatigueSystem, injurySystem, moraleSystem)
new HerdPanicGoal(mob, plugin, herdManager, behavior, moraleSystem)
```

---

## Build Status
✅ **BUILD SUCCESSFUL** - All files compiled without errors

## Files Modified
1. Created: `InjurySystem.java`
2. Created: `MoraleSystem.java`
3. Created: `FatigueSystem.java`
4. Created: `StalkPreyGoal.java`
5. Created: `TrackWoundedPreyGoal.java`
6. Created: `FlankAndSurroundGoal.java`
7. Modified: `ChaseAndMeleeAttackGoal.java`
8. Modified: `HerdPanicGoal.java`
9. Modified: `AnimalBehaviorNew.java`

## Testing Recommendations
1. Spawn wolves in pack and observe flanking behavior
2. Damage animal to <70% health and verify blood trails
3. Kill 50%+ of herd members and observe morale break
4. Engage in prolonged combat (100+ ticks) to trigger fatigue
5. Test stalking behavior with wolves hunting prey
6. Verify wounded animals are prioritized by predators

---

**Phase 5 Complete!** Combat systems now include realistic injury, morale, fatigue, and advanced hunting tactics.
