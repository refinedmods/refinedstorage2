package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.AbstractPlatform;
import com.refinedmods.refinedstorage2.platform.common.Config;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.TransferManager;
import com.refinedmods.refinedstorage2.platform.common.util.BucketAmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.util.CustomBlockPlaceContext;
import com.refinedmods.refinedstorage2.platform.fabric.grid.strategy.ItemGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.grid.view.FabricFluidGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.fabric.grid.view.FabricItemGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.EditBoxAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.KeyMappingAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ClientToServerCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ServerToClientCommunicationsImpl;
import com.refinedmods.refinedstorage2.platform.fabric.support.containermenu.ContainerTransferDestination;
import com.refinedmods.refinedstorage2.platform.fabric.support.containermenu.MenuOpenerImpl;
import com.refinedmods.refinedstorage2.platform.fabric.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.FluidVariantFluidRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.util.SimpleSingleStackStorage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
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
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static com.refinedmods.refinedstorage2.platform.fabric.support.resource.VariantUtil.ofFluidVariant;
import static com.refinedmods.refinedstorage2.platform.fabric.support.resource.VariantUtil.toFluidVariant;
import static com.refinedmods.refinedstorage2.platform.fabric.support.resource.VariantUtil.toItemVariant;

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
            new BucketAmountFormatting(FluidConstants.BUCKET),
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
        return !keyMapping.isUnbound() && InputConstants.isKeyDown(
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
    public Optional<ContainedFluid> getContainedFluid(final ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        final SimpleSingleStackStorage interceptingStorage = new SimpleSingleStackStorage(stack);
        final Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, ContainerItemContext.ofSingleSlot(
            interceptingStorage
        ));
        try (Transaction tx = Transaction.openOuter()) {
            final var extracted = StorageUtil.extractAny(storage, Long.MAX_VALUE, tx);
            if (extracted == null) {
                return Optional.empty();
            }
            return Optional.of(new ContainedFluid(
                interceptingStorage.getStack(),
                new ResourceAmount<>(ofFluidVariant(extracted.resource()), extracted.amount())
            ));
        }
    }

    @Override
    public Optional<FluidResource> convertJeiIngredientToFluid(final Object ingredient) {
        if (ingredient instanceof IJeiFluidIngredient fluidIngredient) {
            return Optional.of(new FluidResource(
                fluidIngredient.getFluid(),
                fluidIngredient.getTag().orElse(null)
            ));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ItemStack> convertToBucket(final FluidResource fluidResource) {
        final SimpleSingleStackStorage interceptingStorage = SimpleSingleStackStorage.forEmptyBucket();
        final Storage<FluidVariant> destination = FluidStorage.ITEM.find(
            interceptingStorage.getStack(),
            ContainerItemContext.ofSingleSlot(interceptingStorage)
        );
        if (destination == null) {
            return Optional.empty();
        }
        try (Transaction tx = Transaction.openOuter()) {
            destination.insert(toFluidVariant(fluidResource), FluidConstants.BUCKET, tx);
            return Optional.of(interceptingStorage.getStack());
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
                                                            final CraftingContainer craftingContainer) {
        return craftingRecipe.getRemainingItems(craftingContainer);
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
            || (block instanceof LiquidBlockContainer lbc && lbc.canPlaceLiquid(level, pos, blockState, content));
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
                                         final BlockGetter level,
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
}
