package org.shotrush.atom.display;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.shotrush.atom.Atom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayParser {
    private static final Pattern SUMMON_PATTERN = Pattern.compile("/summon\\s+(\\w+)\\s+([~\\d.-]+)\\s+([~\\d.-]+)\\s+([~\\d.-]+)\\s+\\{(.+)\\}");
    private static final Pattern PASSENGER_PATTERN = Pattern.compile("\\{id:\"minecraft:(\\w+)\",block_state:\\{Name:\"minecraft:(\\w+)\"[^}]*\\},transformation:\\[([\\d.f,-]+)\\]\\}");
    
    public static DisplayGroup parseCommand(String command, Location origin) {
        Matcher summonMatcher = SUMMON_PATTERN.matcher(command);
        if (!summonMatcher.find()) return null;
        
        String entityType = summonMatcher.group(1);
        double x = parseCoordinate(summonMatcher.group(2), origin.getX());
        double y = parseCoordinate(summonMatcher.group(3), origin.getY());
        double z = parseCoordinate(summonMatcher.group(4), origin.getZ());
        String nbt = summonMatcher.group(5);
        
        Location spawnLoc = new Location(origin.getWorld(), x, y, z);
        DisplayGroup group = Atom.getInstance().getDisplayManager().createDisplayGroup(spawnLoc);
        
        BlockDisplay root = Atom.getInstance().getDisplayManager().createBlockDisplay(spawnLoc, Material.COPPER_BLOCK);
        group.setRoot(root);
        
        Matcher passengerMatcher = PASSENGER_PATTERN.matcher(nbt);
        while (passengerMatcher.find()) {
            String passengerId = passengerMatcher.group(1);
            String blockName = passengerMatcher.group(2);
            String transformData = passengerMatcher.group(3);
            
            if (passengerId.contains("block_display")) {
                Material material = parseMaterial(blockName);
                BlockDisplay display = (BlockDisplay) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY);
                display.setBlock(material.createBlockData());
                
                Transformation transform = parseTransformation(transformData);
                display.setTransformation(transform);
                
                group.addDisplay(display);
            }
        }
        
        return group;
    }
    
    private static double parseCoordinate(String coord, double origin) {
        if (coord.startsWith("~")) {
            String offset = coord.substring(1);
            return origin + (offset.isEmpty() ? 0 : Double.parseDouble(offset));
        }
        return Double.parseDouble(coord);
    }
    
    private static Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.STONE;
        }
    }
    
    private static Transformation parseTransformation(String data) {
        String[] values = data.replace("f", "").split(",");
        if (values.length != 16) {
            return new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(0, 0, 0, 1),
                new Vector3f(1, 1, 1),
                new Quaternionf(0, 0, 0, 1)
            );
        }
        
        float[] matrix = new float[16];
        for (int i = 0; i < 16; i++) {
            matrix[i] = Float.parseFloat(values[i].trim());
        }
        
        Vector3f translation = new Vector3f(matrix[3], matrix[7], matrix[11]);
        Vector3f scale = new Vector3f(
            (float) Math.sqrt(matrix[0] * matrix[0] + matrix[1] * matrix[1] + matrix[2] * matrix[2]),
            (float) Math.sqrt(matrix[4] * matrix[4] + matrix[5] * matrix[5] + matrix[6] * matrix[6]),
            (float) Math.sqrt(matrix[8] * matrix[8] + matrix[9] * matrix[9] + matrix[10] * matrix[10])
        );
        
        Quaternionf leftRotation = new Quaternionf(0, 0, 0, 1);
        Quaternionf rightRotation = new Quaternionf(0, 0, 0, 1);
        
        return new Transformation(translation, leftRotation, scale, rightRotation);
    }
    
    public static List<BlockDisplay> parseMultipleDisplays(String command, Location origin) {
        List<BlockDisplay> displays = new ArrayList<>();
        Matcher passengerMatcher = PASSENGER_PATTERN.matcher(command);
        
        while (passengerMatcher.find()) {
            String blockName = passengerMatcher.group(2);
            String transformData = passengerMatcher.group(3);
            
            Material material = parseMaterial(blockName);
            BlockDisplay display = (BlockDisplay) origin.getWorld().spawnEntity(origin, EntityType.BLOCK_DISPLAY);
            display.setBlock(material.createBlockData());
            
            Transformation transform = parseTransformation(transformData);
            display.setTransformation(transform);
            
            displays.add(display);
        }
        
        return displays;
    }
}
