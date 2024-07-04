package com.refinedmods.refinedstorage.platform.common.content;

import com.refinedmods.refinedstorage.platform.common.configurationcard.ConfigurationCardState;
import com.refinedmods.refinedstorage.platform.common.security.SecurityCardBoundPlayer;
import com.refinedmods.refinedstorage.platform.common.security.SecurityCardPermissions;
import com.refinedmods.refinedstorage.platform.common.upgrade.RegulatorUpgradeState;

import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;

import static java.util.Objects.requireNonNull;

public final class DataComponents {
    public static final DataComponents INSTANCE = new DataComponents();

    @Nullable
    private Supplier<DataComponentType<Long>> energy;
    @Nullable
    private Supplier<DataComponentType<GlobalPos>> networkLocation;
    @Nullable
    private Supplier<DataComponentType<UUID>> storageReference;
    @Nullable
    private Supplier<DataComponentType<RegulatorUpgradeState>> regulatorUpgradeState;
    @Nullable
    private Supplier<DataComponentType<SecurityCardBoundPlayer>> securityCardBoundPlayer;
    @Nullable
    private Supplier<DataComponentType<SecurityCardPermissions>> securityCardPermissions;
    @Nullable
    private Supplier<DataComponentType<ConfigurationCardState>> configurationCardState;

    private DataComponents() {
    }

    public DataComponentType<Long> getEnergy() {
        return requireNonNull(energy).get();
    }

    public void setEnergy(@Nullable final Supplier<DataComponentType<Long>> supplier) {
        this.energy = supplier;
    }

    public DataComponentType<GlobalPos> getNetworkLocation() {
        return requireNonNull(networkLocation).get();
    }

    public void setNetworkLocation(@Nullable final Supplier<DataComponentType<GlobalPos>> supplier) {
        this.networkLocation = supplier;
    }

    public DataComponentType<UUID> getStorageReference() {
        return requireNonNull(storageReference).get();
    }

    public void setStorageReference(@Nullable final Supplier<DataComponentType<UUID>> supplier) {
        this.storageReference = supplier;
    }

    public DataComponentType<RegulatorUpgradeState> getRegulatorUpgradeState() {
        return requireNonNull(regulatorUpgradeState).get();
    }

    public void setRegulatorUpgradeState(@Nullable final Supplier<DataComponentType<RegulatorUpgradeState>> supplier) {
        this.regulatorUpgradeState = supplier;
    }

    public DataComponentType<SecurityCardBoundPlayer> getSecurityCardBoundPlayer() {
        return requireNonNull(securityCardBoundPlayer).get();
    }

    public void setSecurityCardBoundPlayer(
        @Nullable final Supplier<DataComponentType<SecurityCardBoundPlayer>> supplier
    ) {
        this.securityCardBoundPlayer = supplier;
    }

    public DataComponentType<SecurityCardPermissions> getSecurityCardPermissions() {
        return requireNonNull(securityCardPermissions).get();
    }

    public void setSecurityCardPermissions(
        @Nullable final Supplier<DataComponentType<SecurityCardPermissions>> supplier
    ) {
        this.securityCardPermissions = supplier;
    }

    public DataComponentType<ConfigurationCardState> getConfigurationCardState() {
        return requireNonNull(configurationCardState).get();
    }

    public void setConfigurationCardState(
        @Nullable final Supplier<DataComponentType<ConfigurationCardState>> supplier
    ) {
        this.configurationCardState = supplier;
    }
}
