package com.bivashy.configurate.objectmapping.common.meta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.bivashy.configurate.objectmapping.common.ProxyDefaultMethodInvoker;
import com.bivashy.configurate.objectmapping.proxy.ProxyMethodInvoker;

public class MethodInvokers {

    private MethodInvokers() {
    }

    public static ProxyMethodInvoker toStringInvoker() {
        return (proxy, method, args, intermediate) -> {
            if (!method.getName().equals("toString") || args != null && args.length != 0)
                return null;
            try {
                Method stringifyMethod = findUnderlyingMethod(proxy, "stringify");
                if (!stringifyMethod.isDefault())
                    throw new IllegalStateException("Cannot call 'stringify' on '" + Arrays.toString(proxy.getClass().getInterfaces()) +
                            "', because 'stringify' doesn't have any 'default' implementation!");
                return ProxyDefaultMethodInvoker.invokeDefaultMethod(proxy, stringifyMethod, new Object[0]);
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
            if (!method.getName().equals("equals") || args == null || args.length != 1 || method.getParameterTypes()[0] != Object.class)
                return null;
            Object object = args[0];
            try {
                Method equalToMethod = findUnderlyingMethod(proxy, "equalTo", Object.class);
                if (!equalToMethod.isDefault())
                    throw new IllegalStateException("Cannot call 'equalTo' on '" + Arrays.toString(proxy.getClass().getInterfaces()) +
                            "', because 'equalTo' doesn't have any 'default' implementation!");
                return ProxyDefaultMethodInvoker.invokeDefaultMethod(proxy, equalToMethod, new Object[]{object});
            } catch (NoSuchMethodException e) {
                if (proxy == object)
                    return true;
                if (!(proxy.getClass().equals(object.getClass())))
                    return false;
                Collection<Object> objectFields = fields(object, intermediate.keySet());
                Collection<Object> proxyFields = fields(proxy, intermediate.keySet());
                return objectFields.size() == proxyFields.size() && objectFields.containsAll(proxyFields) && proxyFields.containsAll(objectFields);
            }
        };
    }

    public static ProxyMethodInvoker hashCodeInvoker() {
        return (proxy, method, args, intermediate) -> {
            if (!method.getName().equals("hashCode") || args != null && args.length != 0)
                return null;
            try {
                Method hashMethod = findUnderlyingMethod(proxy, "hash");
                if (!hashMethod.isDefault())
                    throw new IllegalStateException("Cannot call 'hash' on '" + Arrays.toString(proxy.getClass().getInterfaces()) +
                            "', because 'hash' doesn't have any 'default' implementation!");
                return ProxyDefaultMethodInvoker.invokeDefaultMethod(proxy, hashMethod, new Object[0]);
            } catch (NoSuchMethodException e) {
                return Objects.hash(fields(proxy, intermediate.keySet()));
            }
        };
    }

    static Method findUnderlyingMethod(Object proxy, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        for (Class<?> anInterface : proxy.getClass().getInterfaces()) {
            try {
                return anInterface.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException();
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
