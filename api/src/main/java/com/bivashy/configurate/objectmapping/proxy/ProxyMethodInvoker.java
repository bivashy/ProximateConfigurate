package com.bivashy.configurate.objectmapping.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Represents an invoker for proxy methods.
 *
 * <p>Instances of this interface can be used to customize the behavior of method invocation on proxy objects,
 * allowing for advanced processing or filtering based on method properties or annotations.</p>
 */
public interface ProxyMethodInvoker {

    /**
     * Invokes the specified method on a proxy object.
     *
     * <p>This method can be overridden by implementers to provide custom invocation logic,
     * potentially based on the method's annotations, arguments, or other properties.</p>
     *
     * <p>If the return value is {@code null}, this invoker has chosen to "ignore" the method,
     * and the next {@code ProxyMethodInvoker} in the chain (if any) will be utilized.</p>
     *
     * @param proxy The proxy object the method is invoked on.
     * @param method The method to invoke.
     * @param args The arguments to pass to the method.
     * @param intermediate An map that holds 'methodName:value'.
     * @return The result of the method invocation or {@code null} if the method should be ignored.
     * @throws ReflectiveOperationException If there is an error during reflective method invocation.
     */
    Object invoke(Object proxy, Method method, Object[] args, Map<String, Object> intermediate) throws ReflectiveOperationException;

    /**
     * Produces a new {@code ProxyMethodInvoker} that only invokes methods annotated with the specified annotation.
     *
     * <p>If the method doesn't have the specified annotation, it will return {@code null} and won't proceed with the invocation.</p>
     *
     * @param annotation The annotation class to check for.
     * @return A new invoker that filters based on the presence of the specified annotation.
     */
    default ProxyMethodInvoker annotated(Class<? extends Annotation> annotation) {
        return (proxy, method, args, intermediate) -> {
            if (!method.isAnnotationPresent(annotation))
                return null;
            return invoke(proxy, method, args, intermediate);
        };
    }
}
