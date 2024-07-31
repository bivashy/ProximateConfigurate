package com.bivashy.configurate;

import com.bivashy.configurate.objectmapping.ConfigInterface;
import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;
import com.bivashy.configurate.objectmapping.meta.Transient;
import org.junit.jupiter.api.*;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;

public class TransientMethodCollisionTest {

    @Test
    void testMethodCollision() throws ConfigurateException {
        InputStream resourceStream = getClass().getResourceAsStream("/basic-usage.conf");
        assertNotNull(resourceStream);
        final CommentedConfigurationNode node = HoconConfigurationLoader.builder()
                .source(() -> new BufferedReader(new InputStreamReader(resourceStream)))
                .defaultOptions(opt ->
                        opt.serializers(builder -> builder
                                .registerAll(TypeSerializerCollection.defaults())
                                .register(InterfaceObjectMapperFactory::applicable, new InterfaceObjectMapperFactory())
                        ))
                .build()
                .load();
        ComplexConfiguration object = node.get(ComplexConfiguration.class);
        assertNotNull(object);
        assertNotNull(object.value());
        assertEquals("test", object.value());
        assertEquals(1, object.value(1));
    }

    @ConfigInterface
    public interface ComplexConfiguration {

        String value();

        @Transient
        default int value(int first) {
            return first;
        }

    }

}
