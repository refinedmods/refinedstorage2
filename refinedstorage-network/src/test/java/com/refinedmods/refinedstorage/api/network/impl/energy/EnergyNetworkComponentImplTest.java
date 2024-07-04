package com.refinedmods.refinedstorage.api.network.impl.energy;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyNetworkComponentImplTest {
    @Test
    void testInitialState() {
        // Arrange
        final EnergyNetworkComponent sut = new EnergyNetworkComponentImpl();

        // Assert
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
    }

    @Test
    void shouldNotExtractAnythingWhenNoProvidersAreAvailable() {
        // Arrange
        final EnergyNetworkComponent sut = new EnergyNetworkComponentImpl();

        // Act
        final long extracted = sut.extract(3);

        // Assert
        assertThat(extracted).isZero();
        assertThat(sut.getStored()).isZero();
        assertThat(sut.getCapacity()).isZero();
    }

    @Test
    void shouldExtractFromSingleProviderPartly() {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final EnergyNetworkComponent sut = new EnergyNetworkComponentImpl();
        sut.onContainerAdded(container(a));
        sut.onContainerAdded(container(b));

        // Act
        final long extracted = sut.extract(3);

        // Assert
        assertThat(extracted).isEqualTo(3);
        assertThat(a.getStored()).isEqualTo(7);
        assertThat(b.getStored()).isEqualTo(3);
        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @Test
    void shouldExtractFromSingleProviderCompletely() {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final EnergyNetworkComponent sut = new EnergyNetworkComponentImpl();
        sut.onContainerAdded(container(a));
        sut.onContainerAdded(container(b));

        // Act
        final long extracted = sut.extract(10);

        // Assert
        assertThat(extracted).isEqualTo(10);
        assertThat(a.getStored()).isZero();
        assertThat(b.getStored()).isEqualTo(3);
        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @Test
    void shouldExtractFromMultipleProvidersPartly() {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final EnergyNetworkComponent sut = new EnergyNetworkComponentImpl();
        sut.onContainerAdded(container(a));
        sut.onContainerAdded(container(b));

        // Act
        final long extracted = sut.extract(11);

        // Assert
        assertThat(extracted).isEqualTo(11);
        assertThat(a.getStored()).isZero();
        assertThat(b.getStored()).isEqualTo(2);
        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @Test
    void shouldExtractFromMultipleProvidersCompletely() {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final EnergyNetworkComponent sut = new EnergyNetworkComponentImpl();
        sut.onContainerAdded(container(a));
        sut.onContainerAdded(container(b));

        // Act
        final long extracted = sut.extract(13);

        // Assert
        assertThat(extracted).isEqualTo(13);
        assertThat(a.getStored()).isZero();
        assertThat(b.getStored()).isZero();
        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @Test
    void shouldExtractFromMultipleProvidersCompletelyMoreThanIsAvailable() {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(10);
        final EnergyStorage b = new EnergyStorageImpl(5);

        a.receive(10, Action.EXECUTE);
        b.receive(3, Action.EXECUTE);

        final EnergyNetworkComponent sut = new EnergyNetworkComponentImpl();
        sut.onContainerAdded(container(a));
        sut.onContainerAdded(container(b));

        // Act
        final long extracted = sut.extract(14);

        // Assert
        assertThat(extracted).isEqualTo(13);
        assertThat(a.getStored()).isZero();
        assertThat(b.getStored()).isZero();
        assertThat(sut.getCapacity()).isEqualTo(15);
    }

    @Test
    void shouldNotExceedLongMaxWithCapacityOrStored() {
        // Arrange
        final EnergyStorage a = new EnergyStorageImpl(Long.MAX_VALUE);
        final EnergyStorage b = new EnergyStorageImpl(Long.MAX_VALUE);

        a.receive(Long.MAX_VALUE, Action.EXECUTE);
        b.receive(Long.MAX_VALUE, Action.EXECUTE);

        final EnergyNetworkComponent sut = new EnergyNetworkComponentImpl();
        sut.onContainerAdded(container(a));
        sut.onContainerAdded(container(b));

        // Act
        final long stored = sut.getStored();
        final long capacity = sut.getCapacity();

        // Assert
        assertThat(stored).isEqualTo(Long.MAX_VALUE);
        assertThat(capacity).isEqualTo(Long.MAX_VALUE);
    }

    private NetworkNodeContainer container(final EnergyStorage energyStorage) {
        final ControllerNetworkNode controller = new ControllerNetworkNode();
        controller.setEnergyStorage(energyStorage);
        controller.setActive(true);
        return () -> controller;
    }
}
