package com.bivashy.configurate.objectmapping.common;

import static io.leangen.geantyref.GenericTypeReflector.erase;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.ObjectMapper.Factory;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.bivashy.configurate.objectmapping.ConfigInterface;
import com.bivashy.configurate.objectmapping.common.meta.Constraints;
import com.bivashy.configurate.objectmapping.common.meta.NodeResolvers;
import com.bivashy.configurate.objectmapping.common.meta.Processors;
import com.bivashy.configurate.objectmapping.meta.Comment;
import com.bivashy.configurate.objectmapping.meta.Matches;
import com.bivashy.configurate.objectmapping.meta.Required;

import io.leangen.geantyref.GenericTypeReflector;

public class InterfaceObjectMapperFactory implements Factory, TypeSerializer<Object> {

    public static final Predicate<Type> IS_ANNOTATED_TARGET = (type) -> GenericTypeReflector.annotate(type).isAnnotationPresent(ConfigInterface.class);
    private static final String CLASS_KEY = "__class__";
    private final Factory delegate;

    public InterfaceObjectMapperFactory(Builder builder) {
        this.delegate = builder
                .addNodeResolver(NodeResolvers.nodeKey())
                .addNodeResolver(NodeResolvers.keyFromSetting())
                .addNodeResolver(NodeResolvers.nodeFromParent())
                .addProcessor(Comment.class, Processors.comments())
                .addConstraint(Matches.class, String.class, Constraints.pattern())
                .addConstraint(Required.class, Constraints.required())
                .addDiscoverer(InterfaceMethodDiscoverer.INSTANCE)
                .build();
    }

    @Override
    public ObjectMapper<?> get(Type type) throws SerializationException {
        return delegate.get(type);
    }

    @Override
    public TypeSerializer<Object> asTypeSerializer() {
        return this;
    }

    @Override
    public Object deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final Type clazz = instantiableType(node, type, node.node(CLASS_KEY).getString());
        return get(clazz).load(node);
    }

    private Type instantiableType(final ConfigurationNode node, final Type type,
                                  final @Nullable String configuredName) throws SerializationException {
        final Type retClass;
        final Class<?> rawType = erase(type);
        if (!rawType.isInterface() && Modifier.isAbstract(rawType.getModifiers())) {
            if (configuredName == null) {
                throw new SerializationException(node, type, "No available configured type for instances of this type");
            } else {
                try {
                    retClass = Class.forName(configuredName);
                } catch (final ClassNotFoundException e) {
                    throw new SerializationException(node, type, "Unknown class of object " + configuredName, e);
                }
                if (!GenericTypeReflector.isSuperType(type, retClass)) {
                    throw new SerializationException(node, type, "Configured type " + configuredName + " does not extend "
                            + rawType.getCanonicalName());
                }
            }
        } else {
            retClass = type;
        }
        return retClass;
    }

    @Override
    public void serialize(Type type, @Nullable Object obj, ConfigurationNode node) throws SerializationException {
        delegate.asTypeSerializer().serialize(type, obj, node);
    }

}