package com.bivashy.configurate.objectmapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the given type is capable of being serialized and deserialized
 * by an object mapper.
 *
 * <p>Types with this annotation must be interface.</p>
 *
 * @since 0.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigInterface {

}
