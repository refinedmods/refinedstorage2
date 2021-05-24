package com.refinedmods.refinedstorage2.core.network.energy;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.util.Action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class CompositeEnergyStorageTest {
    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_filling_when_no_sources_are_present(Action action) {
        // Arrange
        CompositeEnergyStorage sut = new CompositeEnergyStorage();

        // Act
        long remainder = sut.receive(3, action);

        // Assert
        assertThat(remainder).isEqualTo(3);
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_filling_single_source_partially_when_receiving(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long remainder = sut.receive(3, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(3);
            assertThat(sut.getStored()).isEqualTo(3);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(b.getStored()).isZero();
        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_filling_single_source_completely_when_receiving(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long remainder = sut.receive(10, action);

        // Assert
        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(sut.getStored()).isEqualTo(10);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(remainder).isZero();
        assertThat(b.getStored()).isZero();
        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_filling_multiple_sources_partially_when_receiving(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long remainder = sut.receive(13, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
            assertThat(sut.getStored()).isEqualTo(13);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_filling_multiple_sources_completely_when_receiving(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long remainder = sut.receive(15, action);

        // Assert
        assertThat(remainder).isZero();

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(5);
            assertThat(sut.getStored()).isEqualTo(15);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_filling_multiple_sources_completely_when_receiving_with_overflow(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long remainder = sut.receive(16, action);

        // Assert
        assertThat(remainder).isEqualTo(1);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(5);
            assertThat(sut.getStored()).isEqualTo(15);
        } else {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
            assertThat(sut.getStored()).isZero();
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_when_no_sources_are_available(Action action) {
        // Arrange
        CompositeEnergyStorage sut = new CompositeEnergyStorage();

        // Act
        long extracted = sut.extract(3, action);

        // Assert
        assertThat(extracted).isZero();
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_on_single_source_partially(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long extracted = sut.extract(3, action);

        // Assert
        assertThat(extracted).isEqualTo(3);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isEqualTo(7);
            assertThat(b.getStored()).isEqualTo(3);
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_on_single_source_completely(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long extracted = sut.extract(10, action);

        // Assert
        assertThat(extracted).isEqualTo(10);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isEqualTo(3);
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_on_multiple_sources_partially(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long extracted = sut.extract(11, action);

        // Assert
        assertThat(extracted).isEqualTo(11);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isEqualTo(2);
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_on_multiple_sources_completely(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long extracted = sut.extract(13, action);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @ParameterizedTest
    @EnumSource(Action.class)
    void Test_extracting_on_multiple_sources_completely_more_than_is_available(Action action) {
        // Arrange
        EnergyStorage a = new EnergyStorageImpl(10);
        EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long extracted = sut.extract(14, action);

        // Assert
        assertThat(extracted).isEqualTo(13);

        if (action == Action.EXECUTE) {
            assertThat(a.getStored()).isZero();
            assertThat(b.getStored()).isZero();
        } else {
            assertThat(a.getStored()).isEqualTo(10);
            assertThat(b.getStored()).isEqualTo(3);
        }

        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @Test
    void Test_energy_stored_and_capacity_on_composite_with_infinite_energy_storages() {
        // Arrange
        EnergyStorage a = new CreativeEnergyStorage();
        EnergyStorage b = new CreativeEnergyStorage();

        CompositeEnergyStorage sut = new CompositeEnergyStorage();
        sut.addSource(a);
        sut.addSource(b);

        // Act
        long stored = sut.getStored();
        long capacity = sut.getCapacity();

        // Assert
        assertThat(stored).isEqualTo(Long.MAX_VALUE);
        assertThat(capacity).isEqualTo(Long.MAX_VALUE);
    }
}
