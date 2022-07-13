package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;

import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;

class TransferHelperTest {
    static Stream<Arguments> provideTransfers() {
        return Stream.of(
            Arguments.of(named("partly, space in destination", TransferBuilder.create()
                .amountInSource("A", 100)
                .amountToTransfer("A", 1)
                .amountExpectedToBeTransferred(1)
                .amountExpectedAfterTransferInSource("A", 99)
                .amountExpectedAfterTransferInDestination("A", 1)
                .build())),
            Arguments.of(named("completely, space in destination", TransferBuilder.create()
                .amountInSource("A", 100)
                .amountToTransfer("A", 100)
                .amountExpectedToBeTransferred(100)
                .amountExpectedAfterTransferInDestination("A", 100)
                .build())),
            Arguments.of(named("more than is available, space in destination", TransferBuilder.create()
                .amountInSource("A", 10)
                .amountToTransfer("A", 11)
                .amountExpectedToBeTransferred(10)
                .amountExpectedAfterTransferInDestination("A", 10)
                .build())),
            Arguments.of(named("resource not existing in source", TransferBuilder.create()
                .amountToTransfer("A", 1)
                .amountExpectedToBeTransferred(0)
                .build())),
            Arguments.of(named("with remainder, space in destination", TransferBuilder.create()
                .amountInSource("A", 50)
                .amountInDestination("A", 51)
                .amountToTransfer("A", 50)
                .amountExpectedToBeTransferred(49)
                .amountExpectedAfterTransferInSource("A", 1)
                .amountExpectedAfterTransferInDestination("A", 100)
                .build())),
            Arguments.of(named("with remainder, no space in destination", TransferBuilder.create()
                .amountInSource("A", 50)
                .amountInDestination("A", 100)
                .amountToTransfer("A", 50)
                .amountExpectedToBeTransferred(0)
                .amountExpectedAfterTransferInSource("A", 50)
                .amountExpectedAfterTransferInDestination("A", 100)
                .build()))
        );
    }

    @ParameterizedTest
    @MethodSource("provideTransfers")
    void shouldTransferCorrectly(final Transfer transfer) {
        // Arrange
        final Storage<String> source = new LimitedStorageImpl<>(100);
        final Storage<String> destination = new LimitedStorageImpl<>(100);

        if (transfer.amountInSource != null) {
            source.insert(
                transfer.amountInSource.getResource(),
                transfer.amountInSource.getAmount(),
                Action.EXECUTE,
                EmptyActor.INSTANCE
            );
        }

        if (transfer.amountInDestination != null) {
            destination.insert(
                transfer.amountInDestination.getResource(),
                transfer.amountInDestination.getAmount(),
                Action.EXECUTE,
                EmptyActor.INSTANCE
            );
        }

        // Act
        final long transferred = TransferHelper.transfer(
            transfer.amountToTransfer.getResource(),
            transfer.amountToTransfer.getAmount(),
            EmptyActor.INSTANCE,
            source,
            destination
        );

        // Assert
        assertThat(transferred).isEqualTo(transfer.amountExpectedToBeTransferred);
        if (transfer.amountExpectedAfterTransferInSource != null) {
            assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                transfer.amountExpectedAfterTransferInSource
            );
        } else {
            assertThat(source.getAll()).isEmpty();
        }
        if (transfer.amountExpectedAfterTransferInDestination != null) {
            assertThat(destination.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
                transfer.amountExpectedAfterTransferInDestination
            );
        } else {
            assertThat(destination.getAll()).isEmpty();
        }
    }

    @Test
    void shouldNotTransferWhenEventualExecutedExtractFromSourceFailed() {
        // Arrange
        final Storage<String> source = new LimitedStorageImpl<>(100) {
            @Override
            public long extract(final String resource, final long amount, final Action action, final Actor actor) {
                if (action == Action.EXECUTE) {
                    return 0L;
                }
                return super.extract(resource, amount, action, actor);
            }
        };
        final Storage<String> destination = new LimitedStorageImpl<>(100);

        source.insert("A", 100, Action.EXECUTE, EmptyActor.INSTANCE);

        // Act
        final long transferred = TransferHelper.transfer("A", 50, EmptyActor.INSTANCE, source, destination);

        // Assert
        assertThat(transferred).isZero();
        assertThat(source.getAll()).usingRecursiveFieldByFieldElementComparator().containsExactly(
            new ResourceAmount<>("A", 100)
        );
        assertThat(destination.getAll()).isEmpty();
    }

    record Transfer(@Nullable ResourceAmount<String> amountInSource,
                    @Nullable ResourceAmount<String> amountInDestination,
                    ResourceAmount<String> amountToTransfer,
                    long amountExpectedToBeTransferred,
                    @Nullable ResourceAmount<String> amountExpectedAfterTransferInSource,
                    @Nullable ResourceAmount<String> amountExpectedAfterTransferInDestination) {
    }

    public static class TransferBuilder {
        @Nullable
        private ResourceAmount<String> amountInSource;
        @Nullable
        private ResourceAmount<String> amountInDestination;
        @Nullable
        private ResourceAmount<String> amountToTransfer;
        private long amountExpectedToBeTransferred;
        @Nullable
        private ResourceAmount<String> amountExpectedAfterTransferInSource;
        @Nullable
        private ResourceAmount<String> amountExpectedAfterTransferInDestination;

        private TransferBuilder() {
        }

        public static TransferBuilder create() {
            return new TransferBuilder();
        }

        public TransferBuilder amountInSource(final String resource, final long amount) {
            this.amountInSource = new ResourceAmount<>(resource, amount);
            return this;
        }

        public TransferBuilder amountInDestination(final String resource, final long amount) {
            this.amountInDestination = new ResourceAmount<>(resource, amount);
            return this;
        }

        public TransferBuilder amountToTransfer(final String resource, final long amount) {
            this.amountToTransfer = new ResourceAmount<>(resource, amount);
            return this;
        }

        public TransferBuilder amountExpectedAfterTransferInSource(final String resource, final long amount) {
            this.amountExpectedAfterTransferInSource = new ResourceAmount<>(resource, amount);
            return this;
        }

        public TransferBuilder amountExpectedAfterTransferInDestination(final String resource, final long amount) {
            this.amountExpectedAfterTransferInDestination = new ResourceAmount<>(resource, amount);
            return this;
        }

        public TransferBuilder amountExpectedToBeTransferred(final long amount) {
            this.amountExpectedToBeTransferred = amount;
            return this;
        }

        public Transfer build() {
            return new Transfer(
                amountInSource,
                amountInDestination,
                Objects.requireNonNull(amountToTransfer),
                amountExpectedToBeTransferred,
                amountExpectedAfterTransferInSource,
                amountExpectedAfterTransferInDestination
            );
        }
    }
}
