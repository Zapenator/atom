package org.shotrush.atom.core.blocks.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a BlockType class for automatic registration
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoRegister {
    /**
     * Priority for registration order (lower = earlier)
     */
    int priority() default 100;
}
