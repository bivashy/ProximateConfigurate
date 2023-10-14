package com.bivashy.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.*;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;
import com.bivashy.configurate.objectmapping.meta.Comment;
import com.bivashy.configurate.objectmapping.meta.Setting;

import io.leangen.geantyref.TypeToken;

class InterfaceObjectMapperTest {

    @ConfigInterface
    public interface TestObject {
        @Setting("test-key") String stringVal();
    }

    private ObjectMapper.Factory objectMapperFactory() {
        return new InterfaceObjectMapperFactory();
    }

    private ConfigurationOptions configurationOptions() {
        return ConfigurationOptions.defaults().serializers(opt -> opt.register(InterfaceObjectMapperFactory::applicable, new InterfaceObjectMapperFactory()));
    }

    @Test
    void testCreateFromNode() throws SerializationException {
        final ObjectMapper<TestObject> mapper = objectMapperFactory().get(TestObject.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root();
        source.node("test-key").set("some are born great, some achieve greatness, and some have greatness thrust upon them");

        final TestObject obj = mapper.load(source);
        assertEquals("some are born great, some achieve greatness, and some have greatness thrust upon them", obj.stringVal());
    }

    @Test
    void testNullsPreserved() throws SerializationException {
        final ObjectMapper<TestObject> mapper = objectMapperFactory().get(TestObject.class);
        final TestObject obj = mapper.load(BasicConfigurationNode.root());
        assertNull(obj.stringVal());
    }

    @ConfigInterface
    public interface CommentedObject {
        @Setting("commented-key")
        @Comment("You look nice today")
        default String color() {
            return "Some color";
        }
        @Setting("no-comment")
        default String politician() {
            return "Politician?";
        }
    }

    @Test
    void testCommentsApplied() throws SerializationException {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root();
        final ObjectMapper<CommentedObject> mapper = objectMapperFactory().get(CommentedObject.class);
        final CommentedObject obj = mapper.load(node);
        mapper.save(obj, node);
        assertEquals("You look nice today", node.node("commented-key").comment());
        assertNull(node.node("no-comment").comment());
    }

    @ConfigInterface
    public interface FieldNameObject {
        @Setting
        boolean loads();
    }

    @Test
    void testKeyFromFieldName() throws SerializationException {
        final ObjectMapper<FieldNameObject> mapper = objectMapperFactory().get(FieldNameObject.class);
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("loads").set(true);

        final FieldNameObject obj = mapper.load(node);
        assertTrue(obj.loads());
    }

    @ConfigInterface
    public interface ParentObject {
        @Comment("Comment on parent")
        default InnerObject inner() {
            return new InnerObject() {};
        }
    }

    @ConfigInterface
    public interface InnerObject {
        @Comment("Inner object")
        default String test() {
            return "Default value";
        }
    }

    @Test
    void testNestedObjectWithComments() throws SerializationException {
        final CommentedConfigurationNode node = CommentedConfigurationNode.root(configurationOptions().shouldCopyDefaults(true));
        final ObjectMapper<ParentObject> mapper = objectMapperFactory().get(ParentObject.class);
        mapper.load(node);
        assertEquals("Comment on parent", node.node("inner").comment());
        assertTrue(node.node("inner").isMap());
        assertEquals("Default value", node.node("inner", "test").getString());
        assertEquals("Inner object", node.node("inner", "test").comment());
    }

    @ConfigInterface
    public interface ParentInterface {
        @Comment("Something")
        String test();
    }

    @ConfigInterface
    public interface ChildObject extends ParentInterface {
        @Override
        default String test()  {
            return "Default value";
        }
    }

    @ConfigInterface
    public interface ContainingObject {
        @Setting
        default ParentInterface inner() {
            return new ChildObject(){};
        }
        @Setting
        default List<ParentInterface> list() {
            return new ArrayList<>();
        }
    }

    @Test
    void testInterfaceSerialization() throws SerializationException {

        final ChildObject childObject = new ChildObject(){
            @Override
            public String test() {
                return "Changed value";
            }
        };

        final ContainingObject containingObject = new ContainingObject() {
            @Override
            public ChildObject inner() {
                return childObject;
            }

            @Override
            public List<ParentInterface> list() {
                return Collections.singletonList(childObject);
            }
        };

        final CommentedConfigurationNode node = CommentedConfigurationNode.root(configurationOptions());
        final ObjectMapper<ContainingObject> mapper = objectMapperFactory().get(ContainingObject.class);
        mapper.save(containingObject, node);
        final ContainingObject newContainingObject = mapper.load(node);

        // serialization
        assertEquals(1, node.node("list").childrenList().size());
        assertEquals("Changed value", node.node("inner").node("test").getString());
        assertEquals("Changed value", node.node("list").childrenList().get(0).node("test").getString());
        assertEquals("Something", node.node("inner").node("test").comment());
        assertEquals("Something", node.node("list").childrenList().get(0).node("test").comment());

        // deserialization
        assertEquals(1, newContainingObject.list().size());
        assertEquals("Changed value", newContainingObject.inner().test());
        assertEquals("Changed value", newContainingObject.list().get(0).test());
    }

    @ConfigInterface
    public interface GenericSerializable<V> {
        @Setting
        List<V> elements();
    }

    @ConfigInterface
    public interface ParentTypesResolved extends GenericSerializable<URL> {

        @Setting
        default String test() {
            return "hi";
        }

    }

    @Test
    void testGenericTypesResolved() throws SerializationException {
        final TypeToken<GenericSerializable<String>> stringSerializable = new TypeToken<GenericSerializable<String>>() {};
        final TypeToken<GenericSerializable<Integer>> intSerializable = new TypeToken<GenericSerializable<Integer>>() {};

        final ObjectMapper<GenericSerializable<String>> stringMapper = objectMapperFactory().get(stringSerializable);
        final ObjectMapper<GenericSerializable<Integer>> intMapper = objectMapperFactory().get(intSerializable);

        final BasicConfigurationNode stringNode = BasicConfigurationNode.root(p -> {
            p.node("elements").act(n -> {
                n.appendListNode().raw("hello");
                n.appendListNode().raw("world");
            });
        });
        final BasicConfigurationNode intNode = BasicConfigurationNode.root(p -> {
            p.node("elements").act(n -> {
                n.appendListNode().raw(1);
                n.appendListNode().raw(1);
                n.appendListNode().raw(2);
                n.appendListNode().raw(3);
                n.appendListNode().raw(5);
                n.appendListNode().raw(8);
            });
        });

        final GenericSerializable<String> stringObject = stringMapper.load(stringNode);
        assertEquals(Arrays.asList("hello", "world"), stringObject.elements());

        final GenericSerializable<Integer> intObject = intMapper.load(intNode);
        assertEquals(Arrays.asList(1, 1, 2, 3, 5, 8), intObject.elements());
    }

    @Test
    void testGenericsResolvedThroughSuperclass() throws SerializationException, MalformedURLException {
        final ObjectMapper<ParentTypesResolved> mapper = objectMapperFactory().get(ParentTypesResolved.class);

        final BasicConfigurationNode urlNode = BasicConfigurationNode.root(p -> {
            p.node("elements").act(n -> {
                n.appendListNode().raw("https://spongepowered.org");
                n.appendListNode().raw("https://yaml.org");
            });
            p.node("test").raw("bye");
        });

        final ParentTypesResolved resolved = mapper.load(urlNode);
        assertEquals(Arrays.asList(new URL("https://spongepowered.org"), new URL("https://yaml.org")), resolved.elements());
        assertEquals("bye", resolved.test());

    }

    @ConfigInterface
    public interface HandleNonVirtualNulls {
        @Comment("Test")
        @Nullable
        String hello();
    }

    @Test
    void testNullNonVirtualNodeHasNoValue() throws SerializationException {
        final CommentedConfigurationNode value = CommentedConfigurationNode.root(configurationOptions(), n -> {
            n.node("hello").comment("Hi friend!");
        });

        assertFalse(value.node("hello").virtual());
        assertTrue(value.node("hello").isNull());

        final HandleNonVirtualNulls deserialized = value.require(HandleNonVirtualNulls.class);
        assertNull(deserialized.hello());
    }

}
