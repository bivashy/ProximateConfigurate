package com.bivashy.configurate.objectmapping.common;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ProxyDefaultMethodInvoker {

    private ProxyDefaultMethodInvoker() {
    }

    public static Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws ReflectiveOperationException {
        final Class<?> declaringClass = method.getDeclaringClass();

        method.setAccessible(true);

        MethodHandle methodHandle;
        if (javaVersion() <= 8) { // Java 8 and lower
            // Lookup(Class)
            Constructor<Lookup> constructor = Lookup.class
                    .getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);

            methodHandle = constructor.newInstance(declaringClass)
                    .in(declaringClass)
                    .unreflectSpecial(method, declaringClass)
                    .bindTo(proxy);
        } else {  // For Java 9 and above
            // MethodHandles.privateLookupIn(Class, Lookup)
            Method privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, Lookup.class);
            Lookup lookup = (Lookup) privateLookupIn.invoke(null, declaringClass, MethodHandles.lookup());
            methodHandle = lookup.unreflectSpecial(method, declaringClass)
                    .bindTo(proxy);
        }

        try {
            return methodHandle.invokeWithArguments(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static int javaVersion() {
        String versionProperty = System.getProperty("java.specification.version");
        if (versionProperty == null)
            return 6;
        if (versionProperty.startsWith("1.")) // 1.6, 1.7, 1.8
            return Integer.parseInt(versionProperty.substring(2));  // Extracts the 8 from 1.8
        return Integer.parseInt(versionProperty);
    }

}
