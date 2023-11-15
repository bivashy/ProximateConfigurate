package com.bivashy.configurate.objectmapping.common.meta;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import com.bivashy.configurate.objectmapping.proxy.ProxyMethodInvoker;

public class MethodInvokers {

    private MethodInvokers() {
    }

    public static ProxyMethodInvoker toStringInvoker() {
        return (proxy, method, args, intermediate) -> {
            if (method.getName().equals("toString") && (args == null || args.length == 0)) {
                StringBuilder stringBuilder = new StringBuilder();

                String fields = intermediate.entrySet().stream().map(entry -> {
                    String name = entry.getKey();
                    String value = Objects.toString(entry.getValue());
                    return name + "=" + value;
                }).collect(Collectors.joining(", "));

                String classNames = Arrays.toString(proxy.getClass().getInterfaces());
                return stringBuilder.append(classNames).append("{").append(fields).append("}").toString();
            }
            return null;
        };
    }

}
