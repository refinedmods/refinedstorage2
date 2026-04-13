package com.refinedmods.refinedstorage.common;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage.common.api.grid.GridInsertionHints;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.grid.screen.hint.GridInsertionHintsImpl;
import com.refinedmods.refinedstorage.common.grid.screen.hint.ItemGridInsertionHint;
import com.refinedmods.refinedstorage.common.grid.screen.hint.SingleItemGridInsertionHint;
import com.refinedmods.refinedstorage.common.storage.storageblock.StorageBlockContainerMenu;
import com.refinedmods.refinedstorage.common.storage.storageblock.StorageBlockScreen;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResourceRendering;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

public class RefinedStorageClientApiImpl implements RefinedStorageClientApi {
    private final Map<Class<?>, ResourceRendering> resourceRenderingMap = new HashMap<>();
    private final Map<Item, Identifier> diskModelsByItem = new HashMap<>();
    private final Set<Identifier> diskModels = new HashSet<>();
    private final GridInsertionHintsImpl gridInsertionHints = new GridInsertionHintsImpl(
        new ItemGridInsertionHint(),
        new SingleItemGridInsertionHint()
    );

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AbstractContainerScreen<AbstractContainerMenu> createStorageBlockScreen(
        final AbstractContainerMenu menu,
        final Inventory inventory,
        final Component title,
        final Class<? extends ResourceKey> resourceClass
    ) {
        return (AbstractContainerScreen) new StorageBlockScreen(
            (StorageBlockContainerMenu) menu,
            inventory,
            title,
            getResourceRendering(resourceClass)
        );
    }

    @Override
    public void openAutocraftingPreview(final List<ResourceAmount> requests, @Nullable final Screen parentScreen) {
        if (requests.isEmpty()) {
            return;
        }
        ClientPlatformUtil.openCraftingPreview(requests, parentScreen);
    }

    @Override
    public void addAlternativeGridInsertionHint(final GridInsertionHint hint) {
        gridInsertionHints.addAlternativeHint(hint);
    }

    @Override
    public GridInsertionHints getGridInsertionHints() {
        return gridInsertionHints;
    }

    @Override
    public void registerDiskModel(final Item item, final Identifier model) {
        diskModelsByItem.put(item, model);
        diskModels.add(model);
    }

    @Override
    public Set<Identifier> getDiskModels() {
        return diskModels;
    }

    @Override
    public Map<Item, Identifier> getDiskModelsByItem() {
        return Collections.unmodifiableMap(diskModelsByItem);
    }

    @Override
    public <T extends ResourceKey> void registerResourceRendering(final Class<T> resourceClass,
                                                                  final ResourceRendering rendering) {
        resourceRenderingMap.put(resourceClass, rendering);
    }

    @Override
    public <T extends ResourceKey> ResourceRendering getResourceRendering(final Class<T> resourceClass) {
        // fast path for items
        if (resourceClass == ItemResource.class) {
            return ItemResourceRendering.INSTANCE;
        }
        return resourceRenderingMap.get(resourceClass);
    }
}
