package com.refinedmods.refinedstorage2.platform.common.content;

import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraft.client.KeyMapping;

public final class KeyMappings {
    public static final KeyMappings INSTANCE = new KeyMappings();

    @Nullable
    private KeyMapping focusSearchBar;
    @Nullable
    private KeyMapping clearCraftingGridMatrixToNetwork;
    @Nullable
    private KeyMapping clearCraftingGridMatrixToInventory;
    @Nullable
    private KeyMapping openWirelessGrid;

    private KeyMappings() {
    }

    public KeyMapping getFocusSearchBar() {
        return Objects.requireNonNull(focusSearchBar);
    }

    public void setFocusSearchBar(final KeyMapping focusSearchBar) {
        this.focusSearchBar = focusSearchBar;
    }

    @Nullable
    public KeyMapping getClearCraftingGridMatrixToNetwork() {
        return clearCraftingGridMatrixToNetwork;
    }

    public void setClearCraftingGridMatrixToNetwork(final KeyMapping clearCraftingGridMatrixToNetwork) {
        this.clearCraftingGridMatrixToNetwork = clearCraftingGridMatrixToNetwork;
    }

    @Nullable
    public KeyMapping getClearCraftingGridMatrixToInventory() {
        return clearCraftingGridMatrixToInventory;
    }

    public void setClearCraftingGridMatrixToInventory(final KeyMapping clearCraftingGridMatrixToInventory) {
        this.clearCraftingGridMatrixToInventory = clearCraftingGridMatrixToInventory;
    }

    @Nullable
    public KeyMapping getOpenWirelessGrid() {
        return openWirelessGrid;
    }

    public void setOpenWirelessGrid(final KeyMapping openWirelessGrid) {
        this.openWirelessGrid = openWirelessGrid;
    }
}
