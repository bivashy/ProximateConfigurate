package com.bivashy.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.*;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;
import com.bivashy.configurate.objectmapping.common.meta.Constraints;
import com.bivashy.configurate.objectmapping.meta.Matches;
import com.bivashy.configurate.objectmapping.meta.Required;

class ConstraintTest {

    private ObjectMapper.Factory objectMapperFactory() {
        return new InterfaceObjectMapperFactory();
    }

    // required //

    public interface TestRequired {
        @Nullable UUID optional();
        @Required
        UUID mandatory();
    }

    @Test
    void testRequired() throws SerializationException {
        final ObjectMapper<TestRequired> mapper = objectMapperFactory().get(TestRequired.class);

        assertAll(
                // optional present, required missing
                () -> {
                    assertThrows(SerializationException.class, () -> {
                        mapper.load(BasicConfigurationNode.root(n -> {
                            n.node("optional").raw(UUID.randomUUID().toString());
                        }));
                    });
                },
                // required present, optional missing
                () -> {
                    final UUID expected = UUID.randomUUID();
                    final TestRequired result = mapper.load(BasicConfigurationNode.root(n -> {
                        n.node("mandatory").raw(expected);
                    }));
                    assertEquals(expected, result.mandatory());
                    assertNull(result.optional());
                },
                // both present
                () -> {
                    final UUID optionalVal = UUID.randomUUID();
                    final UUID requiredVal = UUID.randomUUID();
                    final TestRequired result = mapper.load(BasicConfigurationNode.root(n -> {
                        n.node("optional").raw(optionalVal.toString());
                        n.node("mandatory").raw(requiredVal.toString());
                    }));
                    assertEquals(optionalVal, result.optional());
                    assertEquals(requiredVal, result.mandatory());
                }
        );
    }

    // pattern //

    public interface TestPattern {
        @Matches("[a-z]+") String test();
        @Matches(value = "[abc]", flags = Pattern.LITERAL) String flagsTest();
    }

    @Test
    void testPattern() throws SerializationException {
        final ObjectMapper<TestPattern> mapper = objectMapperFactory().get(TestPattern.class);

        assertAll(
                // Empty values are not tested
                () -> mapper.load(BasicConfigurationNode.root()),
                // Valid value loads without error
                () -> {
                    final TestPattern result = mapper.load(BasicConfigurationNode.root(n -> {
                        n.node("test").raw("lowercase");
                    }));
                    assertEquals("lowercase", result.test());
                },
                // Invalid value throws ObjectMappingException
                () -> assertThrows(SerializationException.class, () -> {
                    mapper.load(BasicConfigurationNode.root(n -> {
                        n.node("test").raw("LOUD");
                    }));
                }),
                () -> {
                    final TestPattern result = mapper.load(BasicConfigurationNode.root(n -> {
                        n.node("flags-test").raw("[abc]");
                    }));
                    assertEquals("[abc]", result.flagsTest());
                },
                () -> assertThrows(SerializationException.class, () -> {
                    mapper.load(BasicConfigurationNode.root(n -> {
                        n.node("flags-test").raw("a");
                    }));
                })
        );

    }

    // localized pattern //

    public interface TestLocalizedPattern {
        @Matches(value = "Test", failureMessage = "configurate.test.matchfail") String pattern();
        @Matches(value = "[0-9.+-]+", failureMessage = "Value {0} is non-numeric") String numberLike();
    }

    @Test
    void testLocalizedPattern() throws SerializationException {
        // load a bundle with fixed locale, to avoid regional dependence
        final ResourceBundle bundle = ResourceBundle.getBundle("com.bivashy.configurate.objectmapping.messages", new Locale("en", "US"));

        // Create a mapper from a customized factory that does our localization
        final ObjectMapper<TestLocalizedPattern> mapper = InterfaceObjectMapperFactory.factoryBuilder()
                .addConstraint(Matches.class, String.class, Constraints.localizedPattern(bundle))
                .build()
                .get(TestLocalizedPattern.class);

        assertAll(
                // Matches both
                () -> {
                    final BasicConfigurationNode node = BasicConfigurationNode.root(n -> {
                        n.node("pattern").raw("Test");
                        n.node("number-like").raw("0.0.42+4");
                    });
                    final TestLocalizedPattern result = mapper.load(node);

                    assertEquals("Test", result.pattern());
                    assertEquals("0.0.42+4", result.numberLike());
                },
                // Fails one with localized key
                () -> {
                    final BasicConfigurationNode node = BasicConfigurationNode.root(n -> {
                        n.node("pattern").raw("bad");
                        n.node("number-like").raw("0.0.42+4");
                    });

                    assertEquals("failed for input string \"bad\" against pattern \"Test\"!", assertThrows(SerializationException.class, () -> {
                        mapper.load(node);
                    }).rawMessage());
                },
                // Fails second with non-localized passthrough
                () -> {
                    final BasicConfigurationNode node = BasicConfigurationNode.root(n -> {
                        n.node("pattern").raw("Test");
                        n.node("number-like").raw("invalid");
                    });

                    assertEquals("Value invalid is non-numeric", assertThrows(SerializationException.class, () -> {
                        mapper.load(node);
                    }).rawMessage());
                }
        );
    }

}
