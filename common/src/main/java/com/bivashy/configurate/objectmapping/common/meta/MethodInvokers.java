package com.bivashy.configurate.objectmapping.common.meta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.bivashy.configurate.objectmapping.common.ProxyDefaultMethodInvoker;
import com.bivashy.configurate.objectmapping.meta.Style;
import com.bivashy.configurate.objectmapping.meta.Transient;
import com.bivashy.configurate.objectmapping.proxy.ProxyMethodInvoker;

public class MethodInvokers {

    private MethodInvokers() {
    }

    public static ProxyMethodInvoker transientDefaultInvoker() {
        return (proxy, method, args, intermediate) -> {
            if(!method.isDefault())
                return null;
            if (!method.isAnnotationPresent(Transient.class))
                return null;
            return ProxyDefaultMethodInvoker.invokeDefaultMethod(proxy, method, args);
        };
    }

    public static ProxyMethodInvoker setterInvoker() {
        return (proxy, method, args, intermediate) -> {
            if (args == null || args.length != 1)
                return null;
            Class<?> returnType = method.getReturnType();
            if (!returnType.equals(proxy.getClass()) && !returnType.equals(void.class))
                return null;
            Object argument = args[0];
            intermediate.put(method.getName(), argument);
            return proxy;
        };
    }

    public static ProxyMethodInvoker toStringInvoker() {
        return (proxy, method, args, intermediate) -> {
            if (methodNotEquals(method, "toString"))
                return null;
            try {
                String methodName = findStyle(proxy).map(Style::toStringName).orElse("stringify");
                return invokeDefaultMethod(proxy, methodName, new Object[0]);
            } catch (NoSuchMethodException e) {
                StringBuilder stringBuilder = new StringBuilder();

                String fields = intermediate.entrySet().stream().map(entry -> {
                    String name = entry.getKey();
                    String value = Objects.toString(entry.getValue());
                    return name + "=" + value;
                }).collect(Collectors.joining(", "));

                String classNames = Arrays.toString(proxy.getClass().getInterfaces());
                return stringBuilder.append(classNames).append("{").append(fields).append("}").toString();
            }
        };
    }

    public static ProxyMethodInvoker equalsInvoker() {
        return (proxy, method, args, intermediate) -> {
            if (methodNotEquals(method, "equals", Object.class))
                return null;
            Object object = args[0];
            try {
                String methodName = findStyle(proxy).map(Style::equalsName).orElse("equalTo");
                return invokeDefaultMethod(proxy, methodName, new Object[]{object}, Object.class);
            } catch (NoSuchMethodException e) {
                if (proxy == object)
                    return true;
                if (object == null || !proxy.getClass().equals(object.getClass()))
                    return false;
                Collection<Object> objectFields = fields(object, intermediate.keySet());
                Collection<Object> proxyFields = fields(proxy, intermediate.keySet());
                return objectFields.size() == proxyFields.size() && objectFields.containsAll(proxyFields) && proxyFields.containsAll(objectFields);
            }
        };
    }

    public static ProxyMethodInvoker hashCodeInvoker() {
        return (proxy, method, args, intermediate) -> {
            if (methodNotEquals(method, "hashCode"))
                return null;
            try {
                String methodName = findStyle(proxy).map(Style::hashCodeName).orElse("hash");
                return invokeDefaultMethod(proxy, methodName, new Object[0]);
            } catch (NoSuchMethodException ez) {
                return Objects.hash(fields(proxy, intermediate.keySet()));
            }
        };
    }

    static Optional<Style> findStyle(Object proxy) {
        Class<?>[] interfaces = proxy.getClass().getInterfaces();
        // Theoretically not possible
        if (interfaces.length == 0)
            return Optional.empty();
        Class<?> clazz = interfaces[0];
        return clazz.isAnnotationPresent(Style.class) ? Optional.of(clazz.getAnnotation(Style.class)) : Optional.empty();
    }

    static Object invokeDefaultMethod(Object proxy, String methodName, Object[] args, Class<?>... parameterTypes) throws ReflectiveOperationException {
        Class<?>[] interfaces = proxy.getClass().getInterfaces();
        // Theoretically not possible
        if (interfaces.length == 0)
            throw new NoSuchMethodException();
        Method method = interfaces[0].getDeclaredMethod(methodName, parameterTypes);
        if (!method.isDefault())
            throw new IllegalStateException("Cannot call '" + methodName + "' on '" + Arrays.toString(proxy.getClass().getInterfaces()) +
                    "', because '" + methodName + "' doesn't have 'default' keyword");
        return ProxyDefaultMethodInvoker.invokeDefaultMethod(proxy, method, args);
    }

    static boolean methodNotEquals(Method method, String methodName, Object... argumentTypes) {
        if (method == null || !method.getName().equals(methodName))
            return true;
        return !Arrays.equals(method.getParameterTypes(), argumentTypes);
    }

    static Collection<Object> fields(Object proxy, Set<String> keys) {
        return keys.stream().map(key -> {
            try {
                return proxy.getClass().getDeclaredMethod(key).invoke(proxy);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }).collect(Collectors.toList());
    }

}
