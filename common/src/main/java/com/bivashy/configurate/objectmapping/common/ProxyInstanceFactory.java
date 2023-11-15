package com.bivashy.configurate.objectmapping.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.spongepowered.configurate.objectmapping.FieldDiscoverer.InstanceFactory;
import org.spongepowered.configurate.util.Types;

import com.bivashy.configurate.objectmapping.proxy.ProxyMethodInvoker;

class ProxyInstanceFactory implements InstanceFactory<Map<String, Object>> {

    private final Class<?> clazz;
    private final Collection<ProxyMethodInvoker> invokers;

    public ProxyInstanceFactory(Class<?> clazz, Collection<ProxyMethodInvoker> invokers) {
        this.clazz = clazz;
        this.invokers = invokers;
    }

    @Override
    public Map<String, Object> begin() {
        return new HashMap<>();
    }

    @Override
    public Object complete(Map<String, Object> intermediate) {
        return createProxy((proxy, method, args) -> {
            Object intermediateValue = intermediate.get(method.getName());
            Optional<Object> invocationResult = chooseInvocationResult(proxy, method, args, intermediate);
            if (invocationResult.isPresent()) {
                return invocationResult.get();
            } else if (intermediateValue == null && method.isDefault()) {
                return ProxyDefaultMethodInvoker.invokeDefaultMethod(proxy, method, args);
            }

            if(intermediateValue == null && method.getReturnType().isPrimitive())
                return intermediate.computeIfAbsent(method.getName(), (ignored) -> Types.defaultValue(method.getReturnType()));

            return intermediateValue;
        });
    }

    @Override
    public boolean canCreateInstances() {
        return true;
    }

    private Object createProxy(InvocationHandler invocationHandler) {
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, invocationHandler);
    }

    private Optional<Object> chooseInvocationResult(Object proxy, Method method, Object[] args, Map<String, Object> intermediateValue) {
        return invokers.stream()
                .map(invoker -> {
                    try {
                        return invoker.invoke(proxy, method, args, intermediateValue);
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

}
