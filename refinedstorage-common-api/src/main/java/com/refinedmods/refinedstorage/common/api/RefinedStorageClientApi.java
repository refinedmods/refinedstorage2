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
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.11")
public interface RefinedStorageClientApi {
    RefinedStorageClientApi INSTANCE = new RefinedStorageClientApiProxy();

    AbstractContainerScreen<AbstractContainerMenu> createStorageBlockScreen(
        AbstractContainerMenu menu,
        Inventory inventory,
        Component title,
        Class<? extends ResourceKey> resourceClass
    );

    void openAutocraftingPreview(List<ResourceAmount> requests, @Nullable Screen parentScreen);

    <T extends ResourceKey> void registerResourceRendering(Class<T> resourceClass, ResourceRendering rendering);

    <T extends ResourceKey> ResourceRendering getResourceRendering(Class<T> resourceClass);

    void addAlternativeGridInsertionHint(GridInsertionHint hint);

    GridInsertionHints getGridInsertionHints();

    void registerDiskModel(Item item, Identifier model);

    Set<Identifier> getDiskModels();

    Map<Item, Identifier> getDiskModelsByItem();
}
