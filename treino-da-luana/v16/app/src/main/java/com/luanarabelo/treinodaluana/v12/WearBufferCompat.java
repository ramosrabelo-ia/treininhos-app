package com.luanarabelo.treinodaluana.v12;

import java.lang.reflect.Method;

/**
 * Mantem o bytecode compativel com DataItemBuffer/DataEventBuffer reais do
 * Google Play Services, que sao classes e implementam Iterable.
 */
final class WearBufferCompat {
    private WearBufferCompat() {}

    @SuppressWarnings("unchecked")
    static <T> Iterable<T> iterable(Object buffer) {
        return (Iterable<T>) buffer;
    }

    static void release(Object buffer) {
        if (buffer == null) return;
        try {
            Method release = buffer.getClass().getMethod("release");
            release.invoke(buffer);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // A falha ao liberar um buffer nunca pode fechar o aplicativo.
        }
    }
}
