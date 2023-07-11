package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.transfer.TransferManager;
import com.refinedmods.refinedstorage2.platform.common.menu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.common.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.common.render.FluidRenderer;
import com.refinedmods.refinedstorage2.platform.common.util.BucketAmountFormatting;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PlatformProxy implements Platform {
    @Nullable
    private Platform platform;

    public void setPlatform(final Platform platform) {
        if (this.platform != null) {
            throw new IllegalStateException("Platform already set");
        }
        this.platform = platform;
    }

    @Override
    public ServerToClientCommunications getServerToClientCommunications() {
        return ensureLoaded().getServerToClientCommunications();
    }

    @Override
    public ClientToServerCommunications getClientToServerCommunications() {
        return ensureLoaded().getClientToServerCommunications();
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
    public TagKey<Item> getWrenchTag() {
        return ensureLoaded().getWrenchTag();
    }

    @Override
    public BucketAmountFormatting getBucketAmountFormatter() {
        return ensureLoaded().getBucketAmountFormatter();
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
    public GridResourceFactory getItemGridResourceFactory() {
        return ensureLoaded().getItemGridResourceFactory();
    }

    @Override
    public GridResourceFactory getFluidGridResourceFactory() {
        return ensureLoaded().getFluidGridResourceFactory();
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
    public Optional<FluidResource> convertToFluid(final ItemStack stack) {
        return ensureLoaded().convertToFluid(stack);
    }

    @Override
    public EnergyStorage createEnergyStorage(final ControllerType controllerType, final Runnable listener) {
        return ensureLoaded().createEnergyStorage(controllerType, listener);
    }

    @Override
    public void setEnergy(final EnergyStorage energyStorage, final long stored) {
        ensureLoaded().setEnergy(energyStorage, stored);
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
                                                            final CraftingContainer container) {
        return ensureLoaded().getRemainingCraftingItems(player, craftingRecipe, container);
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
                                         final BlockGetter level,
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
                                                                 final GuiGraphics graphics,
                                                                 final int mouseX,
                                                                 final Optional<TooltipComponent> imageComponent,
                                                                 final List<Component> components) {
        return ensureLoaded().processTooltipComponents(stack, graphics, mouseX, imageComponent, components);
    }

    @Override
    public void renderTooltip(final GuiGraphics graphics,
                              final List<ClientTooltipComponent> components,
                              final int x,
                              final int y) {
        ensureLoaded().renderTooltip(graphics, components, x, y);
    }

    private Platform ensureLoaded() {
        if (platform == null) {
            throw new IllegalStateException("Platform not loaded yet");
        }
        return platform;
    }
}
