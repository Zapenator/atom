package org.shotrush.atom.example;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.shotrush.atom.Atom;
import org.shotrush.atom.display.AnimationBuilder;
import org.shotrush.atom.display.DisplayGroup;

@CommandAlias("spawnstructure|structure")
@Description("Spawn example rotating structure")
public class RotatingStructureExample extends BaseCommand {
    
    @Default
    @CommandPermission("atom.structure.spawn")
    public void onSpawn(Player player) {
        createRotatingStructure(player.getLocation());
        player.sendMessage("§aCreated rotating structure!");
    }
    
    @Subcommand("fast")
    @CommandPermission("atom.structure.spawn")
    public void onSpawnFast(Player player) {
        Location loc = player.getLocation();
        createRotatingStructure(loc);
        var dm = Atom.getInstance().getDisplayManager();
        dm.getDisplayGroups().asMap().values().stream()
            .filter(g -> g.getOrigin().distance(loc) < 5)
            .findFirst()
            .ifPresent(group -> {
                Atom.getInstance().getSchedulerManager().runGlobalTimer(() -> {
                    group.rotate(18);
                }, 1, 1);
            });
        player.sendMessage("§aCreated fast rotating structure!");
    }
    
    @Subcommand("slow")
    @CommandPermission("atom.structure.spawn")
    public void onSpawnSlow(Player player) {
        Location loc = player.getLocation();
        createRotatingStructure(loc);
        var dm = Atom.getInstance().getDisplayManager();
        dm.getDisplayGroups().asMap().values().stream()
            .filter(g -> g.getOrigin().distance(loc) < 5)
            .findFirst()
            .ifPresent(group -> {
                Atom.getInstance().getSchedulerManager().runGlobalTimer(() -> {
                    group.rotate(1.8f);
                }, 1, 1);
            });
        player.sendMessage("§aCreated slow rotating structure!");
    }
    
    @Subcommand("bounce")
    @CommandPermission("atom.structure.spawn")
    public void onSpawnBounce(Player player) {
        Location loc = player.getLocation();
        var dm = Atom.getInstance().getDisplayManager();
        DisplayGroup group = createRotatingStructure(loc);
        
        dm.animate(group.getRootEntity())
            .scale(1.5f, 1.5f, 1.5f, 20)
            .scale(1.0f, 1.0f, 1.0f, 20)
            .easing(AnimationBuilder.EasingFunction.BOUNCE)
            .loop();
        
        player.sendMessage("§aCreated bouncing structure!");
    }
    
