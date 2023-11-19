package com.bivashy.configurate;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.jupiter.api.*;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import com.bivashy.configurate.objectmapping.ConfigInterface;
import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;
import com.bivashy.configurate.objectmapping.meta.Style;
import com.bivashy.configurate.objectmapping.meta.Transient;

public class InheritanceTest {

    @Test
    void testStyleOrdering() throws ConfigurateException {
        InputStream resourceStream = getClass().getResourceAsStream("/inheritance.conf");
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
        Animal animal = node.node("animal").get(Animal.class);
        Dog dog = node.node("dog").get(Dog.class);
        Labrador labrador = node.node("labrador").get(Labrador.class);
        assertNotNull(animal, "animal");
        assertNotNull(dog, "dog");
        assertNotNull(labrador, "labrador");
        String animalString = animal.toString();
        String dogString = dog.toString();
        String labradorString = labrador.toString();
        assertNotNull(animalString, "animalString");
        assertNotNull(dogString, "dogString");
        assertNotNull(labradorString, "labradorString");
        assertEquals(animalString, "animal stringify");
        assertEquals("[interface com.bivashy.configurate.InheritanceTest$Dog]{nickname=Charlie, age=2}", dogString);
        assertEquals("Labrador{age = 3, nickname = Scout, coatType = black}", labradorString);
    }

    @ConfigInterface
    public interface Animal {

        int age();

        @Transient
        default String stringify() {
            return "animal stringify";
        }

    }
    @ConfigInterface
    @Style(toStringName = "ignored") // Added to test conflict with @Style in Labrador (should not happen)
    public interface Dog extends Animal {

        String nickname();

    }
    @ConfigInterface
    @Style(toStringName = "string")
    public interface Labrador extends Dog {

        String coatType();

        default String string() {
            return "Labrador{" +
                    "age = " + age() + ", " +
                    "nickname = " + nickname() + ", " +
                    "coatType = " + coatType() +
                    "}";
        }

    }

}
