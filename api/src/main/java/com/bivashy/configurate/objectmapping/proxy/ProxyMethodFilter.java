package com.bivashy.configurate.objectmapping.proxy;

import java.lang.reflect.Method;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Represents a filter that determines if a given method should be processed or ignored by the field discoverer.
 *
 * <p>Instances of this interface can be used to filter out specific types of methods, such as synthetic or static methods.</p>
 */
public interface ProxyMethodFilter {

    /**
     * Evaluates the given method to determine if it should be processed or ignored.
     *
     * <p>For example, to ignore <b>synthetic</b> methods, one can use:</p>
     * <pre>
     * ProxyMethodFilter filter = method -> !method.isSynthetic();
     * </pre>
     *
     * <p>In the above example, the filter will exclude synthetic methods from further processing.</p>
     *
     * @param method The method from the interface to evaluate.
     * @return {@code true} if the method should be processed further, {@code false} if it should be ignored.
     * @throws SerializationException If the method has invalid syntax or cannot be evaluated.
     */
    boolean test(Method method) throws SerializationException;

    /**
     * Returns a new {@code ProxyMethodFilter} that negates the result of this filter.
     *
     * <p>For instance, if the current filter allows a method, the reversed filter will ignore it, and vice versa.</p>
     *
     * @return A new filter that negates the result of this filter.
     */
    default ProxyMethodFilter reverse() {
        return (method) -> !this.test(method);
    }
}