    public static DisplayGroup createRotatingStructure(Location origin) {
        var dm = Atom.getInstance().getDisplayManager();
        var im = Atom.getInstance().getInteractionManager();
        
        Location spawnLoc = origin.clone().add(-0.5, -0.5, -0.5);
        DisplayGroup group = dm.createDisplayGroup(spawnLoc);
        
        BlockDisplay root = dm.createBlockDisplay(spawnLoc, Material.COPPER_BLOCK);
        root.setTransformation(createTransform(1, 0, 0, -0.5f, 0, 1, 0, -0.5f, 0, 0, 1, 0));
        group.setRoot(root);
        
        addDisplay(group, spawnLoc, Material.ACACIA_SLAB, 1, 0, 0, -1.5f, 0, 1, 0, -0.25f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.ACACIA_SLAB, 1, 0, 0, 0.5f, 0, 1, 0, -0.25f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.ACACIA_SLAB, 0, 1, 0, -0.25f, -1, 0, 0, 1.5f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.CUT_COPPER, -1, 0, 0, 0.5f, 0, 1, 0, 1.5f, 0, 0, -1, 1);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, 1, 0, 0, 0.5f, 0, 1, 0, 1.5f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.ACACIA_SLAB, 0, 1, 0, -0.25f, -1, 0, 0, -0.5f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, -1, 0, 0, -0.5f, 0, 1, 0, 1.5f, 0, 0, -1, 1);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, -1, 0, 0, -0.5f, 0, -1, 0, 1.5f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.CUT_COPPER, -1, 0, 0, -1.5f, 0, 1, 0, -0.5f, 0, 0, -1, 1);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, 1, 0, 0, -2.5f, 0, -1, 0, 1.5f, 0, 0, -1, 1);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, 1, 0, 0, -2.5f, 0, 1, 0, -1.5f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, -1, 0, 0, -0.5f, 0, 1, 0, -1.5f, 0, 0, -1, 1);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, -1, 0, 0, -0.5f, 0, -1, 0, -1.5f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.CUT_COPPER, -1, 0, 0, 0.5f, 0, 1, 0, -2.5f, 0, 0, -1, 1);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, 1, 0, 0, 0.5f, 0, -1, 0, -1.5f, 0, 0, -1, 1);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, 1, 0, 0, 0.5f, 0, 1, 0, -1.5f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.CUT_COPPER, -1, 0, 0, 2.5f, 0, 1, 0, -0.5f, 0, 0, -1, 1);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, -1, 0, 0, 2.5f, 0, 1, 0, -1.5f, 0, 0, -1, 1);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, -1, 0, 0, 2.5f, 0, -1, 0, 1.5f, 0, 0, 1, 0);
        addDisplay(group, spawnLoc, Material.CUT_COPPER_STAIRS, 1, 0, 0, 0.5f, 0, -1, 0, 1.5f, 0, 0, -1, 1);
        
        Atom.getInstance().getSchedulerManager().runGlobalTimer(() -> {
            group.rotate(3.6f);
        }, 1, 1);
        
        im.attachToGroup(group, 4.0f, 4.0f, (player, interaction) -> {
            player.sendMessage("§6You clicked the rotating structure!");
            group.scaleAll(1.2f, 1.2f, 1.2f, 10);
            Atom.getInstance().getSchedulerManager().runAtLocationDelayed(origin, () -> {
                group.scaleAll(1.0f, 1.0f, 1.0f, 10);
            }, 20);
        });
        
        Atom.getInstance().getSchedulerManager().runAtLocationDelayed(origin, () -> {
            dm.animate(root)
                .rotate(0, 360, 0, 100)
                .easing(AnimationBuilder.EasingFunction.LINEAR)
                .loop();
        }, 100);
        
        return group;
    }
    
    private static void addDisplay(DisplayGroup group, Location base, Material material, 
                                   float m00, float m01, float m02, float m03,
                                   float m10, float m11, float m12, float m13,
                                   float m20, float m21, float m22, float m23) {
        var dm = Atom.getInstance().getDisplayManager();
        BlockDisplay display = dm.createBlockDisplay(base, material);
        display.setTransformation(createTransform(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23));
        group.addDisplay(display);
    }
    
    private static Transformation createTransform(float m00, float m01, float m02, float m03,
                                                 float m10, float m11, float m12, float m13,
                                                 float m20, float m21, float m22, float m23) {
        Vector3f translation = new Vector3f(m03, m13, m23);
        
        float scaleX = (float) Math.sqrt(m00 * m00 + m10 * m10 + m20 * m20);
        float scaleY = (float) Math.sqrt(m01 * m01 + m11 * m11 + m21 * m21);
        float scaleZ = (float) Math.sqrt(m02 * m02 + m12 * m12 + m22 * m22);
        Vector3f scale = new Vector3f(scaleX, scaleY, scaleZ);
        
        float[][] rotMatrix = {
            {m00 / scaleX, m01 / scaleY, m02 / scaleZ},
            {m10 / scaleX, m11 / scaleY, m12 / scaleZ},
            {m20 / scaleX, m21 / scaleY, m22 / scaleZ}
        };
        
        Quaternionf rotation = matrixToQuaternion(rotMatrix);
        
        return new Transformation(
            translation,
            rotation,
            scale,
            new Quaternionf(0, 0, 0, 1)
        );
    }
    
    private static Quaternionf matrixToQuaternion(float[][] m) {
        float trace = m[0][0] + m[1][1] + m[2][2];
        
        if (trace > 0) {
            float s = 0.5f / (float) Math.sqrt(trace + 1.0f);
            return new Quaternionf(
                (m[2][1] - m[1][2]) * s,
                (m[0][2] - m[2][0]) * s,
                (m[1][0] - m[0][1]) * s,
                0.25f / s
            );
        } else if (m[0][0] > m[1][1] && m[0][0] > m[2][2]) {
            float s = 2.0f * (float) Math.sqrt(1.0f + m[0][0] - m[1][1] - m[2][2]);
            return new Quaternionf(
                0.25f * s,
                (m[0][1] + m[1][0]) / s,
                (m[0][2] + m[2][0]) / s,
                (m[2][1] - m[1][2]) / s
            );
        } else if (m[1][1] > m[2][2]) {
            float s = 2.0f * (float) Math.sqrt(1.0f + m[1][1] - m[0][0] - m[2][2]);
            return new Quaternionf(
                (m[0][1] + m[1][0]) / s,
                0.25f * s,
                (m[1][2] + m[2][1]) / s,
                (m[0][2] - m[2][0]) / s
            );
        } else {
            float s = 2.0f * (float) Math.sqrt(1.0f + m[2][2] - m[0][0] - m[1][1]);
            return new Quaternionf(
                (m[0][2] + m[2][0]) / s,
                (m[1][2] + m[2][1]) / s,
                0.25f * s,
                (m[1][0] - m[0][1]) / s
            );
        }
    }
}
