package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.InfiniteEnergyStorage;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.AbstractPlatform;
import com.refinedmods.refinedstorage2.platform.common.Config;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.transfer.TransferManager;
import com.refinedmods.refinedstorage2.platform.common.util.BucketAmountFormatting;
import com.refinedmods.refinedstorage2.platform.forge.containermenu.ContainerTransferDestination;
import com.refinedmods.refinedstorage2.platform.forge.integration.energy.ControllerForgeEnergy;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.ItemGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.view.ForgeFluidGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.view.ForgeItemGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.forge.menu.MenuOpenerImpl;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;
import com.refinedmods.refinedstorage2.platform.forge.packet.c2s.ClientToServerCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.forge.packet.s2c.ServerToClientCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.forge.render.FluidStackFluidRenderer;

import java.util.Optional;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

public final class PlatformImpl extends AbstractPlatform {
    private static final TagKey<Item> WRENCH_TAG = TagKey.create(
        ForgeRegistries.ITEMS.getRegistryKey(),
        new ResourceLocation("forge", "tools/wrench")
    );

    private final ConfigImpl config = new ConfigImpl();

    public PlatformImpl(final NetworkManager networkManager) {
        super(
            new ServerToClientCommunicationsImpl(networkManager),
            new ClientToServerCommunicationsImpl(networkManager),
            new MenuOpenerImpl(),
            new BucketAmountFormatting(FluidType.BUCKET_VOLUME),
            new FluidStackFluidRenderer(),
            ItemGridInsertionStrategy::new
        );
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.getSpec());
    }

    @Override
    public long getBucketAmount() {
        return FluidType.BUCKET_VOLUME;
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
    public boolean canEditBoxLoseFocus(final EditBox editBox) {
        return editBox.canLoseFocus;
    }

    @Override
    public boolean isKeyDown(final KeyMapping keyMapping) {
        return InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow().getWindow(),
            keyMapping.getKey().getValue()
        );
    }

    @Override
    public GridResourceFactory getItemGridResourceFactory() {
        return new ForgeItemGridResourceFactory();
    }

    @Override
    public GridResourceFactory getFluidGridResourceFactory() {
        return new ForgeFluidGridResourceFactory();
    }

    @Override
    public Optional<FluidResource> convertToFluid(final ItemStack stack) {
        return stack
            .getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null)
            .map(handler -> handler.getFluidInTank(0))
            .map(contents -> contents.isEmpty() ? null : new FluidResource(contents.getFluid(), contents.getTag()));
    }

    @Override
    public EnergyStorage createEnergyStorage(final ControllerType controllerType, final Runnable listener) {
        return switch (controllerType) {
            case NORMAL -> new ControllerForgeEnergy(listener);
            case CREATIVE -> new InfiniteEnergyStorage();
        };
    }

    @Override
    public void setEnergy(final EnergyStorage energyStorage, final long stored) {
        if (energyStorage instanceof ControllerForgeEnergy controllerForgeEnergy) {
            controllerForgeEnergy.setSilently(stored);
        }
    }

    @Override
    public TransferManager createTransferManager(final AbstractContainerMenu containerMenu) {
        return new TransferManager(containerMenu, ContainerTransferDestination::new);
    }

    @Override
    public long insertIntoContainer(final Container container,
                                    final ItemResource itemResource,
                                    final long amount,
                                    final Action action) {
        final InvWrapper wrapper = new InvWrapper(container);
        final ItemStack stack = itemResource.toItemStack(amount);
        final ItemStack remainder = ItemHandlerHelper.insertItem(wrapper, stack, action == Action.SIMULATE);
        return amount - remainder.getCount();
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state,
                                       final Level level,
                                       final BlockHitResult hitResult,
                                       final Player player) {
        return state.getCloneItemStack(hitResult, level, hitResult.getBlockPos(), player);
    }
}
