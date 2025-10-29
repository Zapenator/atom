package org.shotrush.atom.display;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.shotrush.atom.display.AnimationBuilder.*;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class AnimationState {
    private final Display display;
    private final List<AnimationStep> steps;
    private final EasingFunction easing;
    private final Consumer<Display> onComplete;
    @Setter private boolean looping;
    private boolean active;
    private int currentStep;
    private int currentTick;
    private Transformation startTransform;
    private Transformation targetTransform;
    
    public AnimationState(Display display, List<AnimationStep> steps, EasingFunction easing, Consumer<Display> onComplete) {
        this.display = display;
        this.steps = steps;
        this.easing = easing;
        this.onComplete = onComplete;
        this.looping = false;
        this.active = false;
        this.currentStep = 0;
        this.currentTick = 0;
    }
    
    public void start() {
        if (steps.isEmpty()) return;
        active = true;
        currentStep = 0;
        currentTick = 0;
        startTransform = display.getTransformation();
        prepareStep();
    }
    
    public void tick() {
        if (!active || currentStep >= steps.size()) {
            if (looping) {
                start();
            } else {
                stop();
            }
            return;
        }
        
        AnimationStep step = steps.get(currentStep);
        currentTick++;
        
        if (step.type() == AnimationType.WAIT) {
            if (currentTick >= step.duration()) {
                nextStep();
            }
            return;
        }
        
        float progress = Math.min(1.0f, (float) currentTick / step.duration());
        float easedProgress = easing.apply(progress);
        
        Transformation current = interpolateTransform(startTransform, targetTransform, easedProgress);
        display.setTransformation(current);
        
        if (currentTick >= step.duration()) {
            nextStep();
        }
    }
    
    private void prepareStep() {
        if (currentStep >= steps.size()) return;
        
        AnimationStep step = steps.get(currentStep);
        startTransform = display.getTransformation();
        
        switch (step.type()) {
            case ROTATE -> {
                Quaternionf rotation = new Quaternionf().rotateXYZ(
                    (float) Math.toRadians(step.values()[0]),
                    (float) Math.toRadians(step.values()[1]),
                    (float) Math.toRadians(step.values()[2])
                );
                targetTransform = new Transformation(
                    startTransform.getTranslation(),
                    rotation,
                    startTransform.getScale(),
                    startTransform.getRightRotation()
                );
            }
            case SCALE -> targetTransform = new Transformation(
                startTransform.getTranslation(),
                startTransform.getLeftRotation(),
                new Vector3f(step.values()[0], step.values()[1], step.values()[2]),
                startTransform.getRightRotation()
            );
            case TRANSLATE -> targetTransform = new Transformation(
                new Vector3f(step.values()[0], step.values()[1], step.values()[2]),
                startTransform.getLeftRotation(),
                startTransform.getScale(),
                startTransform.getRightRotation()
            );
            case WAIT -> targetTransform = startTransform;
        }
    }
    
    private void nextStep() {
        currentStep++;
        currentTick = 0;
        if (currentStep < steps.size()) {
            prepareStep();
        } else if (!looping) {
            stop();
        }
    }
    
    private Transformation interpolateTransform(Transformation start, Transformation end, float t) {
        Vector3f translation = new Vector3f(
            lerp(start.getTranslation().x, end.getTranslation().x, t),
            lerp(start.getTranslation().y, end.getTranslation().y, t),
            lerp(start.getTranslation().z, end.getTranslation().z, t)
        );
        
        Quaternionf leftRot = new Quaternionf(start.getLeftRotation()).slerp(end.getLeftRotation(), t);
        
        Vector3f scale = new Vector3f(
            lerp(start.getScale().x, end.getScale().x, t),
            lerp(start.getScale().y, end.getScale().y, t),
            lerp(start.getScale().z, end.getScale().z, t)
        );
        
        Quaternionf rightRot = new Quaternionf(start.getRightRotation()).slerp(end.getRightRotation(), t);
        
        return new Transformation(translation, leftRot, scale, rightRot);
    }
    
    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
    
    public void stop() {
        active = false;
        if (onComplete != null) {
            onComplete.accept(display);
        }
    }
    
    public void pause() {
        active = false;
    }
    
    public void resume() {
        active = true;
    }
}
