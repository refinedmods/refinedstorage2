package com.refinedmods.refinedstorage2.platform.abstractions;

import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.abstractions.menu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PlatformAbstractionsProxy implements PlatformAbstractions {
    private PlatformAbstractions abstractions;

    public void setAbstractions(PlatformAbstractions abstractions) {
        if (this.abstractions != null) {
            throw new IllegalStateException("Platform abstractions already injected");
        }
        this.abstractions = abstractions;
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
    public WrenchHelper getWrenchHelper() {
        return ensureLoaded().getWrenchHelper();
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
    public int getInventoryIndexOfSlot(Slot slot) {
        return ensureLoaded().getInventoryIndexOfSlot(slot);
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

    private PlatformAbstractions ensureLoaded() {
        if (abstractions == null) {
            throw new IllegalStateException("Platform abstractions not loaded yet");
        }
        return abstractions;
    }
}