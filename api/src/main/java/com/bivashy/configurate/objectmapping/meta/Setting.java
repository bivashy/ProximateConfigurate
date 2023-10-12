package com.bivashy.configurate.objectmapping.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.spongepowered.configurate.objectmapping.meta.NodeResolver;

/**
 * Copy of {@link org.spongepowered.configurate.objectmapping.meta.Setting} with {@code ElementType.METHOD} as target.
 *
 * <p>Marks a proxy method to be targeted by the object mapper.</p>
 *
 * <p>Optionally, a path override can be provided.</p>
 *
 * <p>This annotation is not required on methods unless the
 * {@link NodeResolver#onlyWithSetting()} resolver filter has been applied to
 * the loading object mapper.</p>
 *
 * @see org.spongepowered.configurate.objectmapping.meta.Setting
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Setting {

    /**
     * The path this setting is located at.
     *
     * @return the path
     * @since 0.0.0
     */
    String value() default "";

    /**
     * Whether a method should use its containing node for its value.
     *
     * @return whether this method should source its data from the node of
     * its container
     * @since 0.0.0
     */
    boolean nodeFromParent() default false;

}
