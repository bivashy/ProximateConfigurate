package com.bivashy.configurate.objectmapping.common.meta;

import java.util.ResourceBundle;

import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.objectmapping.meta.Processor;

import com.bivashy.configurate.objectmapping.meta.Comment;

public class Processors {

    private Processors() {
    }

    /**
     * Apply comments from {@link Comment} annotation on save.
     *
     * @return a new processor factory
     * @since 0.0.0
     */
    public static Processor.Factory<Comment, Object> comments() {
        return (data, fieldType) -> (value, destination) -> {
            if (destination instanceof CommentedConfigurationNodeIntermediary<?>) {
                final CommentedConfigurationNodeIntermediary<?> commented = (CommentedConfigurationNodeIntermediary<?>) destination;
                if (data.override()) {
                    commented.comment(data.value());
                } else {
                    commented.commentIfAbsent(data.value());
                }
            }
        };
    }

    /**
     * Apply localized comments from {@link Comment} annotation on save.
     *
     * <p>The {@link Comment#value() comment's value} will be treated as a key
     * into {@code source}, resolved to the system default locale. Missing keys
     * will be written literally to node.</p>
     *
     * @param source source bundle for comments
     * @return a new processor factory
     * @since 0.0.0
     */
    public static Processor.Factory<Comment, Object> localizedComments(final ResourceBundle source) {
        return (data, fieldType) -> {
            final String translated = Localization.key(source, data.value());
            return (value, destination) -> {
                if (destination instanceof CommentedConfigurationNodeIntermediary<?>) {
                    final CommentedConfigurationNodeIntermediary<?> commented = (CommentedConfigurationNodeIntermediary<?>) destination;
                    if (data.override()) {
                        commented.comment(translated);
                    } else {
                        commented.commentIfAbsent(translated);
                    }
                }
            };
        };
    }

}
