package com.bivashy.configurate.objectmapping.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Copy of {@link org.spongepowered.configurate.objectmapping.meta.Comment} with {@code ElementType.METHOD} as target.
 * A comment that will be applied to a configuration node if possible.
 *
 * <p>By default, this node will not override any user-defined comments.</p>
 *
 * @since 0.0.0
 * @see org.spongepowered.configurate.objectmapping.meta.Comment
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {

    /**
     * The comment to use.
     *
     * @return comment
     * @since 4.0.0
     */
    String value();

    /**
     * Whether or not to override existing comments on a node.
     *
     * @return if we should override.
     * @since 4.0.0
     */
    boolean override() default false;

}