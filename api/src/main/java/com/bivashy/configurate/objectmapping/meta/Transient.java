package com.bivashy.configurate.objectmapping.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method should be considered transient, similar to how fields can be marked as transient.
 * Transient methods are excluded from field discovery process.
 *
 * @since 0.0.0
 * @see com.bivashy.configurate.objectmapping.proxy.ProxyMethodFilter
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}
