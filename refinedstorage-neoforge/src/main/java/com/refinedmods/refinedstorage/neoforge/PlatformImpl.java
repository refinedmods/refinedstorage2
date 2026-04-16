package com.refinedmods.refinedstorage.neoforge;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.AbstractPlatform;
import com.refinedmods.refinedstorage.common.Config;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.api.support.resource.FluidOperationResult;
import com.refinedmods.refinedstorage.common.support.RecipeMapRecipeProvider;
import com.refinedmods.refinedstorage.common.support.RecipeProvider;
import com.refinedmods.refinedstorage.common.support.containermenu.TransferManager;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.util.CustomBlockPlaceContext;
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;
import com.refinedmods.refinedstorage.neoforge.grid.strategy.ItemGridInsertionStrategy;
import com.refinedmods.refinedstorage.neoforge.support.containermenu.ContainerTransferDestination;
import com.refinedmods.refinedstorage.neoforge.support.containermenu.MenuOpenerImpl;
import com.refinedmods.refinedstorage.neoforge.support.energy.EnergyStorageEnergyHandlerAdapter;
import com.refinedmods.refinedstorage.neoforge.support.render.FluidStackFluidRenderer;
import com.refinedmods.refinedstorage.neoforge.support.resource.SimpleItemStackResourceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.ofPlatform;
import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toPlatform;
import static java.util.Objects.requireNonNull;

public final class PlatformImpl extends AbstractPlatform {
    private final ConfigImpl config = new ConfigImpl();
    @Nullable
    private RecipeProvider clientRecipeProvider;

    public PlatformImpl(final ModContainer modContainer) {
        super(new MenuOpenerImpl(), new FluidStackFluidRenderer(), ItemGridInsertionStrategy::new);
        modContainer.registerConfig(ModConfig.Type.COMMON, config.getSpec());
    }

    @Override
    public long getBucketAmount() {
        return FluidType.BUCKET_VOLUME;
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
        return !keyMapping.isUnbound() && InputConstants.isKeyDown(
            Minecraft.getInstance().getWindow(),
            keyMapping.getKey().getValue()
        );
    }

