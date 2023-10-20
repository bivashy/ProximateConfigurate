package com.bivashy.configurate;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.bivashy.configurate.objectmapping.ConfigInterface;
import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;

public class BasicUsageTest {

    @Test
    void testConfigurationLoadFromFile() throws ConfigurateException {
        InputStream resourceStream = getClass().getResourceAsStream("/test.conf");
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
        assertEquals("test", object.value());
        assertEquals("This have default value!", object.defaultValue());
        assertNotNull(object.sub());
        assertEquals(1, object.sub().number());
        System.out.println(node.node("sub", "numberList").getList(Double.class));
        assertEquals(Arrays.asList(0.1d, 1.3d, 3.2d), object.sub().numberList());
    }

    @ConfigInterface
    public interface ComplexConfiguration {

        String value();

        default String defaultValue() {
            return "This have default value!";
        }

        SubComplexConfiguration sub();

        @ConfigInterface
        interface SubComplexConfiguration {

            int number();

            List<Double> numberList();

        }

    }

}
