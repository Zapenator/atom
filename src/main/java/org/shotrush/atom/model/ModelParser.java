package org.shotrush.atom.model;

import org.bukkit.Material;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelParser {
    private static final Pattern SUMMON_PATTERN = Pattern.compile("/summon\\s+(\\w+)\\s+([~\\d.-]+)\\s+([~\\d.-]+)\\s+([~\\d.-]+)\\s+\\{(.+)\\}");
    private static final Pattern BLOCK_DISPLAY_PATTERN = Pattern.compile("\\{id:\"minecraft:block_display\",block_state:\\{Name:\"minecraft:([\\w_]+)\"(?:,Properties:\\{([^}]*)\\})?\\},transformation:\\[([\\d.f,-]+)\\]\\}");
    private static final Pattern ROOT_BLOCK_PATTERN = Pattern.compile("block_state:\\{Name:\"minecraft:([\\w_]+)\"(?:,Properties:\\{([^}]*)\\})?\\}");
    
    public static DisplayModel parseFromCommand(String command, String modelId, String modelName) {
        DisplayModel model = new DisplayModel(modelId, modelName);
        
        Matcher summonMatcher = SUMMON_PATTERN.matcher(command);
        if (!summonMatcher.find()) {
            throw new IllegalArgumentException("Invalid summon command format");
        }
        
        String nbt = summonMatcher.group(5);
        
        Matcher blockMatcher = BLOCK_DISPLAY_PATTERN.matcher(nbt);
        while (blockMatcher.find()) {
            String blockName = blockMatcher.group(1);
            String properties = blockMatcher.group(2);
            String transformData = blockMatcher.group(3);
            
            DisplayModel.DisplayPart part = new DisplayModel.DisplayPart();
            part.setType("block_display");
            part.setMaterial(parseMaterial(blockName));
            String cleanProperties = "";
            if (properties != null && !properties.isEmpty()) {
                cleanProperties = properties
                    .replace("\"", "")
                    .replace(":", "=");
            }
            part.setBlockState(cleanProperties);
            part.setTransform(parseTransformation(transformData));
            parseTags(nbt, part);
            part.getTags().add("collision_" + java.util.UUID.randomUUID().toString());
            
            model.getParts().add(part);
        }
        
        int itemDisplayIndex = 0;
        while ((itemDisplayIndex = nbt.indexOf("{id:\"minecraft:item_display\",item:{", itemDisplayIndex)) != -1) {
            try {
                int itemStart = nbt.indexOf("item:{", itemDisplayIndex) + 5;
                int braceCount = 1;
                int itemEnd = itemStart;
                
                while (braceCount > 0 && itemEnd < nbt.length()) {
                    itemEnd++;
                    if (nbt.charAt(itemEnd) == '{') braceCount++;
                    else if (nbt.charAt(itemEnd) == '}') braceCount--;
                }
                
                String itemData = nbt.substring(itemStart + 1, itemEnd);
                
                int transformStart = nbt.indexOf("transformation:[", itemEnd) + 16;
                int transformEnd = nbt.indexOf("]", transformStart);
                String transformData = nbt.substring(transformStart, transformEnd);
                
                DisplayModel.DisplayPart part = new DisplayModel.DisplayPart();
                part.setType("item_display");
                part.setItemNbt(itemData);
                part.setMaterial(parseItemMaterial(itemData));
                part.setTransform(parseTransformation(transformData));
                parseTags(nbt, part);
                part.getTags().add("collision_" + java.util.UUID.randomUUID().toString());
                
                model.getParts().add(part);
                System.out.println("Parsed item display: " + part.getMaterial() + " with " + itemData.length() + " chars of NBT");
                itemDisplayIndex = transformEnd;
            } catch (Exception e) {
                System.err.println("Failed to parse item display at index " + itemDisplayIndex + ": " + e.getMessage());
                itemDisplayIndex++;
            }
        }
        
        model.getMetadata().setPartCount(model.getParts().size());
        model.setBoundingBox(calculateBoundingBox(model));
        return model;
    }
    
    private static Material parseMaterial(String name) {
        try {
            String materialName = name.toUpperCase().replace("MINECRAFT:", "");
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to parse material: " + name);
            return Material.STONE;
        }
    }
    
    private static Material parseItemMaterial(String itemData) {
        Pattern idPattern = Pattern.compile("id:\"minecraft:([\\w_]+)\"");
        Matcher matcher = idPattern.matcher(itemData);
        if (matcher.find()) {
            return parseMaterial(matcher.group(1));
        }
        return Material.STONE;
    }
    
    private static void parseTags(String nbt, DisplayModel.DisplayPart part) {
        java.util.regex.Pattern tagPattern = java.util.regex.Pattern.compile("Tags:\\[([^\\]]+)\\]");
        java.util.regex.Matcher tagMatcher = tagPattern.matcher(nbt);
        
        if (tagMatcher.find()) {
            String tagsStr = tagMatcher.group(1);
            String[] tags = tagsStr.split(",");
            for (String tag : tags) {
                String cleanTag = tag.trim().replace("\"", "");
                if (!cleanTag.isEmpty()) {
                    part.getTags().add(cleanTag);
                }
            }
        }
    }
    
    private static DisplayModel.TransformData parseTransformation(String data) {
        DisplayModel.TransformData transform = new DisplayModel.TransformData();
        
        String[] values = data.replace("f", "").split(",");
        if (values.length != 16) {
            return transform;
        }
        
        float[] matrix = new float[16];
        for (int i = 0; i < 16; i++) {
            matrix[i] = Float.parseFloat(values[i].trim());
        }
        transform.setMatrix(matrix);
        
        return transform;
    }
    
    private static DisplayModel.BoundingBox calculateBoundingBox(DisplayModel model) {
        DisplayModel.BoundingBox bbox = new DisplayModel.BoundingBox();
        
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE, maxZ = Float.MIN_VALUE;
        
        for (DisplayModel.DisplayPart part : model.getParts()) {
            if (part.getTransform() != null && part.getTransform().getMatrix() != null) {
                float[] m = part.getTransform().getMatrix();
                

                float x = m[3];
                float y = m[7];
                float z = m[11];
                float scaleX = (float) Math.sqrt(m[0]*m[0] + m[4]*m[4] + m[8]*m[8]);
                float scaleY = (float) Math.sqrt(m[1]*m[1] + m[5]*m[5] + m[9]*m[9]);
                float scaleZ = (float) Math.sqrt(m[2]*m[2] + m[6]*m[6] + m[10]*m[10]);
              
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                minZ = Math.min(minZ, z);
                
                maxX = Math.max(maxX, x + scaleX);
                maxY = Math.max(maxY, y + scaleY);
                maxZ = Math.max(maxZ, z + scaleZ);
            }
        }
        
        bbox.setMinX(minX);
        bbox.setMinY(minY);
        bbox.setMaxX(maxX);
        bbox.setMaxY(maxY);
        bbox.setMaxZ(maxZ);
        
        return bbox;
    }
    
    private static float[] matrixToQuaternion(float[][] m) {
        float trace = m[0][0] + m[1][1] + m[2][2];
        float[] quat = new float[4];
        
        if (trace > 0) {
            float s = 0.5f / (float) Math.sqrt(trace + 1.0f);
            quat[0] = (m[2][1] - m[1][2]) * s;
            quat[1] = (m[0][2] - m[2][0]) * s;
            quat[2] = (m[1][0] - m[0][1]) * s;
            quat[3] = 0.25f / s;
        } else if (m[0][0] > m[1][1] && m[0][0] > m[2][2]) {
            float s = 2.0f * (float) Math.sqrt(1.0f + m[0][0] - m[1][1] - m[2][2]);
            quat[0] = 0.25f * s;
            quat[1] = (m[0][1] + m[1][0]) / s;
            quat[2] = (m[0][2] + m[2][0]) / s;
            quat[3] = (m[2][1] - m[1][2]) / s;
        } else if (m[1][1] > m[2][2]) {
            float s = 2.0f * (float) Math.sqrt(1.0f + m[1][1] - m[0][0] - m[2][2]);
            quat[0] = (m[0][1] + m[1][0]) / s;
            quat[1] = 0.25f * s;
            quat[2] = (m[1][2] + m[2][1]) / s;
            quat[3] = (m[0][2] - m[2][0]) / s;
        } else {
            float s = 2.0f * (float) Math.sqrt(1.0f + m[2][2] - m[0][0] - m[1][1]);
            quat[0] = (m[0][2] + m[2][0]) / s;
            quat[1] = (m[1][2] + m[2][1]) / s;
            quat[2] = 0.25f * s;
            quat[3] = (m[1][0] - m[0][1]) / s;
        }
        
        return quat;
    }
}
