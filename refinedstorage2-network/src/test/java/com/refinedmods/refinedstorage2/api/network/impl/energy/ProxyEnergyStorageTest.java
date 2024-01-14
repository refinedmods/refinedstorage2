package com.refinedmods.refinedstorage2.api.network.impl.energy;

import com.refinedmods.refinedstorage2.api.core.Action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyEnergyStorageTest {
    AbstractProxyEnergyStorage sut;

    @BeforeEach
    void setUp() {
        sut = new AbstractProxyEnergyStorage(new EnergyStorageImpl(100)) {
        };
    }

    @Test
    void testProxy() {
        // Act
        final long capacity = sut.getCapacity();
        final long received = sut.receive(100, Action.EXECUTE);
        final long extracted = sut.extract(15, Action.EXECUTE);
        final long stored = sut.getStored();

        // Assert
        assertThat(capacity).isEqualTo(100);
        assertThat(received).isEqualTo(100);
        assertThat(extracted).isEqualTo(15);
        assertThat(stored).isEqualTo(100 - 15);
    }
}
