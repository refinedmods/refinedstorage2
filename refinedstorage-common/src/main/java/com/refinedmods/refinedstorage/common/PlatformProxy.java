package com.refinedmods.refinedstorage.common;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.api.support.resource.FluidOperationResult;
import com.refinedmods.refinedstorage.common.support.RecipeProvider;
import com.refinedmods.refinedstorage.common.support.containermenu.MenuOpener;
import com.refinedmods.refinedstorage.common.support.containermenu.TransferManager;
import com.refinedmods.refinedstorage.common.support.render.FluidRenderer;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.client.KeyMapping;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class PlatformProxy implements Platform {
    @Nullable
    private Platform platform;

    public static void loadPlatform(final Platform platform) {
        final PlatformProxy proxy = (PlatformProxy) INSTANCE;
        if (proxy.platform != null) {
            throw new IllegalStateException("Platform already set");
        }
        proxy.platform = platform;
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
    public Config getConfig() {
        return ensureLoaded().getConfig();
    }

    @Override
    public boolean canEditBoxLoseFocus(final EditBox editBox) {
        return ensureLoaded().canEditBoxLoseFocus(editBox);
    }

    @Override
    public boolean isKeyDown(final KeyMapping keyMapping) {
        return ensureLoaded().isKeyDown(keyMapping);
    }

    @Override
    public GridInsertionStrategyFactory getDefaultGridInsertionStrategyFactory() {
        return ensureLoaded().getDefaultGridInsertionStrategyFactory();
    }

    @Override
    public FluidRenderer getFluidRenderer() {
        return ensureLoaded().getFluidRenderer();
    }

    @Override
    public Optional<FluidOperationResult> drainContainer(final ItemStack container) {
        return ensureLoaded().drainContainer(container);
    }

    @Override
    public Optional<FluidOperationResult> fillContainer(final ItemStack container,
                                                        final ResourceAmount resourceAmount) {
        return ensureLoaded().fillContainer(container, resourceAmount);
    }

    @Override
    public TransferManager createTransferManager(final AbstractContainerMenu containerMenu) {
        return ensureLoaded().createTransferManager(containerMenu);
    }

    @Override
    public long insertIntoContainer(final Container container,
                                    final ItemResource itemResource,
                                    final long amount,
                                    final Action action) {
        return ensureLoaded().insertIntoContainer(container, itemResource, amount, action);
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state,
                                       final Level level,
                                       final BlockHitResult hitResult,
                                       final Player player) {
        return ensureLoaded().getCloneItemStack(state, level, hitResult, player);
    }

    @Override
    public NonNullList<ItemStack> getRemainingCraftingItems(final Player player,
                                                            final CraftingRecipe craftingRecipe,
                                                            final CraftingInput input) {
        return ensureLoaded().getRemainingCraftingItems(player, craftingRecipe, input);
    }

    @Override
    public void onItemCrafted(final Player player, final ItemStack craftedStack, final CraftingContainer container) {
        ensureLoaded().onItemCrafted(player, craftedStack, container);
    }

    @Override
    public Player getFakePlayer(final ServerLevel level, @Nullable final UUID playerId) {
        return ensureLoaded().getFakePlayer(level, playerId);
    }

    @Override
    public boolean canBreakBlock(final Level level, final BlockPos pos, final BlockState state, final Player player) {
        return ensureLoaded().canBreakBlock(level, pos, state, player);
    }

    @Override
    public boolean placeBlock(final Level level,
                              final BlockPos pos,
                              final Direction direction,
                              final Player player,
                              final ItemStack stack) {
        return ensureLoaded().placeBlock(level, pos, direction, player, stack);
    }

    @Override
    public boolean placeFluid(final Level level,
                              final BlockPos pos,
                              final Direction direction,
                              final Player player,
                              final FluidResource fluidResource) {
        return ensureLoaded().placeFluid(level, pos, direction, player, fluidResource);
    }

    @Override
    public ItemStack getBlockAsItemStack(final Block block,
                                         final BlockState state,
                                         final Direction direction,
                                         final LevelReader level,
                                         final BlockPos pos,
                                         final Player player) {
        return ensureLoaded().getBlockAsItemStack(block, state, direction, level, pos, player);
    }

    @Override
    public Optional<SoundEvent> getBucketPickupSound(final LiquidBlock liquidBlock, final BlockState state) {
        return ensureLoaded().getBucketPickupSound(liquidBlock, state);
    }

    @Override
    public List<ClientTooltipComponent> processTooltipComponents(final ItemStack stack,
                                                                 final GuiGraphicsExtractor graphics,
                                                                 final int mouseX,
                                                                 final Optional<TooltipComponent> imageComponent,
                                                                 final List<Component> components) {
        return ensureLoaded().processTooltipComponents(stack, graphics, mouseX, imageComponent, components);
    }

    @Override
    public Optional<EnergyStorage> getEnergyStorage(final ItemStack stack, final EnergyItemContext context) {
        return ensureLoaded().getEnergyStorage(stack, context);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToServer(final T packet) {
        ensureLoaded().sendPacketToServer(packet);
    }

    @Override
    public <T extends CustomPacketPayload> void sendPacketToClient(final ServerPlayer player, final T packet) {
        ensureLoaded().sendPacketToClient(player, packet);
    }

    @Nullable
    @Override
    public NetworkNodeContainerProvider getContainerProvider(final Level level,
                                                             final BlockPos pos,
                                                             @Nullable final Direction direction) {
        return ensureLoaded().getContainerProvider(level, pos, direction);
    }

    @Nullable
    @Override
    public NetworkNodeContainerProvider getContainerProviderSafely(final Level level,
                                                                   final BlockPos pos,
                                                                   @Nullable final Direction direction) {
        return ensureLoaded().getContainerProviderSafely(level, pos, direction);
    }

    @Override
    public void setSlotY(final Slot slot, final int y) {
        ensureLoaded().setSlotY(slot, y);
    }

    @Override
    public void requestModelDataUpdateOnClient(final BlockEntity blockEntity, final boolean updateChunk) {
        ensureLoaded().requestModelDataUpdateOnClient(blockEntity, updateChunk);
    }

    @Override
    public void updateImageHeight(final AbstractContainerScreen<?> screen, final int height) {
        ensureLoaded().updateImageHeight(screen, height);
    }

    @Override
    public RecipeProvider getClientRecipeProvider(final Level level) {
        return ensureLoaded().getClientRecipeProvider(level);
    }

    @Override
    public void setClientRecipeProvider(final RecipeProvider recipeProvider) {
        ensureLoaded().setClientRecipeProvider(recipeProvider);
    }

    @Override
    public void resetSlots(final AbstractContainerMenu containerMenu) {
        ensureLoaded().resetSlots(containerMenu);
    }

    private Platform ensureLoaded() {
        if (platform == null) {
            throw new IllegalStateException("Platform not loaded yet");
        }
        return platform;
    }
}
