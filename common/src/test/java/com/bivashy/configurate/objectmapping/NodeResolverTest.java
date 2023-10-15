package com.bivashy.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.*;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.serialize.SerializationException;

import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;
import com.bivashy.configurate.objectmapping.meta.NodeKey;
import com.bivashy.configurate.objectmapping.meta.Setting;

class NodeResolverTest {

    private ObjectMapper.Factory objectMapperFactory() {
        return new InterfaceObjectMapperFactory();
    }

    private ConfigurationOptions configurationOptions() {
        return ConfigurationOptions.defaults().serializers(opt -> opt.register(InterfaceObjectMapperFactory::applicable, new InterfaceObjectMapperFactory()));
    }

    // node key

    public interface TestNodeKey {
        @NodeKey
        String ownKey();
        String own();
    }

    @Test
    void testNodeKey() throws SerializationException {
        final ObjectMapper<TestNodeKey> mapper = objectMapperFactory().get(TestNodeKey.class);
        final BasicConfigurationNode source = BasicConfigurationNode.root().node("test");
        source.node("own").set("yeet");

        final TestNodeKey object = mapper.load(source);

        assertEquals("test", object.ownKey());
        assertEquals("yeet", object.own());
    }

    // key from setting

    public interface TestSettingKey {
        @Setting("something") String notSomething();
    }

    @Test
    void testSettingKey() throws SerializationException {
        final ObjectMapper<TestSettingKey> mapper = objectMapperFactory().get(TestSettingKey.class);

        final BasicConfigurationNode source = BasicConfigurationNode.root(n -> {
            n.node("something").raw("blah");
        });

        final TestSettingKey object = mapper.load(source);

        assertEquals("blah", object.notSomething());
    }

    // only with annotation (setting.class in this case)

    public interface TestOnlyWithAnnotation {
        @Setting String marked();
        String notProcessed();
    }

    @Test
    void testOnlyWithAnnotation() throws SerializationException {
        final ObjectMapper<TestOnlyWithAnnotation> mapper = InterfaceObjectMapperFactory.factoryBuilder()
                .addNodeResolver(NodeResolver.onlyWithAnnotation(Setting.class))
                .build().get(TestOnlyWithAnnotation.class);

        final BasicConfigurationNode source = BasicConfigurationNode.root(n -> {
            n.node("marked").raw("something");
            n.node("not-processed").raw("ignored");
        });

        final TestOnlyWithAnnotation object = mapper.load(source);

        assertEquals("something", object.marked());
        assertNull(object.notProcessed());
    }

    @ConfigInterface
    public interface HolderOne {
        default String hello() {
            return "eek";
        }
    }

    @ConfigInterface
    public interface HolderTwo {
        default String skeletons() {
            return "spooky | scary";
        }
    }

    @ConfigInterface
    public interface TestNodeFromParent {
        @Setting(nodeFromParent = true)
        HolderOne one();
        @Setting(nodeFromParent = true) HolderTwo two();
    }

    @Test
    void testNodeFromParentRead() throws SerializationException {
        final ConfigurationNode root = BasicConfigurationNode.root(configurationOptions()
                .nativeTypes(Collections.singleton(String.class)));

        root.node("hello").set("yay");
        root.node("skeletons").set("go clunk");

        final TestNodeFromParent value = root.get(TestNodeFromParent.class);
        assertNotNull(value);

        assertEquals("yay", value.one().hello());
        assertEquals("go clunk", value.two().skeletons());
    }

    @Test
    void testNodeFromParentWritesDefaults() throws SerializationException {
        final ConfigurationNode root = BasicConfigurationNode.root(configurationOptions()
                .nativeTypes(Collections.singleton(String.class))
                .implicitInitialization(true)
                .shouldCopyDefaults(true));

        root.get(TestNodeFromParent.class);

        assertEquals("eek", root.node("hello").raw());
        assertEquals("spooky | scary", root.node("skeletons").raw());
    }

}
