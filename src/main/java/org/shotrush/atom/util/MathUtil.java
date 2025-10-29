package org.shotrush.atom.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtil {
    
    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
    
    public static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }
    
    public static Vector3f lerp(Vector3f start, Vector3f end, float t) {
        return new Vector3f(
            lerp(start.x, end.x, t),
            lerp(start.y, end.y, t),
            lerp(start.z, end.z, t)
        );
    }
    
    public static Quaternionf slerp(Quaternionf start, Quaternionf end, float t) {
        return new Quaternionf(start).slerp(end, t);
    }
    
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static Vector rotateAroundAxis(Vector vector, Vector axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double dot = vector.dot(axis);
        
        return axis.clone()
            .multiply(dot * (1 - cos))
            .add(vector.clone().multiply(cos))
            .add(axis.clone().crossProduct(vector).multiply(sin));
    }
    
    public static Vector3f rotateAroundY(Vector3f point, float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        
        return new Vector3f(
            point.x * cos - point.z * sin,
            point.y,
            point.x * sin + point.z * cos
        );
    }
    
    public static Vector3f rotateAroundX(Vector3f point, float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        
        return new Vector3f(
            point.x,
            point.y * cos - point.z * sin,
            point.y * sin + point.z * cos
        );
    }
    
    public static Vector3f rotateAroundZ(Vector3f point, float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        
        return new Vector3f(
            point.x * cos - point.y * sin,
            point.x * sin + point.y * cos,
            point.z
        );
    }
    
    public static Matrix4f createTransformMatrix(Vector3f translation, Quaternionf rotation, Vector3f scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.translationRotateScale(translation, rotation, scale);
        return matrix;
    }
    
    public static double distance2D(Location loc1, Location loc2) {
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    public static float easeInQuad(float t) {
        return t * t;
    }
    
    public static float easeOutQuad(float t) {
        return t * (2 - t);
    }
    
    public static float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }
    
    public static float easeInCubic(float t) {
        return t * t * t;
    }
    
    public static float easeOutCubic(float t) {
        float f = t - 1;
        return f * f * f + 1;
    }
    
    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    }
    
    public static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }
}
