package com.refinedmods.refinedstorage.api.network.impl.node.relay;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.security.SecurityNetworkComponent;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

public class RelayComponentType<T> {
    public static final RelayComponentType<EnergyNetworkComponent> ENERGY = new RelayComponentType<>(
        network -> network.getComponent(EnergyNetworkComponent.class),
        output -> output::setEnergyDelegate
    );
    public static final RelayComponentType<SecurityNetworkComponent> SECURITY = new RelayComponentType<>(
        network -> network.getComponent(SecurityNetworkComponent.class),
        output -> output::setSecurityDelegate
    );
    public static final RelayComponentType<StorageNetworkComponent> STORAGE = new RelayComponentType<>(
        network -> network.getComponent(StorageNetworkComponent.class),
        output -> output::setStorageDelegate
    );
    public static final RelayComponentType<AutocraftingNetworkComponent> AUTOCRAFTING = new RelayComponentType<>(
        network -> network.getComponent(AutocraftingNetworkComponent.class),
        output -> output::setAutocraftingDelegate
    );
    public static final Set<RelayComponentType<?>> ALL = Set.of(ENERGY, SECURITY, STORAGE, AUTOCRAFTING);

    private final Function<Network, T> componentProvider;
    private final Function<RelayOutputNetworkNode, Consumer<@Nullable T>> componentApplier;

    private RelayComponentType(final Function<Network, T> componentProvider,
                               final Function<RelayOutputNetworkNode, Consumer<@Nullable T>> componentApplier) {
        this.componentProvider = componentProvider;
        this.componentApplier = componentApplier;
    }

    void apply(final Network network, final RelayOutputNetworkNode output) {
        final T component = componentProvider.apply(network);
        componentApplier.apply(output).accept(component);
    }

    void remove(final RelayOutputNetworkNode output) {
        componentApplier.apply(output).accept(null);
    }
}
