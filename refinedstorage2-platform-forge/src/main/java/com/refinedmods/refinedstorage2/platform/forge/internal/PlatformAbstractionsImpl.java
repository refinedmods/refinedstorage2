package com.refinedmods.refinedstorage2.platform.forge.internal;

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
import com.refinedmods.refinedstorage2.platform.forge.integration.energy.ControllerForgeEnergy;
import com.refinedmods.refinedstorage2.platform.forge.internal.menu.MenuOpenerImpl;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;
import com.refinedmods.refinedstorage2.platform.forge.packet.c2s.ClientToServerCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.forge.packet.s2c.ServerToClientCommunicationsImpl;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class PlatformAbstractionsImpl implements PlatformAbstractions {
    private final BucketQuantityFormatter bucketQuantityFormatter = new BucketQuantityFormatter(FluidAttributes.BUCKET_VOLUME);
    private final ConfigImpl config = new ConfigImpl();
    private final MenuOpener menuOpener = new MenuOpenerImpl();
    private final WrenchHelper wrenchHelper = new WrenchHelperImpl();
    private final NetworkManager networkManager = new NetworkManager();
    private final ServerToClientCommunications serverToClientCommunications = new ServerToClientCommunicationsImpl(networkManager);
    private final ClientToServerCommunications clientToServerCommunications = new ClientToServerCommunicationsImpl(networkManager);
    private final FluidRenderer fluidRenderer = new FluidStackFluidRenderer();

    public PlatformAbstractionsImpl() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.getSpec());
    }

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
        return FluidAttributes.BUCKET_VOLUME;
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
        return config;
    }

    @Override
    public boolean canEditBoxLoseFocus(EditBox editBox) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isKeyDown(KeyMapping keyMapping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInventoryIndexOfSlot(Slot slot) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemGridEventHandler createItemGridEventHandler(AbstractContainerMenu containerMenu, GridService<ItemResource> gridService, Inventory playerInventory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FluidGridEventHandler createFluidGridEventHandler(AbstractContainerMenu containerMenu, GridService<FluidResource> gridService, Inventory playerInventory, ExtractableStorage<ItemResource> bucketStorage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Function<ResourceAmount<ItemResource>, GridResource<ItemResource>> getItemGridResourceFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Function<ResourceAmount<FluidResource>, GridResource<FluidResource>> getFluidGridResourceFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FluidRenderer getFluidRenderer() {
        return fluidRenderer;
    }

    @Override
    public Optional<FluidResource> convertToFluid(ItemStack stack) {
        return stack
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                .map(handler -> handler.getFluidInTank(0))
                .map(contents -> contents.isEmpty() ? null : new FluidResource(contents.getFluid(), contents.getTag()));
    }

    @Override
    public EnergyStorage createEnergyStorage(ControllerType controllerType, Runnable listener) {
        return switch (controllerType) {
            case NORMAL -> new ControllerForgeEnergy(listener);
            case CREATIVE -> new InfiniteEnergyStorage();
        };
    }

    @Override
    public void setEnergy(EnergyStorage energyStorage, long stored) {
        if (energyStorage instanceof ControllerForgeEnergy controllerForgeEnergy) {
            controllerForgeEnergy.setSilently(stored);
        }
    }
}