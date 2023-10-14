package com.bivashy.configurate.objectmapping.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.configurate.objectmapping.FieldDiscoverer;
import org.spongepowered.configurate.serialize.SerializationException;

import com.bivashy.configurate.objectmapping.meta.Transient;
import com.bivashy.configurate.objectmapping.proxy.ProxyMethodFilter;
import com.bivashy.configurate.objectmapping.proxy.ProxyMethodInvoker;

import io.leangen.geantyref.GenericTypeReflector;

final class InterfaceMethodDiscoverer implements FieldDiscoverer<Map<String, Object>> {

    static final InterfaceMethodDiscoverer INSTANCE = InterfaceMethodDiscoverer.defaultBuilder().build();
    private final Collection<ProxyMethodFilter> filters;
    private final Collection<ProxyMethodInvoker> invokers;

    private InterfaceMethodDiscoverer(Builder builder) {
        filters = builder.filters;
        invokers = builder.invokers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder defaultBuilder() {
        return builder().reverseFilter(Method::isSynthetic)
                .reverseFilter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isDefault() || !method.isAnnotationPresent(Transient.class))
                .filter(method -> {
                    if (method.getParameterCount() != 0 && !method.isDefault())
                        throw new SerializationException(method.getDeclaringClass(),
                                "Interface methods should not have parameters: '" + method.toGenericString() + "'");
                    return true;
                });
    }

    @Override
    public <V> InstanceFactory<Map<String, Object>> discover(final AnnotatedType target,
                                                             final FieldCollector<Map<String, Object>, V> collector) throws SerializationException {

        final Class<?> clazz = GenericTypeReflector.erase(target.getType());
        if (!clazz.isInterface())
            return null;

        collectMethods(target, collector);
        for (AnnotatedType superType : getInterfaces(target, clazz)) {
            collectMethods(superType, collector);
        }

        return new ProxyInstanceFactory(clazz, invokers);
    }

    private <V> void collectMethods(AnnotatedType type, final FieldCollector<Map<String, Object>, V> collector) {
        for (Method method : GenericTypeReflector.erase(type.getType()).getDeclaredMethods()) {
            final String name = method.getName();

            if (shouldBeIgnored(method))
                continue;

            final AnnotatedType returnType = GenericTypeReflector.getReturnType(method, type);
            final ProxyMethodSerializer<V> methodSerializer = new ProxyMethodSerializer<>(method, name);
            collector.accept(name, returnType, method, methodSerializer, methodSerializer);
        }
    }

    private List<AnnotatedType> getInterfaces(AnnotatedType parentType, Type type) {
        Class<?>[] interfaces = GenericTypeReflector.erase(type).getInterfaces();
        return Stream.of(interfaces)
                .map(interfaceType -> GenericTypeReflector.getExactSuperType(parentType, interfaceType))
                .flatMap(superType -> Stream.concat(Stream.of(superType), getInterfaces(parentType, superType.getType()).stream()))
                .collect(Collectors.toList());
    }

    private boolean shouldBeIgnored(Method method) {
        return filters.stream().anyMatch(filter -> {
            try {
                return !filter.test(method);
            } catch (SerializationException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public static final class Builder {

        private final Collection<ProxyMethodFilter> filters = new ArrayList<>();
        private final Collection<ProxyMethodInvoker> invokers = new ArrayList<>();

        private Builder() {
        }

        public Builder filter(ProxyMethodFilter filter) {
            filters.add(filter);
            return this;
        }

        public Builder reverseFilter(ProxyMethodFilter filter) {
            filters.add(filter.reverse());
            return this;
        }

        public Builder invoker(ProxyMethodInvoker invoker) {
            invokers.add(invoker);
            return this;
        }

        public Builder invoker(Class<? extends Annotation> annotation, ProxyMethodInvoker invoker) {
            invokers.add(invoker.annotated(annotation));
            return this;
        }

        public InterfaceMethodDiscoverer build() {
            return new InterfaceMethodDiscoverer(this);
        }

    }

}
