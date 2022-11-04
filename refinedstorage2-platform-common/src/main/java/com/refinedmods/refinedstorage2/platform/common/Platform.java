package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.view.AbstractGridResource;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.transfer.TransferManager;
import com.refinedmods.refinedstorage2.platform.common.menu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.common.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.common.render.FluidRenderer;
import com.refinedmods.refinedstorage2.platform.common.util.BucketQuantityFormatter;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface Platform {
    Platform INSTANCE = new PlatformProxy();

    ServerToClientCommunications getServerToClientCommunications();

    ClientToServerCommunications getClientToServerCommunications();

    MenuOpener getMenuOpener();

    long getBucketAmount();

    TagKey<Item> getWrenchTag();

    BucketQuantityFormatter getBucketQuantityFormatter();

    Config getConfig();

    boolean canEditBoxLoseFocus(EditBox editBox);

    boolean isKeyDown(KeyMapping keyMapping);

    ItemGridEventHandler createItemGridEventHandler(AbstractContainerMenu containerMenu,
                                                    GridService<ItemResource> gridService,
                                                    Inventory playerInventory);

    FluidGridEventHandler createFluidGridEventHandler(AbstractContainerMenu containerMenu,
                                                      GridService<FluidResource> gridService,
                                                      Inventory playerInventory,
                                                      ExtractableStorage<ItemResource> bucketStorage);

    Function<ResourceAmount<ItemResource>, AbstractGridResource<ItemResource>> getItemGridResourceFactory();

    Function<ResourceAmount<FluidResource>, AbstractGridResource<FluidResource>> getFluidGridResourceFactory();

    FluidRenderer getFluidRenderer();

    Optional<FluidResource> convertToFluid(ItemStack stack);

    EnergyStorage createEnergyStorage(ControllerType controllerType, Runnable listener);

    void setEnergy(EnergyStorage energyStorage, long stored);

    TransferManager createTransferManager(AbstractContainerMenu containerMenu);

    long insertIntoContainer(Container container, ItemResource itemResource, long amount, Action action);
}
