package com.bivashy.configurate.objectmapping.common;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.spongepowered.configurate.objectmapping.FieldDiscoverer.InstanceFactory;

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
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (proxy, method, args) -> {
            Object intermediateValue = intermediate.get(method.getName());

            Optional<Object> invocationResult = invokers.stream()
                    .map(invoker -> {
                        try {
                            return invoker.invoke(proxy, method, args, intermediate.get(method.getName()));
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst();

            if (invocationResult.isPresent()) {
                return invocationResult.get();
            } else if (intermediateValue == null && method.isDefault()) {
                return ProxyDefaultMethodInvoker.invokeDefaultMethod(proxy, method, args);
            }

            return intermediateValue;
        });
    }

    @Override
    public boolean canCreateInstances() {
        return true;
    }

}
