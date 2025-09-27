package com.refinedmods.refinedstorage.fabric;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.AbstractPlatform;
import com.refinedmods.refinedstorage.common.Config;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.api.support.resource.FluidOperationResult;
import com.refinedmods.refinedstorage.common.support.containermenu.TransferManager;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.util.CustomBlockPlaceContext;
import com.refinedmods.refinedstorage.fabric.api.RefinedStorageFabricApi;
import com.refinedmods.refinedstorage.fabric.grid.strategy.ItemGridInsertionStrategy;
import com.refinedmods.refinedstorage.fabric.mixin.EditBoxAccessor;
import com.refinedmods.refinedstorage.fabric.mixin.KeyMappingAccessor;
import com.refinedmods.refinedstorage.fabric.support.containermenu.ContainerTransferDestination;
import com.refinedmods.refinedstorage.fabric.support.containermenu.MenuOpenerImpl;
import com.refinedmods.refinedstorage.fabric.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage.fabric.support.render.FluidVariantFluidRenderer;
import com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil;
import com.refinedmods.refinedstorage.fabric.util.SimpleSingleStackStorage;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil.toFluidVariant;
import static com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil.toItemVariant;

public final class PlatformImpl extends AbstractPlatform {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformImpl.class);

    public PlatformImpl() {
        super(new MenuOpenerImpl(), new FluidVariantFluidRenderer(), ItemGridInsertionStrategy::new);
    }

    @Override
    public long getBucketAmount() {
        return FluidConstants.BUCKET;
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
        return !keyMapping.isUnbound() && InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow().getWindow(),
            ((KeyMappingAccessor) keyMapping).getKey().getValue()
        );
    }

    @Override
    public Optional<FluidOperationResult> drainContainer(final ItemStack container) {
        if (container.isEmpty()) {
            return Optional.empty();
        }
        final SimpleSingleStackStorage interceptingStorage = new SimpleSingleStackStorage(container);
        final Storage<FluidVariant> storage = FluidStorage.ITEM.find(container, ContainerItemContext.ofSingleSlot(
            interceptingStorage
        ));
        try (Transaction tx = Transaction.openOuter()) {
            final var extracted = StorageUtil.extractAny(storage, Long.MAX_VALUE, tx);
            if (extracted == null) {
                return Optional.empty();
            }
            return Optional.of(new FluidOperationResult(
                interceptingStorage.getStack(),
                VariantUtil.ofFluidVariant(extracted.resource()),
                extracted.amount()
            ));
        }
    }

    @Override
    public Optional<FluidOperationResult> fillContainer(final ItemStack container,
                                                        final ResourceAmount resourceAmount) {
        if (!(resourceAmount.resource() instanceof FluidResource fluidResource)) {
            return Optional.empty();
        }
        final SimpleSingleStackStorage interceptingStorage = new SimpleSingleStackStorage(container);
        final Storage<FluidVariant> storage = FluidStorage.ITEM.find(container, ContainerItemContext.ofSingleSlot(
            interceptingStorage
        ));
        if (storage == null) {
            return Optional.empty();
        }
        try (Transaction tx = Transaction.openOuter()) {
            final long inserted = storage.insert(toFluidVariant(fluidResource), resourceAmount.amount(), tx);
            return Optional.of(new FluidOperationResult(
                interceptingStorage.getStack(),
                fluidResource,
                inserted
            ));
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

    @Override
    public NonNullList<ItemStack> getRemainingCraftingItems(final Player player,
                                                            final CraftingRecipe craftingRecipe,
                                                            final CraftingInput input) {
        return craftingRecipe.getRemainingItems(input);
    }

    @Override
    public void onItemCrafted(final Player player, final ItemStack craftedStack, final CraftingContainer container) {
        // no op
    }

    @Override
    public Player getFakePlayer(final ServerLevel level, @Nullable final UUID playerId) {
        return Optional.ofNullable(playerId)
            .flatMap(id -> level.getServer().getProfileCache().get(id))
            .map(profile -> FakePlayer.get(level, profile))
            .orElseGet(() -> FakePlayer.get(level));
    }

    @Override
    public boolean canBreakBlock(final Level level, final BlockPos pos, final BlockState state, final Player player) {
        return PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(
            level,
            player,
            pos,
            state,
            level.getBlockEntity(pos)
        );
    }

    @Override
    public boolean placeBlock(
        final Level level,
        final BlockPos pos,
        final Direction direction,
        final Player player,
        final ItemStack stack
    ) {
        final BlockPlaceContext ctx = new CustomBlockPlaceContext(
            level,
            player,
            InteractionHand.MAIN_HAND,
            stack,
            new BlockHitResult(Vec3.ZERO, direction, pos, false)
        );
        final InteractionResult result = stack.useOn(ctx);
        return result.consumesAction();
    }

    @Override
    public boolean placeFluid(
        final Level level,
        final BlockPos pos,
        final Direction direction,
        final Player player,
        final FluidResource fluidResource
    ) {
        // Stolen from BucketItem#emptyContents
        final Fluid content = fluidResource.fluid();
        if (!(content instanceof FlowingFluid)) {
            return false;
        }
        final BlockState blockState = level.getBlockState(pos);
        final Block block = blockState.getBlock();
        final boolean replaceable = blockState.canBeReplaced(content);
        final boolean canPlace = blockState.isAir()
            || replaceable
            || (block instanceof LiquidBlockContainer lbc
            && lbc.canPlaceLiquid(player, level, pos, blockState, content));
        if (!canPlace || blockState.getFluidState().isSource()) {
            return false;
        } else if (block instanceof LiquidBlockContainer lbc && content == Fluids.WATER) {
            lbc.placeLiquid(level, pos, blockState, ((FlowingFluid) content).getSource(false));
            playEmptySound(content, player, level, pos);
            return true;
        }
        return doPlaceFluid(level, pos, player, content, blockState, replaceable);
    }

    @SuppressWarnings("deprecation")
    private boolean doPlaceFluid(final Level level,
                                 final BlockPos pos,
                                 final Player player,
                                 final Fluid content,
                                 final BlockState blockState,
                                 final boolean replaceable) {
        if (replaceable && !blockState.liquid()) {
            level.destroyBlock(pos, true);
        }
        if (!level.setBlock(pos, content.defaultFluidState().createLegacyBlock(), 11)
            && !blockState.getFluidState().isSource()) {
            return false;
        }
        playEmptySound(content, player, level, pos);
        return true;
    }

    @SuppressWarnings("deprecation")
    private void playEmptySound(final Fluid content, final Player player, final LevelAccessor level,
                                final BlockPos pos) {
        final SoundEvent soundEvent = content.is(FluidTags.LAVA)
            ? SoundEvents.BUCKET_EMPTY_LAVA
            : SoundEvents.BUCKET_EMPTY;
        level.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }

    @Override
    public ItemStack getBlockAsItemStack(final Block block,
                                         final BlockState state,
                                         final Direction direction,
                                         final LevelReader level,
                                         final BlockPos position,
                                         final Player player) {
        return block.getCloneItemStack(level, position, state);
    }

    @Override
    public Optional<SoundEvent> getBucketPickupSound(final LiquidBlock liquidBlock, final BlockState state) {
        return liquidBlock.getPickupSound();
    }

    @Override
    public List<ClientTooltipComponent> processTooltipComponents(final ItemStack stack,
                                                                 final GuiGraphics graphics,
                                                                 final int mouseX,
                                                                 final Optional<TooltipComponent> imageComponent,
                                                                 final List<Component> components) {
        final List<ClientTooltipComponent> processedComponents = components
            .stream()
            .map(Component::getVisualOrderText)
            .map(ClientTooltipComponent::create)
            .collect(Collectors.toList());
        imageComponent.ifPresent(image -> processedComponents.add(1, ClientTooltipComponent.create(image)));
        return processedComponents;
    }

    @Override
    public void renderTooltip(final GuiGraphics graphics,
                              final List<ClientTooltipComponent> components,
                              final int x,
                              final int y) {
        graphics.renderTooltipInternal(
            Minecraft.getInstance().font,
            components,
            x,
            y,
            DefaultTooltipPositioner.INSTANCE
        );
    }

    @Override
    public Optional<EnergyStorage> getEnergyStorage(final ItemStack stack) {
        final ConstantContainerItemContext ctx = new ConstantContainerItemContext(
            ItemVariant.of(stack),
            stack.getCount()
        );
        return Optional.ofNullable(team.reborn.energy.api.EnergyStorage.ITEM.find(stack, ctx))
            .filter(EnergyStorageAdapter.class::isInstance)
            .map(EnergyStorageAdapter.class::cast)
            .map(EnergyStorageAdapter::getEnergyStorage);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToServer(final T packet) {
        ClientPlayNetworking.send(packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToClient(final ServerPlayer player, final T packet) {
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public void saveSavedData(final SavedData savedData,
                              final File file,
                              final HolderLookup.Provider provider,
                              final BiConsumer<File, HolderLookup.Provider> defaultSaveFunction) {
        if (!savedData.isDirty()) {
            return;
        }
        final var targetPath = file.toPath().toAbsolutePath();
        final var tempFile = targetPath.getParent().resolve(file.getName() + ".temp");
        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("data", savedData.save(new CompoundTag(), provider));
        NbtUtils.addCurrentDataVersion(compoundTag);
        try {
            doSave(compoundTag, tempFile, targetPath);
        } catch (final IOException e) {
            LOGGER.error("Could not save data", e);
        }
        savedData.setDirty(false);
    }

    @Nullable
    @Override
    public NetworkNodeContainerProvider getContainerProvider(final Level level,
                                                             final BlockPos pos,
                                                             @Nullable final Direction direction) {
        return RefinedStorageFabricApi.INSTANCE.getNetworkNodeContainerProviderLookup().find(level, pos, direction);
    }

    @Nullable
    @Override
    public NetworkNodeContainerProvider getContainerProviderSafely(final Level level,
                                                                   final BlockPos pos,
                                                                   @Nullable final Direction direction) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        // Avoid using EntityCreationType.IMMEDIATE.
        // By default, the block is removed first and then the block entity (see BaseBlock#onRemove).
        // But, when using mods like "Carrier", "Carpet" or "Carry On" that allow for moving block entities,
        // they remove the block entity first and then the block.
        // When removing a block with Carrier for example,
        // this causes a problematic situation that the block entity IS gone,
        // but that the #getBlockEntity() call here with type IMMEDIATE would recreate the block entity because
        // the block is still there.
        // If the block entity is returned here again even if it is removed, the preconditions in NetworkBuilder will
        // fail as the "removed" block entity/connection would still be present.
        final BlockEntity safeBlockEntity = level.getChunkAt(pos).getBlockEntity(
            pos,
            LevelChunk.EntityCreationType.CHECK
        );
        if (safeBlockEntity == null) {
            return null;
        }
        return RefinedStorageFabricApi.INSTANCE.getNetworkNodeContainerProviderLookup().find(
            level,
            pos,
            level.getBlockState(pos),
            safeBlockEntity,
            direction
        );
    }

    @Override
    public void setSlotY(final Slot slot, final int y) {
        slot.y = y;
    }

    @Override
    public void requestModelDataUpdateOnClient(final BlockEntity blockEntity, final boolean updateChunk) {
        final Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        if (!level.isClientSide()) {
            throw new IllegalArgumentException("Cannot request model data update on server");
        }
        if (updateChunk) {
            level.sendBlockUpdated(
                blockEntity.getBlockPos(),
                blockEntity.getBlockState(),
                blockEntity.getBlockState(),
                Block.UPDATE_ALL
            );
        }
    }

    private void doSave(final CompoundTag compoundTag, final Path tempFile, final Path targetPath) throws IOException {
        // Write to temp file first.
        NbtIo.writeCompressed(compoundTag, tempFile);
        // Try atomic move
        try {
            Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (final AtomicMoveNotSupportedException ignored) {
            Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
