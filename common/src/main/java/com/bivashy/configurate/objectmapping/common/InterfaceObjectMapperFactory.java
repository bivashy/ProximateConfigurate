package com.bivashy.configurate.objectmapping.common;

import static io.leangen.geantyref.GenericTypeReflector.erase;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
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

    private static final String CLASS_KEY = "__class__";
    private final Factory delegate;

    public InterfaceObjectMapperFactory() {
        this(ObjectMapper.factoryBuilder());
    }

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

    public static ObjectMapper.Factory.Builder factoryBuilder() {
        return ObjectMapper.factoryBuilder()
                .addNodeResolver(NodeResolvers.nodeKey())
                .addNodeResolver(NodeResolvers.keyFromSetting())
                .addNodeResolver(NodeResolvers.nodeFromParent())
                .addProcessor(Comment.class, Processors.comments())
                .addConstraint(Matches.class, String.class, Constraints.pattern())
                .addConstraint(Required.class, Constraints.required())
                .addDiscoverer(InterfaceMethodDiscoverer.INSTANCE);
    }

    public static boolean applicable(Type type) {
        return GenericTypeReflector.annotate(type).isAnnotationPresent(ConfigInterface.class);
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
        if (obj == null) {
            final ConfigurationNode clazz = node.node(CLASS_KEY);
            node.set(null);
            if (!clazz.virtual()) {
                node.node(CLASS_KEY).set(clazz);
            }
            return;
        }
        final Class<?> rawType = erase(type);
        final ObjectMapper<?> mapper;
        if (!rawType.isInterface() && Modifier.isAbstract(rawType.getModifiers())) {
            // serialize obj's concrete type rather than the interface/abstract class
            node.node(CLASS_KEY).set(obj.getClass().getName());
            mapper = get(obj.getClass());
        } else {
            mapper = get(type);
        }
        ((ObjectMapper<Object>) mapper).save(obj, node);
    }

    @Override
    public @Nullable Object emptyValue(final Type specificType, final ConfigurationOptions options) {
        try {
            // preserve options, but don't copy defaults into temporary node
            return get(specificType).load(BasicConfigurationNode.root(options.shouldCopyDefaults(false)));
        } catch (final SerializationException ex) {
            return null;
        }
    }

}
