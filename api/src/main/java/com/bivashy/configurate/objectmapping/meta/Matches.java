package com.bivashy.configurate.objectmapping.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import org.checkerframework.checker.regex.qual.Regex;

/**
 * Copy of {@link org.spongepowered.configurate.objectmapping.meta.Matches} with {@code ElementType.METHOD} as target.
 * Constrains a method value to ensure it matches the provided expression.
 *
 * <p>This constraint will always pass with an empty method. See {@link Required}
 * to enforce a non-null value.</p>
 *
 * @since 0.0.0
 * @see org.spongepowered.configurate.objectmapping.meta.Matches
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Matches {

    /**
     * Pattern to test string value against.
     *
     * @return pattern to test against
     * @since 0.0.0
     */
    @Regex String value();

    /**
     * Flags to pass to the compiled {@link Pattern}.
     *
     * @return the regex pattern parsing flags
     * @see Pattern for the bitflags accepted here
     * @since 0.0.0
     */
    int flags() default 0;

    /**
     * Message to throw in an exception when a match fails.
     *
     * <p>This message will be formatted as a MessageFormat with two
     * parameters:</p>
     * <ol start="0">
     *     <li>the input string</li>
     *     <li>the pattern being matched</li>
     * </ol>
     *
     * @return message format.
     * @since 0.0.0
     */
    String failureMessage() default "";

}