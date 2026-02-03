package com.refinedmods.refinedstorage.neoforge;

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
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;
import com.refinedmods.refinedstorage.neoforge.cape.TenthAnniversaryCape;
import com.refinedmods.refinedstorage.neoforge.grid.strategy.ItemGridInsertionStrategy;
import com.refinedmods.refinedstorage.neoforge.support.containermenu.ContainerTransferDestination;
import com.refinedmods.refinedstorage.neoforge.support.containermenu.MenuOpenerImpl;
import com.refinedmods.refinedstorage.neoforge.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage.neoforge.support.render.FluidStackFluidRenderer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
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
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toFluidStack;

public final class PlatformImpl extends AbstractPlatform {
    private final ConfigImpl config = new ConfigImpl();

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
            Minecraft.getInstance().getWindow().getWindow(),
            keyMapping.getKey().getValue()
        );
    }

    @Override
    public Optional<FluidOperationResult> drainContainer(final ItemStack container) {
        final FluidTank tank = new FluidTank(Integer.MAX_VALUE);
        final FluidActionResult result = FluidUtil.tryEmptyContainer(
            container,
            tank,
            Integer.MAX_VALUE,
            null,
            true
        );
        if (!result.isSuccess() || tank.isEmpty()) {
            return Optional.empty();
        }
        final FluidResource fluidResource = ofFluidStack(tank.getFluid());
        return Optional.of(new FluidOperationResult(
            result.getResult(),
            fluidResource,
            tank.getFluidAmount()
        ));
    }

    @Override
    public Optional<FluidOperationResult> fillContainer(final ItemStack container,
                                                        final ResourceAmount resourceAmount) {
        if (!(resourceAmount.resource() instanceof FluidResource fluidResource)) {
            return Optional.empty();
        }
        return FluidUtil.getFluidHandler(container.copy()).map(handler -> {
            final FluidStack fluidStack = toFluidStack(fluidResource, resourceAmount.amount());
            final long filled = handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            return new FluidOperationResult(handler.getContainer(), fluidResource, filled);
        });
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
            .flatMap(id -> level.getServer().getProfileCache().get(id))
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
        final FluidStack stack = toFluidStack(fluidResource, FluidType.BUCKET_VOLUME);
        final FluidTank tank = new FluidTank(FluidType.BUCKET_VOLUME);
        tank.fill(stack, IFluidHandler.FluidAction.EXECUTE);
        return FluidUtil.tryPlaceFluid(
            player,
            level,
            InteractionHand.MAIN_HAND,
            pos,
            tank,
            toFluidStack(fluidResource, FluidType.BUCKET_VOLUME)
        );
    }

    @Override
    public ItemStack getBlockAsItemStack(final Block block,
                                         final BlockState state,
                                         final Direction direction,
                                         final LevelReader level,
                                         final BlockPos position,
                                         final Player player) {
        return block.getCloneItemStack(
            state,
            new BlockHitResult(Vec3.ZERO, direction, position, false),
            level,
            position,
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
        final GuiGraphics graphics,
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
        return Optional.ofNullable(stack.getCapability(Capabilities.EnergyStorage.ITEM))
            .filter(EnergyStorageAdapter.class::isInstance)
            .map(EnergyStorageAdapter.class::cast)
            .map(EnergyStorageAdapter::energyStorage);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToServer(final T packet) {
        PacketDistributor.sendToServer(packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToClient(final ServerPlayer player, final T packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public void saveSavedData(final SavedData savedData,
                              final File file,
                              final HolderLookup.Provider provider,
                              final BiConsumer<File, HolderLookup.Provider> defaultSaveFunction) {
        defaultSaveFunction.accept(file, provider);
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
    public void setTenthAnniversaryCape(final Player player, final boolean enabled) {
        player.setData(TenthAnniversaryCape.getAttachment(), enabled);
    }
}
