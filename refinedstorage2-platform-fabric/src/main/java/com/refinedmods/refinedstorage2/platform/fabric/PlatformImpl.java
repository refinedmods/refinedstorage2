package com.refinedmods.refinedstorage2.platform.fabric;

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
import com.refinedmods.refinedstorage2.platform.common.util.BucketQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.ContainerTransferDestination;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.ControllerTeamRebornEnergy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.ItemGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.FabricFluidGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.FabricItemGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.fabric.menu.MenuOpenerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.EditBoxAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.KeyMappingAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ClientToServerCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ServerToClientCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.fabric.render.FluidVariantFluidRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil;

import java.util.Optional;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.context.ConstantContainerItemContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
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

import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.toItemVariant;

public final class PlatformImpl extends AbstractPlatform {
    private static final TagKey<Item> WRENCH_TAG = TagKey.create(
        BuiltInRegistries.ITEM.key(),
        new ResourceLocation("c", "wrenches")
    );

    public PlatformImpl() {
        super(
            new ServerToClientCommunicationsImpl(),
            new ClientToServerCommunicationsImpl(),
            new MenuOpenerImpl(),
            new BucketQuantityFormatter(FluidConstants.BUCKET),
            new FluidVariantFluidRenderer(),
            ItemGridInsertionStrategy::new
        );
    }

    @Override
    public long getBucketAmount() {
        return FluidConstants.BUCKET;
    }

    @Override
    public TagKey<Item> getWrenchTag() {
        return WRENCH_TAG;
    }

    @Override
    public Config getConfig() {
        return ConfigImpl.get();
    }

    @Override
    public boolean canEditBoxLoseFocus(final EditBox editBox) {
        return ((EditBoxAccessor) editBox).getCanLoseFocus();
    }

    @Override
    public boolean isKeyDown(final KeyMapping keyMapping) {
        return InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow().getWindow(),
            ((KeyMappingAccessor) keyMapping).getKey().getValue()
        );
    }

    @Override
    public GridResourceFactory getItemGridResourceFactory() {
        return new FabricItemGridResourceFactory();
    }

    @Override
    public GridResourceFactory getFluidGridResourceFactory() {
        return new FabricFluidGridResourceFactory();
    }

    @Override
    public Optional<FluidResource> convertToFluid(final ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return convertNonEmptyToFluid(stack);
    }

    @Override
    public EnergyStorage createEnergyStorage(final ControllerType controllerType, final Runnable listener) {
        return switch (controllerType) {
            case NORMAL -> new ControllerTeamRebornEnergy(listener);
            case CREATIVE -> new InfiniteEnergyStorage();
        };
    }

    @Override
    public void setEnergy(final EnergyStorage energyStorage, final long stored) {
        if (energyStorage instanceof ControllerTeamRebornEnergy controllerTeamRebornEnergy) {
            controllerTeamRebornEnergy.setStoredSilently(stored);
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
        try (Transaction tx = Transaction.openOuter()) {
            final long inserted = InventoryStorage
                .of(container, null)
                .insert(toItemVariant(itemResource), amount, tx);
            if (action == Action.EXECUTE) {
                tx.commit();
            }
            return inserted;
        }
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state,
                                       final Level level,
                                       final BlockHitResult hitResult,
                                       final Player player) {
        return state.getBlock().getCloneItemStack(level, hitResult.getBlockPos(), state);
    }

    private Optional<FluidResource> convertNonEmptyToFluid(final ItemStack stack) {
        final Storage<FluidVariant> storage = FluidStorage.ITEM.find(
            stack,
            new ConstantContainerItemContext(ItemVariant.of(stack), 1)
        );
        return Optional
            .ofNullable(StorageUtil.findExtractableResource(storage, null))
            .map(VariantUtil::ofFluidVariant);
    }
}
