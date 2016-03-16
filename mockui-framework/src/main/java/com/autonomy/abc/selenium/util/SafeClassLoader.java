package com.autonomy.abc.selenium.util;

public class SafeClassLoader<T> implements Factory<T> {
    private Class<T> tClass;
    private final String className;

    public SafeClassLoader(Class<T> tClass, String className) {
        this.tClass = tClass;
        this.className = className;
    }

    public T create() {
        try {
            return tClass.cast(Class.forName(className).newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalStateException("Could not create instance of " + tClass + ". \nCheck that " + className + " is on the classpath and accessible\n" + e.toString(), e);
        }
    }
}
