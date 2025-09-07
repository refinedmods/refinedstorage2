package com.refinedmods.refinedstorage.common.support;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandlingCodec<E> implements Codec<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingCodec.class);

    private final Codec<E> elementCodec;
    private final Supplier<E> fallback;
    private final String errorMessage;

    public ErrorHandlingCodec(final Codec<E> elementCodec, final Supplier<E> fallback, final String errorMessage) {
        this.elementCodec = elementCodec;
        this.fallback = fallback;
        this.errorMessage = errorMessage;
    }

    @Override
    public <T> DataResult<T> encode(final E input, final DynamicOps<T> ops, final T prefix) {
        final ListBuilder<T> builder = ops.listBuilder();
        builder.add(elementCodec.encodeStart(ops, input));
        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<E, T>> decode(final DynamicOps<T> ops, final T input) {
        final DecoderState<T> decoder = new DecoderState<>(ops);
        decoder.accept(input);
        return decoder.build();
    }

    @Override
    public String toString() {
        return "ErrorHandlingCodec[" + elementCodec + ']';
    }

    private class DecoderState<T> {
        private final DynamicOps<T> ops;
        @Nullable
        private E element;

        private DecoderState(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        private void accept(final T value) {
            try {
                final DataResult<Pair<E, T>> elementResult = elementCodec.decode(ops, value);
                elementResult.error().ifPresent(
                    error -> LOGGER.warn("{} {}", errorMessage, error.message())
                );
                elementResult.resultOrPartial().ifPresentOrElse(
                    pair -> this.element = pair.getFirst(),
                    () -> this.element = fallback.get()
                );
            } catch (Exception e) {
                LOGGER.warn("{} {}", errorMessage, e.getMessage());
                this.element = fallback.get();
            }
        }

        private DataResult<Pair<E, T>> build() {
            return DataResult.success(Pair.of(element, ops.empty()));
        }
    }
}
