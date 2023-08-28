package com.refinedmods.refinedstorage2.api.network.impl.energy;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnergyStorageImplTest {
    EnergyStorage sut;
    int changeCount;

    @BeforeEach
    void setUp() {
        changeCount = 0;
        sut = new EnergyStorageImpl(100) {
            @Override
            protected void changed() {
                super.changed();
                changeCount++;
            }
        };
    }

    @Test
    void testInvalidCapacity() {
        // Act
        final Executable action = () -> new EnergyStorageImpl(-1);

        // Assert
        assertThrows(Exception.class, action);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotReceiveEnergyOnZeroCapacityStorage(final Action action) {
        // Arrange
        final EnergyStorage zeroCapacitySut = new EnergyStorageImpl(0) {
            @Override
            protected void changed() {
                super.changed();
                changeCount++;
            }
        };

        // Act
        final long inserted = zeroCapacitySut.receive(1, action);

        // Assert
        assertThat(inserted).isZero();
        assertThat(changeCount).isZero();
        assertThat(zeroCapacitySut.getStored()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveEnergy(final Action action) {
        // Act
        final long inserted = sut.receive(50, action);

        // Assert
        assertThat(inserted).isEqualTo(50);

        if (action == Action.EXECUTE) {
            assertThat(sut.getStored()).isEqualTo(50);
            assertThat(changeCount).isEqualTo(1);
        } else {
            assertThat(sut.getStored()).isZero();
            assertThat(changeCount).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveEnergyAndReachCapacity(final Action action) {
        // Act
        final long inserted = sut.receive(100, action);

        // Assert
        assertThat(inserted).isEqualTo(100);

        if (action == Action.EXECUTE) {
            assertThat(sut.getStored()).isEqualTo(100);
            assertThat(changeCount).isEqualTo(1);
        } else {
            assertThat(sut.getStored()).isZero();
            assertThat(changeCount).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldReceiveEnergyAndExceedCapacity(final Action action) {
        // Act
        final long inserted = sut.receive(101, action);

        // Assert
        assertThat(inserted).isEqualTo(100);

        if (action == Action.EXECUTE) {
            assertThat(sut.getStored()).isEqualTo(100);
            assertThat(changeCount).isEqualTo(1);
        } else {
            assertThat(sut.getStored()).isZero();
            assertThat(changeCount).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldNotReceiveEnergyWhenFull(final Action action) {
        // Arrange
        sut.receive(100, Action.EXECUTE);
        changeCount = 0;

        // Act
        final long inserted = sut.receive(100, action);

        // Assert
        assertThat(inserted).isZero();
        assertThat(changeCount).isZero();
        assertThat(sut.getStored()).isEqualTo(100);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractEnergyPartly(final Action action) {
        // Arrange
        sut.receive(100, Action.EXECUTE);
        changeCount = 0;

        // Act
        final long extracted = sut.extract(99, action);

        // Assert
        assertThat(extracted).isEqualTo(99);

        if (action == Action.EXECUTE) {
            assertThat(sut.getStored()).isEqualTo(1);
            assertThat(changeCount).isEqualTo(1);
        } else {
            assertThat(sut.getStored()).isEqualTo(100);
            assertThat(changeCount).isZero();
        }
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void shouldExtractEnergyCompletely(final Action action) {
        // Arrange
        sut.receive(50, Action.EXECUTE);
        changeCount = 0;

        // Act
        final long extracted = sut.extract(51, action);

        // Assert
        assertThat(extracted).isEqualTo(50);

        if (action == Action.EXECUTE) {
            assertThat(sut.getStored()).isZero();
            assertThat(changeCount).isEqualTo(1);
        } else {
            assertThat(sut.getStored()).isEqualTo(50);
            assertThat(changeCount).isZero();
        }
    }
}
