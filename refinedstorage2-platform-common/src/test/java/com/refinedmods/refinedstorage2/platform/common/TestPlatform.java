package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.support.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.support.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.TransferManager;
import com.refinedmods.refinedstorage2.platform.common.support.render.FluidRenderer;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TestPlatform implements Platform {
    private final long bucketAmount;

    public TestPlatform(final long bucketAmount) {
        this.bucketAmount = bucketAmount;
    }

    @Override
    public ServerToClientCommunications getServerToClientCommunications() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientToServerCommunications getClientToServerCommunications() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MenuOpener getMenuOpener() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getBucketAmount() {
        return bucketAmount;
    }

    @Override
    public TagKey<Item> getWrenchTag() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Config getConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canEditBoxLoseFocus(final EditBox editBox) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isKeyDown(final KeyMapping keyMapping) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GridResourceFactory getItemGridResourceFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GridResourceFactory getFluidGridResourceFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GridInsertionStrategyFactory getDefaultGridInsertionStrategyFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FluidRenderer getFluidRenderer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ContainedFluid> getContainedFluid(final ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<FluidResource> convertJeiIngredientToFluid(final Object ingredient) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ItemStack> convertToBucket(final FluidResource fluidResource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransferManager createTransferManager(final AbstractContainerMenu containerMenu) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long insertIntoContainer(final Container container, final ItemResource itemResource, final long amount,
                                    final Action action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state, final Level level, final BlockHitResult hitResult,
                                       final Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NonNullList<ItemStack> getRemainingCraftingItems(final Player player, final CraftingRecipe craftingRecipe,
                                                            final CraftingContainer container) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onItemCrafted(final Player player, final ItemStack craftedStack, final CraftingContainer container) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Player getFakePlayer(final ServerLevel level, @Nullable final UUID playerId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canBreakBlock(final Level level, final BlockPos pos, final BlockState state, final Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean placeBlock(final Level level, final BlockPos pos, final Direction direction, final Player player,
                              final ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean placeFluid(final Level level, final BlockPos pos, final Direction direction, final Player player,
                              final FluidResource fluidResource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getBlockAsItemStack(final Block block, final BlockState state, final Direction direction,
                                         final LevelReader level,
                                         final BlockPos pos, final Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SoundEvent> getBucketPickupSound(final LiquidBlock liquidBlock, final BlockState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClientTooltipComponent> processTooltipComponents(final ItemStack stack, final GuiGraphics graphics,
                                                                 final int mouseX,
                                                                 final Optional<TooltipComponent> imageComponent,
                                                                 final List<Component> components) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void renderTooltip(final GuiGraphics graphics, final List<ClientTooltipComponent> components, final int x,
                              final int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<EnergyStorage> getEnergyStorage(final ItemStack stack) {
        throw new UnsupportedOperationException();
    }
}
