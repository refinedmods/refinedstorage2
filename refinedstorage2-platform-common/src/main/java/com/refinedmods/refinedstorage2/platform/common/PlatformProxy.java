package com.refinedmods.refinedstorage2.platform.common;

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
import javax.annotation.Nullable;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlatformProxy implements Platform {
    @Nullable
    private Platform platform;

    public void setPlatform(final Platform platform) {
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
    public TagKey<Item> getWrenchTag() {
        return ensureLoaded().getWrenchTag();
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
    public boolean canEditBoxLoseFocus(final EditBox editBox) {
        return ensureLoaded().canEditBoxLoseFocus(editBox);
    }

    @Override
    public boolean isKeyDown(final KeyMapping keyMapping) {
        return ensureLoaded().isKeyDown(keyMapping);
    }

    @Override
    public ItemGridEventHandler createItemGridEventHandler(final AbstractContainerMenu containerMenu,
                                                           final GridService<ItemResource> gridService,
                                                           final Inventory playerInventory) {
        return ensureLoaded().createItemGridEventHandler(containerMenu, gridService, playerInventory);
    }

    @Override
    public FluidGridEventHandler createFluidGridEventHandler(final AbstractContainerMenu containerMenu,
                                                             final GridService<FluidResource> gridService,
                                                             final Inventory playerInventory,
                                                             final ExtractableStorage<ItemResource> bucketStorage) {
        return ensureLoaded().createFluidGridEventHandler(containerMenu, gridService, playerInventory, bucketStorage);
    }

    @Override
    public Function<ResourceAmount<ItemResource>, AbstractGridResource<ItemResource>> getItemGridResourceFactory() {
        return ensureLoaded().getItemGridResourceFactory();
    }

    @Override
    public Function<ResourceAmount<FluidResource>, AbstractGridResource<FluidResource>> getFluidGridResourceFactory() {
        return ensureLoaded().getFluidGridResourceFactory();
    }

    @Override
    public FluidRenderer getFluidRenderer() {
        return ensureLoaded().getFluidRenderer();
    }

    @Override
    public Optional<FluidResource> convertToFluid(final ItemStack stack) {
        return ensureLoaded().convertToFluid(stack);
    }

    @Override
    public EnergyStorage createEnergyStorage(final ControllerType controllerType, final Runnable listener) {
        return ensureLoaded().createEnergyStorage(controllerType, listener);
    }

    @Override
    public void setEnergy(final EnergyStorage energyStorage, final long stored) {
        ensureLoaded().setEnergy(energyStorage, stored);
    }

    @Override
    public TransferManager createTransferManager(final AbstractContainerMenu containerMenu) {
        return ensureLoaded().createTransferManager(containerMenu);
    }

    private Platform ensureLoaded() {
        if (platform == null) {
            throw new IllegalStateException("Platform not loaded yet");
        }
        return platform;
    }
}
