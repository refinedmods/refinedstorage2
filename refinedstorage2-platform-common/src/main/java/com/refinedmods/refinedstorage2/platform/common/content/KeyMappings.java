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

    private KeyMappings() {
    }

    public KeyMapping getFocusSearchBar() {
        return Objects.requireNonNull(focusSearchBar);
    }

    public void setFocusSearchBar(final KeyMapping focusSearchBar) {
        this.focusSearchBar = focusSearchBar;
    }

    @Nullable // TODO - implement on Fabric
    public KeyMapping getClearCraftingGridMatrixToNetwork() {
        return clearCraftingGridMatrixToNetwork;
    }

    public void setClearCraftingGridMatrixToNetwork(final KeyMapping clearCraftingGridMatrixToNetwork) {
        this.clearCraftingGridMatrixToNetwork = clearCraftingGridMatrixToNetwork;
    }

    @Nullable // TODO - implement on Fabric
    public KeyMapping getClearCraftingGridMatrixToInventory() {
        return clearCraftingGridMatrixToInventory;
    }

    public void setClearCraftingGridMatrixToInventory(final KeyMapping clearCraftingGridMatrixToInventory) {
        this.clearCraftingGridMatrixToInventory = clearCraftingGridMatrixToInventory;
    }
}
