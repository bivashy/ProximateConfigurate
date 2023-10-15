package com.bivashy.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;
import com.bivashy.configurate.objectmapping.meta.Transient;

public class TransientTest {

    private ObjectMapper.Factory objectMapperFactory() {
        return new InterfaceObjectMapperFactory();
    }

    public interface Calculator {

        int first();

        int second();

        @Transient
        default int calculate() {
            return first() + second();
        }

        default int third() {
            return 4;
        }

    }

    @Test
    void testTransientCalculator() throws SerializationException {
        final ObjectMapper<Calculator> mapper = objectMapperFactory().get(Calculator.class);

        final BasicConfigurationNode calculatorNode = BasicConfigurationNode.root(p -> p.act(n -> {
           n.node("first").raw(3);
           n.node("second").raw(7);
        }));
        Calculator calculator = mapper.load(calculatorNode);
        assertNotNull(calculator);
        assertEquals(3, calculator.first());
        assertEquals(7, calculator.second());
        assertEquals(4, calculator.third());
        assertEquals(10, calculator.calculate());
    }

}
