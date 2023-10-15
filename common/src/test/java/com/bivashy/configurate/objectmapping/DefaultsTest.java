package com.bivashy.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.*;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;

/**
 * Tests for application of defaults
 */
class DefaultsTest {

    private ObjectMapper.Factory objectMapperFactory() {
        return new InterfaceObjectMapperFactory();
    }

    public static final ConfigurationOptions IMPLICIT_OPTS = ConfigurationOptions.defaults()
            .serializers(opt -> opt.register(InterfaceObjectMapperFactory::applicable, new InterfaceObjectMapperFactory()))
            .implicitInitialization(true);

    @ConfigInterface
    public interface ImplicitDefaultsOnly {
        List<String> myStrings();
        AnotherThing funTimes();
        int[] items();
    }

    @ConfigInterface
    public interface AnotherThing {

    }

    @Test
    void testFieldsInitialized() throws SerializationException {
        final ImplicitDefaultsOnly instance = objectMapperFactory().get(ImplicitDefaultsOnly.class).load(BasicConfigurationNode.root(IMPLICIT_OPTS));

        assertEquals(Collections.emptyList(), instance.myStrings());
        assertNotNull(instance.funTimes());
        assertNotNull(instance.items());
        assertEquals(0, instance.items().length);
    }

    @Test
    void testImplicitDefaultsSaved() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root(IMPLICIT_OPTS.shouldCopyDefaults(true));
        node.get(ImplicitDefaultsOnly.class);

        this.assertPresentAndEmpty(node.node("my-strings"));
        this.assertPresentAndEmpty(node.node("fun-times"));
        this.assertPresentAndEmpty(node.node("items"));
    }

    private void assertPresentAndEmpty(final ConfigurationNode node) {
        assertFalse(node.virtual());
        assertTrue(node.empty());
    }

}
