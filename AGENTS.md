# Atom - Agent Guidelines

## Build & Test Commands
- **Build**: `./gradlew build` (also runs shadowJar for shaded dependencies)
- **Clean Build**: `./gradlew clean build`
- **Run Test Server**: `./gradlew runServer` (starts Minecraft 1.21 test server)
- **No tests configured yet** - testing framework needs to be added

## Architecture
- **Project Type**: Minecraft Paper/Folia plugin (Java 21, Gradle)
- **Main Class**: `org.shotrush.atom.Atom`
- **Folia Support**: Fully regionized multithreading compatible
- **Core Components**: XP calculation engine with honorary XP inheritance, skill tree hierarchy, SQLite storage with HikariCP, Caffeine cache, event-driven XP tracking
- **Package Structure**: `model` (data), `tree` (skill hierarchy), `engine` (XP calculations), `storage` (database), `listener` (events), `manager` (player data), `config` (trees/definitions), `effects` (penalties/bonuses), `progression` (skill levels), `milestone` (achievements), `advancement` (UI), `commands` (admin), `detection` (crafting), `ml` (future clustering)

## Code Style & Conventions
- **Language**: Java 21 with records, sealed types, pattern matching
- **Code Quality**: Immutable models, thread-safe concurrent collections, async-first with CompletableFuture, builder pattern for complex objects
- **Naming**: Clear descriptive names, no abbreviations (e.g., `EffectiveXp`, `PlayerSkillData`, `SkillTreeRegistry`)
- **Dependencies**: All shaded and relocated under `org.shotrush.atom.*` namespace to prevent conflicts
- **Threading**: NO main thread assumptions - use `GlobalRegionScheduler` for periodic tasks and `EntityScheduler` for player operations
- **Error Handling**: Validate early (null checks, bounds), fail fast at boundaries, log exceptions with stack traces
- **Comments**: Self-documenting code preferred - no inline comments unless complex algorithm
