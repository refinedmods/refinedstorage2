package com.refinedmods.refinedstorage2.platform.abstractions;

import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.abstractions.menu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.network.ControllerType;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public interface PlatformAbstractions {
    PlatformAbstractions INSTANCE = new PlatformAbstractionsProxy();

    ServerToClientCommunications getServerToClientCommunications();

    ClientToServerCommunications getClientToServerCommunications();

    MenuOpener getMenuOpener();

    long getBucketAmount();

    BucketQuantityFormatter getBucketQuantityFormatter();

    WrenchHelper getWrenchHelper();

    Config getConfig();

    boolean canEditBoxLoseFocus(EditBox editBox);

    boolean isKeyDown(KeyMapping keyMapping);

    ItemGridEventHandler createItemGridEventHandler(AbstractContainerMenu containerMenu, GridService<ItemResource> gridService, Inventory playerInventory);

    FluidGridEventHandler createFluidGridEventHandler(AbstractContainerMenu containerMenu, GridService<FluidResource> gridService, Inventory playerInventory, ExtractableStorage<ItemResource> bucketStorage);

    Function<ResourceAmount<ItemResource>, GridResource<ItemResource>> getItemGridResourceFactory();

    Function<ResourceAmount<FluidResource>, GridResource<FluidResource>> getFluidGridResourceFactory();

    FluidRenderer getFluidRenderer();

    Optional<FluidResource> convertToFluid(ItemStack stack);

    EnergyStorage createEnergyStorage(ControllerType controllerType, Runnable listener);

    void setEnergy(EnergyStorage energyStorage, long stored);
}
