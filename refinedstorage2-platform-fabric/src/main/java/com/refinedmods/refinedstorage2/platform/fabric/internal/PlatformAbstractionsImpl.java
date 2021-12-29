package com.refinedmods.refinedstorage2.platform.fabric.internal;

import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.energy.InfiniteEnergyStorage;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.abstractions.BucketQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.abstractions.Config;
import com.refinedmods.refinedstorage2.platform.abstractions.FluidRenderer;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.abstractions.WrenchHelper;
import com.refinedmods.refinedstorage2.platform.abstractions.menu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.abstractions.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.network.ControllerType;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.ConfigImpl;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.ControllerTeamRebornEnergy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.FluidGridEventHandlerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.ItemGridEventHandlerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.FluidGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.ItemGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.fabric.internal.menu.MenuOpenerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.EditBoxAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.KeyMappingAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.SlotAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ClientToServerCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ServerToClientCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.impl.transfer.context.InitialContentsContainerItemContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PlatformAbstractionsImpl implements PlatformAbstractions {
    private final ServerToClientCommunications serverToClientCommunications = new ServerToClientCommunicationsImpl();
    private final ClientToServerCommunications clientToServerCommunications = new ClientToServerCommunicationsImpl();
    private final MenuOpener menuOpener = new MenuOpenerImpl();
    private final BucketQuantityFormatter bucketQuantityFormatter = new BucketQuantityFormatter(FluidConstants.BUCKET);
    private final WrenchHelper wrenchHelper = new WrenchHelperImpl();
    private final FluidRenderer fluidRenderer = new FluidRendererImpl();

    @Override
    public ServerToClientCommunications getServerToClientCommunications() {
        return serverToClientCommunications;
    }

    @Override
    public ClientToServerCommunications getClientToServerCommunications() {
        return clientToServerCommunications;
    }

    @Override
    public MenuOpener getMenuOpener() {
        return menuOpener;
    }

    @Override
    public long getBucketAmount() {
        return FluidConstants.BUCKET;
    }

    @Override
    public BucketQuantityFormatter getBucketQuantityFormatter() {
        return bucketQuantityFormatter;
    }

    @Override
    public WrenchHelper getWrenchHelper() {
        return wrenchHelper;
    }

    @Override
    public Config getConfig() {
        return ConfigImpl.get();
    }

    @Override
    public boolean canEditBoxLoseFocus(EditBox editBox) {
        return ((EditBoxAccessor) editBox).getCanLoseFocus();
    }

    @Override
    public boolean isKeyDown(KeyMapping keyMapping) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), ((KeyMappingAccessor) keyMapping).getKey().getValue());
    }

    @Override
    public int getInventoryIndexOfSlot(Slot slot) {
        return ((SlotAccessor) slot).getSlot();
    }

    @Override
    public ItemGridEventHandler createItemGridEventHandler(AbstractContainerMenu containerMenu, GridService<ItemResource> gridService, Inventory playerInventory) {
        return new ItemGridEventHandlerImpl(containerMenu, gridService, playerInventory);
    }

    @Override
    public FluidGridEventHandler createFluidGridEventHandler(AbstractContainerMenu containerMenu, GridService<FluidResource> gridService, Inventory playerInventory, ExtractableStorage<ItemResource> bucketStorage) {
        return new FluidGridEventHandlerImpl(containerMenu, gridService, playerInventory, bucketStorage);
    }

    @Override
    public Function<ResourceAmount<ItemResource>, GridResource<ItemResource>> getItemGridResourceFactory() {
        return new ItemGridResourceFactory();
    }

    @Override
    public Function<ResourceAmount<FluidResource>, GridResource<FluidResource>> getFluidGridResourceFactory() {
        return new FluidGridResourceFactory();
    }

    @Override
    public FluidRenderer getFluidRenderer() {
        return fluidRenderer;
    }

    @Override
    public Optional<FluidResource> convertToFluid(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return convertNonEmptyToFluid(stack);
    }

    @Override
    public EnergyStorage createEnergyStorage(ControllerType controllerType, Runnable listener) {
        return switch (controllerType) {
            case NORMAL -> new ControllerTeamRebornEnergy(listener);
            case CREATIVE -> new InfiniteEnergyStorage();
        };
    }

    @Override
    public void setEnergy(EnergyStorage energyStorage, long stored) {
        if (energyStorage instanceof ControllerTeamRebornEnergy controllerTeamRebornEnergy) {
            controllerTeamRebornEnergy.setStoredSilently(stored);
        }
    }

    private Optional<FluidResource> convertNonEmptyToFluid(ItemStack stack) {
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(
                stack,
                new InitialContentsContainerItemContext(ItemVariant.of(stack), 1)
        );
        return Optional
                .ofNullable(StorageUtil.findExtractableResource(storage, null))
                .map(VariantUtil::ofFluidVariant);
    }
}
