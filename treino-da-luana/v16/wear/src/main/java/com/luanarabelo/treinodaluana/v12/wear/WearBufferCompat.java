package com.luanarabelo.treinodaluana.v12.wear;

import java.lang.reflect.Method;

/** Compatibilidade binaria dos buffers do Google Play Services no Wear OS. */
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
