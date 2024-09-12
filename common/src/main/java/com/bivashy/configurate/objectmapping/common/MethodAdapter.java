package com.bivashy.configurate.objectmapping.common;

import io.leangen.geantyref.GenericTypeReflector;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * MethodAdapter wraps a Method to enable comparisons and collections
 * based on method signature (name, return type, parameters), ignoring the declaring class.
 */
final class MethodAdapter {

    private final String name;
    private final Type returnType;
    private final Type[] parameterTypes;

    public MethodAdapter(Method method, Type declaringType) {
        this.name = method.getName();
        this.returnType = GenericTypeReflector.getReturnType(method, declaringType);
        this.parameterTypes = GenericTypeReflector.getParameterTypes(method, declaringType);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof MethodAdapter))
            return false;
        MethodAdapter adapter = (MethodAdapter) object;
        return Objects.equals(name, adapter.name) && Objects.equals(returnType, adapter.returnType) &&
                Objects.deepEquals(parameterTypes, adapter.parameterTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, returnType, Arrays.hashCode(parameterTypes));
    }

}