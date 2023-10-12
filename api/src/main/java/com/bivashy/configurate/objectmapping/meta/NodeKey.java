package com.bivashy.configurate.objectmapping.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copy of {@link org.spongepowered.configurate.objectmapping.meta.NodeKey} with {@code ElementType.METHOD} as target.
 * Marks a method that gets its value from the node's key.
 *
 * <p>Methods annotated as such should be treated as <em>read-only</em>.</p>
 *
 * @since 0.0.0
 * @see org.spongepowered.configurate.objectmapping.meta.NodeKey
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface NodeKey {

}