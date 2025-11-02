package org.shotrush.atom.content.mobs.ai.environment;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class EnvironmentalContext {
    
    public static TimeOfDay getTimeOfDay(World world) {
        long time = world.getTime();
        
        if (time >= 0 && time < 6000) {
            return TimeOfDay.MORNING;
        } else if (time >= 6000 && time < 12000) {
            return TimeOfDay.DAY;
        } else if (time >= 12000 && time < 13000) {
            return TimeOfDay.DUSK;
        } else if (time >= 13000 && time < 18000) {
            return TimeOfDay.NIGHT;
        } else {
            return TimeOfDay.PREDAWN;
        }
    }
    
    public static boolean isDaytime(World world) {
        long time = world.getTime();
        return time < 12300 || time > 23850;
    }
    
    public static boolean isNighttime(World world) {
        return !isDaytime(world);
    }
    
    public static WeatherCondition getWeatherCondition(World world) {
        if (world.isThundering()) {
            return WeatherCondition.THUNDERSTORM;
        } else if (world.hasStorm()) {
            return WeatherCondition.RAIN;
        } else {
            return WeatherCondition.CLEAR;
        }
    }
    
    public static double getActivityModifier(World world, ActivityPattern pattern) {
        TimeOfDay timeOfDay = getTimeOfDay(world);
        
        return switch (pattern) {
            case DIURNAL -> switch (timeOfDay) {
                case MORNING -> 1.0;
                case DAY -> 1.2;
                case DUSK -> 0.8;
                case NIGHT -> 0.3;
                case PREDAWN -> 0.5;
            };
            case NOCTURNAL -> switch (timeOfDay) {
                case MORNING -> 0.4;
                case DAY -> 0.2;
                case DUSK -> 0.8;
                case NIGHT -> 1.2;
                case PREDAWN -> 1.0;
            };
            case CREPUSCULAR -> switch (timeOfDay) {
                case MORNING -> 1.0;
                case DAY -> 0.5;
                case DUSK -> 1.2;
                case NIGHT -> 0.4;
                case PREDAWN -> 1.0;
            };
            case ALWAYS_ACTIVE -> 1.0;
        };
    }
    
    public static BiomePreference getBiomePreference(Biome biome, BiomeType preferredType) {
        BiomeType actualType = categorizeBiome(biome);
        
        if (actualType == preferredType) {
            return BiomePreference.PREFERRED;
        }
        
        return switch (preferredType) {
            case PLAINS, FOREST -> actualType == BiomeType.PLAINS || actualType == BiomeType.FOREST 
                ? BiomePreference.NEUTRAL 
                : BiomePreference.UNCOMFORTABLE;
            case MOUNTAIN -> actualType == BiomeType.FOREST 
                ? BiomePreference.NEUTRAL 
                : BiomePreference.UNCOMFORTABLE;
            case DESERT -> actualType == BiomeType.SAVANNA || actualType == BiomeType.MESA 
                ? BiomePreference.NEUTRAL 
                : BiomePreference.UNCOMFORTABLE;
            case TAIGA -> actualType == BiomeType.MOUNTAIN || actualType == BiomeType.TUNDRA 
                ? BiomePreference.NEUTRAL 
                : BiomePreference.UNCOMFORTABLE;
            case TUNDRA -> actualType == BiomeType.TAIGA || actualType == BiomeType.MOUNTAIN 
                ? BiomePreference.NEUTRAL 
                : BiomePreference.UNCOMFORTABLE;
            default -> BiomePreference.NEUTRAL;
        };
    }
    
    private static BiomeType categorizeBiome(Biome biome) {
        String name = biome.name().toLowerCase();
        
        if (name.contains("plains")) return BiomeType.PLAINS;
        if (name.contains("forest") || name.contains("jungle")) return BiomeType.FOREST;
        if (name.contains("mountain") || name.contains("peak") || name.contains("hill")) return BiomeType.MOUNTAIN;
        if (name.contains("desert") || name.contains("badlands")) return BiomeType.DESERT;
        if (name.contains("taiga") || name.contains("grove")) return BiomeType.TAIGA;
        if (name.contains("tundra") || name.contains("snowy") || name.contains("frozen") || name.contains("ice")) return BiomeType.TUNDRA;
        if (name.contains("savanna") || name.contains("mesa")) return BiomeType.SAVANNA;
        if (name.contains("swamp") || name.contains("mangrove")) return BiomeType.SWAMP;
        if (name.contains("ocean") || name.contains("river") || name.contains("beach")) return BiomeType.AQUATIC;
        if (name.contains("nether")) return BiomeType.NETHER;
        if (name.contains("end")) return BiomeType.END;
        
        return BiomeType.PLAINS;
    }
    
    public static boolean shouldSeekShelter(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        
        if (!world.hasStorm()) return false;
        
        if (location.getBlock().getLightFromSky() < 15) {
            return false;
        }
        
        return true;
    }
    
    public enum TimeOfDay {
        MORNING,
        DAY,
        DUSK,
        NIGHT,
        PREDAWN
    }
    
    public enum WeatherCondition {
        CLEAR,
        RAIN,
        THUNDERSTORM
    }
    
    public enum ActivityPattern {
        DIURNAL,
        NOCTURNAL,
        CREPUSCULAR,
        ALWAYS_ACTIVE
    }
    
    public enum BiomeType {
        PLAINS,
        FOREST,
        MOUNTAIN,
        DESERT,
        TAIGA,
        TUNDRA,
        SAVANNA,
        SWAMP,
        MESA,
        AQUATIC,
        NETHER,
        END
    }
    
    public enum BiomePreference {
        PREFERRED(1.0),
        NEUTRAL(0.8),
        UNCOMFORTABLE(0.5),
        HOSTILE(0.2);
        
        private final double speedModifier;
        
        BiomePreference(double speedModifier) {
            this.speedModifier = speedModifier;
        }
        
        public double getSpeedModifier() {
            return speedModifier;
        }
    }
}
