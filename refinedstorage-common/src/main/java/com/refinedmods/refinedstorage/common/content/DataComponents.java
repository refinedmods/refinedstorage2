package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.autocrafting.CraftingPatternState;
import com.refinedmods.refinedstorage.common.autocrafting.PatternState;
import com.refinedmods.refinedstorage.common.autocrafting.ProcessingPatternState;
import com.refinedmods.refinedstorage.common.autocrafting.SmithingTablePatternState;
import com.refinedmods.refinedstorage.common.autocrafting.StonecutterPatternState;
import com.refinedmods.refinedstorage.common.configurationcard.ConfigurationCardState;
import com.refinedmods.refinedstorage.common.security.SecurityCardBoundPlayer;
import com.refinedmods.refinedstorage.common.security.SecurityCardPermissions;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeState;

import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.Nullable;

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
    private Supplier<DataComponentType<UUID>> storageReferenceToBeTransferred;
    @Nullable
    private Supplier<DataComponentType<RegulatorUpgradeState>> regulatorUpgradeState;
    @Nullable
    private Supplier<DataComponentType<SecurityCardBoundPlayer>> securityCardBoundPlayer;
    @Nullable
    private Supplier<DataComponentType<SecurityCardPermissions>> securityCardPermissions;
    @Nullable
    private Supplier<DataComponentType<ConfigurationCardState>> configurationCardState;
    @Nullable
    private Supplier<DataComponentType<PatternState>> patternState;
    @Nullable
    private Supplier<DataComponentType<CraftingPatternState>> craftingPatternState;
    @Nullable
    private Supplier<DataComponentType<ProcessingPatternState>> processingPatternState;
    @Nullable
    private Supplier<DataComponentType<StonecutterPatternState>> stonecutterPatternState;
    @Nullable
    private Supplier<DataComponentType<SmithingTablePatternState>> smithingTablePatternState;

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

    public DataComponentType<UUID> getStorageReferenceToBeTransferred() {
        return requireNonNull(storageReferenceToBeTransferred).get();
    }

    public void setStorageReferenceToBeTransferred(@Nullable final Supplier<DataComponentType<UUID>> supplier) {
        this.storageReferenceToBeTransferred = supplier;
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

    public DataComponentType<PatternState> getPatternState() {
        return requireNonNull(patternState).get();
    }

    public void setPatternState(
        @Nullable final Supplier<DataComponentType<PatternState>> supplier
    ) {
        this.patternState = supplier;
    }

    public DataComponentType<CraftingPatternState> getCraftingPatternState() {
        return requireNonNull(craftingPatternState).get();
    }

    public void setCraftingPatternState(
        @Nullable final Supplier<DataComponentType<CraftingPatternState>> supplier
    ) {
        this.craftingPatternState = supplier;
    }

    public DataComponentType<ProcessingPatternState> getProcessingPatternState() {
        return requireNonNull(processingPatternState).get();
    }

    public void setProcessingPatternState(
        @Nullable final Supplier<DataComponentType<ProcessingPatternState>> supplier
    ) {
        this.processingPatternState = supplier;
    }

    public DataComponentType<StonecutterPatternState> getStonecutterPatternState() {
        return requireNonNull(stonecutterPatternState).get();
    }

    public void setStonecutterPatternState(
        @Nullable final Supplier<DataComponentType<StonecutterPatternState>> supplier
    ) {
        this.stonecutterPatternState = supplier;
    }

    public DataComponentType<SmithingTablePatternState> getSmithingTablePatternState() {
        return requireNonNull(smithingTablePatternState).get();
    }

    public void setSmithingTablePatternState(
        @Nullable final Supplier<DataComponentType<SmithingTablePatternState>> supplier
    ) {
        this.smithingTablePatternState = supplier;
    }
}
