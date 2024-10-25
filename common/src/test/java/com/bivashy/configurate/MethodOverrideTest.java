package com.bivashy.configurate;

import com.bivashy.configurate.objectmapping.ConfigInterface;
import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;
import com.bivashy.configurate.objectmapping.meta.Transient;
import org.junit.jupiter.api.*;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MethodOverrideTest {

    private final ConfigurationNode node = loadConfiguration();

    static ConfigurationNode loadConfiguration() {
        InputStream resourceStream = ObjectContractTest.class.getResourceAsStream("/inheritance.conf");
        assertNotNull(resourceStream);
        try {
            return HoconConfigurationLoader.builder()
                    .source(() -> new BufferedReader(new InputStreamReader(resourceStream)))
                    .defaultOptions(opt ->
                            opt.serializers(builder -> builder
                                    .registerAll(TypeSerializerCollection.defaults())
                                    .register(InterfaceObjectMapperFactory::applicable, new InterfaceObjectMapperFactory())
                            ))
                    .build()
                    .load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSimpleOverride() throws SerializationException {
        AnimalConfig animalConfig = node.node("animal").get(AnimalConfig.class);
        assertNotNull(animalConfig, "animalConfig");

        assertEquals(1, animalConfig.age());

        assertEquals(animalConfig.age(), animalConfig.getAge());
    }

    @Test
    void testGenericOverride() throws SerializationException {
        AgedAnimalConfig agedAnimalConfig = node.node("animal").get(AgedAnimalConfig.class);
        assertNotNull(agedAnimalConfig, "agedAnimalConfig");

        assertEquals(1, agedAnimalConfig.age());
        assertEquals(agedAnimalConfig.age(), agedAnimalConfig.getAge());
        assertEquals(agedAnimalConfig.possibleColors(), agedAnimalConfig.getPossibleColors());
        assertEquals(agedAnimalConfig.getColor(0), agedAnimalConfig.getPossibleColors().get(0));
    }

    public interface Animal {

        int getAge();

    }
    @ConfigInterface
    public interface AnimalConfig extends Animal {

        @Transient
        @Override
        default int getAge() {
            return age();
        }

        int age();

    }
    public interface Aged<T extends Number, I extends Number> {

        T getAge();

        String getColor(I index);

        List<String> getPossibleColors();

    }
    @ConfigInterface
    public interface AgedAnimalConfig extends Aged<Integer, Integer> {

        @Transient
        @Override
        default Integer getAge() {
            return age();
        }

        @Transient
        @Override
        default String getColor(Integer index) {
            return possibleColors().get(index);
        }

        @Transient
        @Override
        default List<String> getPossibleColors() {
            return possibleColors();
        }

        List<String> possibleColors();

        int age();

    }

}
