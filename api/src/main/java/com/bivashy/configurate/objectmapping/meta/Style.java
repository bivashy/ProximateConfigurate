package com.bivashy.configurate.objectmapping.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Defines method style of {@code Model.toString()},{@code Model.equals(Object)},{@code Model.hashCode()} for configuration model.</p>
 *
 * <p>All provided method names is optional.</p>
 *
 * <p>This annotation may be useful if model have conflicting field names, or you want to add custom logic with custom method name</p>
 *
 * @see org.spongepowered.configurate.objectmapping.meta.Setting
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Style {

    /**
     * @return Name for alternative `toString()` name.
     */
    String toStringName() default "stringify";

    /**
     * @return Name for alternative `equals()` name.
     */
    String equalsName() default "equalTo";

    /**
     * @return Name for alternative `hashCode()` name.
     */
    String hashCodeName() default "hash";

}
