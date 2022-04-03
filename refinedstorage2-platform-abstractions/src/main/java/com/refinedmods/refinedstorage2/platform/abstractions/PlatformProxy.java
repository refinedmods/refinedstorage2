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

public class PlatformProxy implements Platform {
    private Platform platform;

    public void setPlatform(Platform platform) {
        if (this.platform != null) {
            throw new IllegalStateException("Platform already set");
        }
        this.platform = platform;
    }

    @Override
    public ServerToClientCommunications getServerToClientCommunications() {
        return ensureLoaded().getServerToClientCommunications();
    }

    @Override
    public ClientToServerCommunications getClientToServerCommunications() {
        return ensureLoaded().getClientToServerCommunications();
    }

    @Override
    public MenuOpener getMenuOpener() {
        return ensureLoaded().getMenuOpener();
    }

    @Override
    public long getBucketAmount() {
        return ensureLoaded().getBucketAmount();
    }

    @Override
    public BucketQuantityFormatter getBucketQuantityFormatter() {
        return ensureLoaded().getBucketQuantityFormatter();
    }

    @Override
    public Config getConfig() {
        return ensureLoaded().getConfig();
    }

    @Override
    public boolean canEditBoxLoseFocus(EditBox editBox) {
        return ensureLoaded().canEditBoxLoseFocus(editBox);
    }

    @Override
    public boolean isKeyDown(KeyMapping keyMapping) {
        return ensureLoaded().isKeyDown(keyMapping);
    }

    @Override
    public ItemGridEventHandler createItemGridEventHandler(AbstractContainerMenu containerMenu, GridService<ItemResource> gridService, Inventory playerInventory) {
        return ensureLoaded().createItemGridEventHandler(containerMenu, gridService, playerInventory);
    }

    @Override
    public FluidGridEventHandler createFluidGridEventHandler(AbstractContainerMenu containerMenu, GridService<FluidResource> gridService, Inventory playerInventory, ExtractableStorage<ItemResource> bucketStorage) {
        return ensureLoaded().createFluidGridEventHandler(containerMenu, gridService, playerInventory, bucketStorage);
    }

    @Override
    public Function<ResourceAmount<ItemResource>, GridResource<ItemResource>> getItemGridResourceFactory() {
        return ensureLoaded().getItemGridResourceFactory();
    }

    @Override
    public Function<ResourceAmount<FluidResource>, GridResource<FluidResource>> getFluidGridResourceFactory() {
        return ensureLoaded().getFluidGridResourceFactory();
    }

    @Override
    public FluidRenderer getFluidRenderer() {
        return ensureLoaded().getFluidRenderer();
    }

    @Override
    public Optional<FluidResource> convertToFluid(ItemStack stack) {
        return ensureLoaded().convertToFluid(stack);
    }

    @Override
    public EnergyStorage createEnergyStorage(ControllerType controllerType, Runnable listener) {
        return ensureLoaded().createEnergyStorage(controllerType, listener);
    }

    @Override
    public void setEnergy(EnergyStorage energyStorage, long stored) {
        ensureLoaded().setEnergy(energyStorage, stored);
    }

    private Platform ensureLoaded() {
        if (platform == null) {
            throw new IllegalStateException("Platform not loaded yet");
        }
        return platform;
    }
}
