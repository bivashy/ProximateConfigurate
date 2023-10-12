package com.bivashy.configurate.objectmapping.common.meta;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helpers for built-in localized processors and constraints.
 */
final class Localization {

    private Localization() {
    }

    /**
     * Get {@code key} from the provided bundle, passing the key through if
     * not found.
     *
     * @param bundle bundle to look in
     * @param key key to find
     * @return localized key, or input
     */
    static String key(final ResourceBundle bundle, final String key) {
        try {
            return bundle.getString(key);
        } catch (final MissingResourceException ex) {
            return key;
        }
    }

}