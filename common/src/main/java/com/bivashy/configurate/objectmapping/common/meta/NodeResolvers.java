package com.bivashy.configurate.objectmapping.common.meta;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;

import com.bivashy.configurate.objectmapping.meta.NodeKey;
import com.bivashy.configurate.objectmapping.meta.Setting;

public class NodeResolvers {

    private NodeResolvers() {
    }

    public static NodeResolver.Factory nodeKey() {
        return (name, element) -> {
            if (element.isAnnotationPresent(NodeKey.class)) {
                return node -> BasicConfigurationNode.root(node.options()).raw(node.key());
            }
            return null;
        };
    }

    public static NodeResolver.Factory nodeFromParent() {
        return (name, element) -> {
            final @Nullable Setting setting = element.getAnnotation(Setting.class);
            if (setting != null && setting.nodeFromParent()) {
                return node -> node;
            }
            return null;
        };
    }

    public static NodeResolver.Factory keyFromSetting() {
        return (name, element) -> {
            if (element.isAnnotationPresent(Setting.class)) {
                final String key = element.getAnnotation(Setting.class).value();
                if (!key.isEmpty()) {
                    return node -> node.node(key);
                }
            }
            return null;
        };
    }

}