    @Override
    public Optional<FluidOperationResult> drainContainer(final ItemStack container) {
        if (container.isEmpty()) {
            return Optional.empty();
        }
        final SimpleItemStackResourceHandler interceptingHandler = SimpleItemStackResourceHandler.forStack(container);
        final ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> source =
            container.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forHandlerIndex(interceptingHandler, 0));
        try (Transaction tx = Transaction.openRoot()) {
            final var extracted = ResourceHandlerUtil.extractFirst(source, r -> true, Integer.MAX_VALUE,
                tx);
            if (extracted == null) {
                return Optional.empty();
            }
            return Optional.of(new FluidOperationResult(
                interceptingHandler.getStack(),
                ofPlatform(extracted.resource()),
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
        final SimpleItemStackResourceHandler interceptingHandler = SimpleItemStackResourceHandler.forStack(container);
        final ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> storage =
            container.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forHandlerIndex(interceptingHandler, 0));
        if (storage == null) {
            return Optional.empty();
        }
        try (Transaction tx = Transaction.openRoot()) {
            final long inserted = storage.insert(toPlatform(fluidResource), (int) resourceAmount.amount(), tx);
            return Optional.of(new FluidOperationResult(
                interceptingHandler.getStack(),
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
        final ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> wrapper =
            VanillaContainerWrapper.of(container);
        final net.neoforged.neoforge.transfer.item.ItemResource platformResource = toPlatform(itemResource);
        try (Transaction tx = Transaction.openRoot()) {
            final long inserted = wrapper.insert(platformResource, (int) amount, tx);
            if (inserted > 0 && action == Action.EXECUTE) {
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
        return state.getCloneItemStack(hitResult.getBlockPos(), level, false, player);
    }

    @Override
    @SuppressWarnings("DataFlowIssue") // NeoForge allows null
    public NonNullList<ItemStack> getRemainingCraftingItems(final Player player,
                                                            final CraftingRecipe craftingRecipe,
                                                            final CraftingInput input) {
        CommonHooks.setCraftingPlayer(player);
        final NonNullList<ItemStack> remainingItems = craftingRecipe.getRemainingItems(input);
        CommonHooks.setCraftingPlayer(null);
        return remainingItems;
    }

    @Override
    public void onItemCrafted(final Player player, final ItemStack craftedStack, final CraftingContainer container) {
        EventHooks.firePlayerCraftingEvent(player, craftedStack, container);
    }

    @Override
    public Player getFakePlayer(final ServerLevel level, @Nullable final UUID playerId) {
        return Optional.ofNullable(playerId)
            .flatMap(id -> level.getServer().services().profileResolver().fetchById(id))
            .map(profile -> FakePlayerFactory.get(level, profile))
            .orElseGet(() -> FakePlayerFactory.getMinecraft(level));
    }

    @Override
    public boolean canBreakBlock(final Level level, final BlockPos pos, final BlockState state, final Player player) {
        final BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(level, pos, state, player);
        return !NeoForge.EVENT_BUS.post(e).isCanceled();
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
        final InteractionResult result = CommonHooks.onPlaceItemIntoWorld(ctx);
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
        if (level.getBlockState(pos).getFluidState().isSource()) {
            return false;
        }
        final net.neoforged.neoforge.transfer.fluid.FluidResource platformResource = toPlatform(fluidResource);
        return FluidUtil.tryPlaceFluid(platformResource, player, level, InteractionHand.MAIN_HAND, pos);
    }

    @Override
    public ItemStack getBlockAsItemStack(final Block block,
                                         final BlockState state,
                                         final Direction direction,
                                         final LevelReader level,
                                         final BlockPos position,
                                         final Player player) {
        return block.getCloneItemStack(
            level,
            position,
            state,
            false,
            player
        );
    }

    @Override
    public Optional<SoundEvent> getBucketPickupSound(final LiquidBlock liquidBlock, final BlockState state) {
        return liquidBlock.getPickupSound(state);
    }

    @Override
    public List<ClientTooltipComponent> processTooltipComponents(
        final ItemStack stack,
        final GuiGraphicsExtractor graphics,
        final int mouseX,
        final Optional<TooltipComponent> imageComponent,
        final List<Component> components
    ) {
        return new ArrayList<>(ClientHooks.gatherTooltipComponents(
            stack,
            components,
            imageComponent,
            mouseX,
            graphics.guiWidth(),
            graphics.guiHeight(),
            Minecraft.getInstance().font
        )); // make modifiable
    }

    @Override
    public Optional<EnergyStorage> getEnergyStorage(final ItemStack stack) {
        return Optional.ofNullable(stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack)))
            .filter(EnergyStorageEnergyHandlerAdapter.class::isInstance)
            .map(EnergyStorageEnergyHandlerAdapter.class::cast)
            .map(EnergyStorageEnergyHandlerAdapter::getEnergyStorage);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToServer(final T packet) {
        ClientPacketDistributor.sendToServer(packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToClient(final ServerPlayer player, final T packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    @Nullable
    public NetworkNodeContainerProvider getContainerProvider(final Level level,
                                                             final BlockPos pos,
                                                             @Nullable final Direction direction) {
        return level.getCapability(
            RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
            pos,
            direction
        );
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
        return level.getCapability(
            RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
            pos,
            null,
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
        blockEntity.requestModelDataUpdate();
        if (updateChunk) {
            level.sendBlockUpdated(
                blockEntity.getBlockPos(),
                blockEntity.getBlockState(),
                blockEntity.getBlockState(),
                Block.UPDATE_ALL
            );
        }
    }

    @Override
    public void updateImageHeight(final AbstractContainerScreen<?> screen, final int height) {
        screen.imageHeight = height;
    }

    @Override
    public RecipeProvider getClientRecipeProvider(final Level level) {
        if (!level.isClientSide()) {
            return new RecipeMapRecipeProvider(requireNonNull(level.getServer()).getRecipeManager().recipeMap());
        }
        if (clientRecipeProvider == null) {
            throw new RuntimeException("Recipe provider is not synced yet");
        }
        return clientRecipeProvider;
    }

    @Override
    public void setClientRecipeProvider(final RecipeProvider recipeProvider) {
        this.clientRecipeProvider = recipeProvider;
    }

    @Override
    public void resetSlots(final AbstractContainerMenu containerMenu) {
        containerMenu.slots.clear();
        containerMenu.lastSlots.clear();
        containerMenu.remoteSlots.clear();
    }
}
