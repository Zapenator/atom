package org.shotrush.atom.display;

import lombok.Getter;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class AnimationBuilder {
    private final Display display;
    private final DisplayEntityManager manager;
    private final List<AnimationStep> steps;
    private EasingFunction easing;
    private Consumer<Display> onComplete;
    
    public AnimationBuilder(Display display, DisplayEntityManager manager) {
        this.display = display;
        this.manager = manager;
        this.steps = new ArrayList<>();
        this.easing = EasingFunction.LINEAR;
    }
    
    public AnimationBuilder rotate(float pitch, float yaw, float roll, int durationTicks) {
        steps.add(new AnimationStep(AnimationType.ROTATE, new float[]{pitch, yaw, roll}, durationTicks));
        return this;
    }
    
    public AnimationBuilder scale(float x, float y, float z, int durationTicks) {
        steps.add(new AnimationStep(AnimationType.SCALE, new float[]{x, y, z}, durationTicks));
        return this;
    }
    
    public AnimationBuilder translate(float x, float y, float z, int durationTicks) {
        steps.add(new AnimationStep(AnimationType.TRANSLATE, new float[]{x, y, z}, durationTicks));
        return this;
    }
    
    public AnimationBuilder stretch(float factor, int durationTicks) {
        steps.add(new AnimationStep(AnimationType.SCALE, new float[]{factor, factor, factor}, durationTicks));
        return this;
    }
    
    public AnimationBuilder wait(int ticks) {
        steps.add(new AnimationStep(AnimationType.WAIT, new float[]{}, ticks));
        return this;
    }
    
    public AnimationBuilder easing(EasingFunction easing) {
        this.easing = easing;
        return this;
    }
    
    public AnimationBuilder onComplete(Consumer<Display> callback) {
        this.onComplete = callback;
        return this;
    }
    
    public AnimationState play() {
        AnimationState state = new AnimationState(display, steps, easing, onComplete);
        manager.getAnimations().put(display.getUniqueId(), state);
        state.start();
        return state;
    }
    
    public AnimationBuilder loop() {
        AnimationState state = play();
        state.setLooping(true);
        return this;
    }
    
    public enum AnimationType {
        ROTATE, SCALE, TRANSLATE, WAIT
    }
    
    public record AnimationStep(AnimationType type, float[] values, int duration) {}
    
    public enum EasingFunction {
        LINEAR(t -> t),
        EASE_IN(t -> t * t),
        EASE_OUT(t -> t * (2 - t)),
        EASE_IN_OUT(t -> t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t),
        ELASTIC(t -> {
            if (t == 0 || t == 1) return t;
            float p = 0.3f;
            return (float) (Math.pow(2, -10 * t) * Math.sin((t - p / 4) * (2 * Math.PI) / p) + 1);
        }),
        BOUNCE(t -> {
            if (t < 1 / 2.75f) {
                return 7.5625f * t * t;
            } else if (t < 2 / 2.75f) {
                t -= 1.5f / 2.75f;
                return 7.5625f * t * t + 0.75f;
            } else if (t < 2.5f / 2.75f) {
                t -= 2.25f / 2.75f;
                return 7.5625f * t * t + 0.9375f;
            } else {
                t -= 2.625f / 2.75f;
                return 7.5625f * t * t + 0.984375f;
            }
        });
        
        private final java.util.function.Function<Float, Float> function;
        
        EasingFunction(java.util.function.Function<Float, Float> function) {
            this.function = function;
        }
        
        public float apply(float t) {
            return function.apply(t);
        }
    }
}
