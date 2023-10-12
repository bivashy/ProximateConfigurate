package com.bivashy.configurate.objectmapping.common.meta;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spongepowered.configurate.objectmapping.meta.Constraint;
import org.spongepowered.configurate.serialize.SerializationException;

import com.bivashy.configurate.objectmapping.meta.Matches;

public class Constraints {

    private Constraints() {
    }

    /**
     * Require a value to be present for fields marked with the
     * annotation {@code T}.
     *
     * @param <T> marker annotation type
     * @return new constraint factory
     * @since 0.0.0
     */
    public static <T extends Annotation> Constraint.Factory<T, Object> required() {
        return (data, type) -> value -> {
            if (value == null) {
                throw new SerializationException("A value is required for this field");
            }
        };
    }

    /**
     * Require values to match the {@link Matches#value() pattern} provided.
     *
     * <p>Upon failure, an error message will be taken from the annotation.</p>
     *
     * @return factory providing matching pattern test
     * @since 0.0.0
     */
    public static Constraint.Factory<Matches, String> pattern() {
        return (data, type) -> {
            final Pattern test = Pattern.compile(data.value(), data.flags());
            final MessageFormat format = new MessageFormat(data.failureMessage(), Locale.getDefault());
            return value -> {
                if (value != null) {
                    final Matcher match = test.matcher(value);
                    if (!match.matches()) {
                        throw new SerializationException(format.format(new Object[]{value, data.value()}));
                    }
                }
            };
        };
    }

    /**
     * Require values to match the {@link Matches#value() pattern} provided.
     *
     * <p>Upon failure, an error message will be taken from {@code bundle} with
     * a key defined in the annotation.</p>
     *
     * @param bundle source for localized messages
     * @return factory providing matching pattern test
     * @since 0.0.0
     */
    public static Constraint.Factory<Matches, String> localizedPattern(final ResourceBundle bundle) {
        return (data, type) -> {
            final Pattern test = Pattern.compile(data.value(), data.flags());
            final MessageFormat format = new MessageFormat(Localization.key(bundle, data.failureMessage()), bundle.getLocale());
            return value -> {
                if (value != null) {
                    final Matcher match = test.matcher(value);
                    if (!match.matches()) {
                        throw new SerializationException(format.format(new Object[]{value, data.value()}));
                    }
                }
            };
        };
    }

}
