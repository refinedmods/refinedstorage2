package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.energy.InfiniteEnergyStorage;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.api.network.ControllerType;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.AbstractPlatform;
import com.refinedmods.refinedstorage2.platform.common.Config;
import com.refinedmods.refinedstorage2.platform.common.util.BucketQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.forge.integration.energy.ControllerForgeEnergy;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.FluidGridEventHandlerImpl;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.ItemGridEventHandlerImpl;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.view.ForgeFluidGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.view.ForgeItemGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.forge.menu.MenuOpenerImpl;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;
import com.refinedmods.refinedstorage2.platform.forge.packet.c2s.ClientToServerCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.forge.packet.s2c.ServerToClientCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.forge.render.FluidStackFluidRenderer;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class PlatformImpl extends AbstractPlatform {
    private static final TagKey<Item> WRENCH_TAG = TagKey.create(Registry.ITEM.key(), new ResourceLocation("forge", "tools/wrench"));

    private final ConfigImpl config = new ConfigImpl();

    public PlatformImpl(NetworkManager networkManager) {
        super(new ServerToClientCommunicationsImpl(networkManager), new ClientToServerCommunicationsImpl(networkManager), new MenuOpenerImpl(), new BucketQuantityFormatter(FluidAttributes.BUCKET_VOLUME), new FluidStackFluidRenderer());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.getSpec());
    }

    @Override
    public long getBucketAmount() {
        return FluidAttributes.BUCKET_VOLUME;
    }

    @Override
    public TagKey<Item> getWrenchTag() {
        return WRENCH_TAG;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public boolean canEditBoxLoseFocus(EditBox editBox) {
        return editBox.canLoseFocus;
    }

    @Override
    public boolean isKeyDown(KeyMapping keyMapping) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyMapping.getKey().getValue());
    }

    @Override
    public ItemGridEventHandler createItemGridEventHandler(AbstractContainerMenu containerMenu, GridService<ItemResource> gridService, Inventory playerInventory) {
        return new ItemGridEventHandlerImpl(containerMenu, gridService, playerInventory);
    }

    @Override
    public FluidGridEventHandler createFluidGridEventHandler(AbstractContainerMenu containerMenu, GridService<FluidResource> gridService, Inventory playerInventory, ExtractableStorage<ItemResource> bucketStorage) {
        return new FluidGridEventHandlerImpl(containerMenu, playerInventory, gridService, bucketStorage);
    }

    @Override
    public Function<ResourceAmount<ItemResource>, GridResource<ItemResource>> getItemGridResourceFactory() {
        return new ForgeItemGridResourceFactory();
    }

    @Override
    public Function<ResourceAmount<FluidResource>, GridResource<FluidResource>> getFluidGridResourceFactory() {
        return new ForgeFluidGridResourceFactory();
    }

    @Override
    public Optional<FluidResource> convertToFluid(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).map(handler -> handler.getFluidInTank(0)).map(contents -> contents.isEmpty() ? null : new FluidResource(contents.getFluid(), contents.getTag()));
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
