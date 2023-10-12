package com.bivashy.configurate.objectmapping.meta;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copy of {@link org.spongepowered.configurate.objectmapping.meta.Required} with {@code ElementType.METHOD} as target.
 * Indicates that a method is required.
 *
 * <p>Loading will fail if this method has a null value.</p>
 *
 * @since 0.0.0
 * @see org.spongepowered.configurate.objectmapping.meta.Required
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@SubtypeOf(NonNull.class)
public @interface Required {
}