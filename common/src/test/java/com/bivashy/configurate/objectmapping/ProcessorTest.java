package com.bivashy.configurate.objectmapping;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.*;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;

import com.bivashy.configurate.objectmapping.common.InterfaceObjectMapperFactory;
import com.bivashy.configurate.objectmapping.common.meta.Processors;
import com.bivashy.configurate.objectmapping.meta.Comment;

class ProcessorTest {

    private ObjectMapper.Factory objectMapperFactory() {
        return new InterfaceObjectMapperFactory();
    }

    // Comments

    public interface TestComment {
        @Comment("An important option") String first();
        @Comment("Another important option!") String second();
    }

    @Test
    void testComment() throws SerializationException {
        final ObjectMapper<TestComment> mapper = objectMapperFactory().get(TestComment.class);
        final TestComment object = new TestComment() {
            @Override
            public String first() {
                return "hello";
            }

            @Override
            public String second() {
                return "world";
            }
        };
        final CommentedConfigurationNode target = CommentedConfigurationNode.root();

        mapper.save(object, target);

        assertEquals("An important option", target.node("first").comment());
        assertEquals("Another important option!", target.node("second").comment());
    }

    public interface TestCommentLocalized {
        @Comment("configurate.test.comment.one")
        default int hello() {
            return 1;
        }
        @Comment("Missing comment passthrough")
        default int goodbye() {
            return 2;
        }
    }

    // Localized comments

    @Test
    void testCommentLocalized() throws SerializationException {
        final ResourceBundle bundle = ResourceBundle.getBundle("com.bivashy.configurate.objectmapping.messages", new Locale("en", "US"));
        final ObjectMapper<TestCommentLocalized> mapper = InterfaceObjectMapperFactory.factoryBuilder()
                .addProcessor(Comment.class, Processors.localizedComments(bundle))
                .build().get(TestCommentLocalized.class);

        final CommentedConfigurationNode target = CommentedConfigurationNode.root();
        mapper.save(new TestCommentLocalized(){}, target);

        assertEquals("First property", target.node("hello").comment());
        assertEquals("Missing comment passthrough", target.node("goodbye").comment());
    }

}
