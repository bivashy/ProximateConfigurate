package com.bivashy.configurate.objectmapping.common;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

import org.spongepowered.configurate.objectmapping.FieldData;
import org.spongepowered.configurate.util.CheckedFunction;

class ProxyMethodSerializer<V> implements FieldData.Deserializer<Map<String, Object>>, CheckedFunction<V, Object, Exception> {

    private final Method method;
    private final String name;

    public ProxyMethodSerializer(Method method, String name) {
        this.method = method;
        this.name = name;
    }

    @Override
    public void accept(Map<String, Object> intermediate, Object newValue, Supplier<Object> implicitInitializer) {
        if (newValue != null) {
            intermediate.put(name, newValue);
        } else {
            intermediate.put(name, implicitInitializer.get());
        }
    }

    @Override
    public Object apply(V obj) throws Exception {
        return method.invoke(obj);
    }

}