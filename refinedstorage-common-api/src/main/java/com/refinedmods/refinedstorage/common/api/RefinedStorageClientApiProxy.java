package com.refinedmods.refinedstorage.common.api;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage.common.api.grid.GridInsertionHints;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public class RefinedStorageClientApiProxy implements RefinedStorageClientApi {
    @Nullable
    private RefinedStorageClientApi delegate;

    public void setDelegate(final RefinedStorageClientApi delegate) {
        if (this.delegate != null) {
            throw new IllegalStateException("Client API already injected");
        }
        this.delegate = delegate;
    }

    @Override
    public AbstractContainerScreen<AbstractContainerMenu> createStorageBlockScreen(
        final AbstractContainerMenu menu,
        final Inventory inventory,
        final Component title,
        final Class<? extends ResourceKey> resourceClass
    ) {
        return ensureLoaded().createStorageBlockScreen(menu, inventory, title, resourceClass);
    }

    @Override
    public void openAutocraftingPreview(final List<ResourceAmount> requests, @Nullable final Screen parentScreen) {
        ensureLoaded().openAutocraftingPreview(requests, parentScreen);
    }

    @Override
    public <T extends ResourceKey> void registerResourceRendering(final Class<T> resourceClass,
                                                                  final ResourceRendering rendering) {
        ensureLoaded().registerResourceRendering(resourceClass, rendering);
    }

    @Override
    public <T extends ResourceKey> ResourceRendering getResourceRendering(final Class<T> resourceClass) {
        return ensureLoaded().getResourceRendering(resourceClass);
    }

    @Override
    public void addAlternativeGridInsertionHint(final GridInsertionHint hint) {
        ensureLoaded().addAlternativeGridInsertionHint(hint);
    }

    @Override
    public GridInsertionHints getGridInsertionHints() {
        return ensureLoaded().getGridInsertionHints();
    }

    @Override
    public void registerDiskModel(final Item item, final Identifier model) {
        ensureLoaded().registerDiskModel(item, model);
    }

    @Override
    public Set<Identifier> getDiskModels() {
        return ensureLoaded().getDiskModels();
    }

    @Override
    public Map<Item, Identifier> getDiskModelsByItem() {
        return ensureLoaded().getDiskModelsByItem();
    }

    private RefinedStorageClientApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("API not loaded yet");
        }
        return delegate;
    }
}
