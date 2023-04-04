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

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface Platform {
    Platform INSTANCE = new PlatformProxy();

    ServerToClientCommunications getServerToClientCommunications();

    ClientToServerCommunications getClientToServerCommunications();

    MenuOpener getMenuOpener();

    long getBucketAmount();

    TagKey<Item> getWrenchTag();

    BucketAmountFormatting getBucketAmountFormatter();

    Config getConfig();

    boolean canEditBoxLoseFocus(EditBox editBox);

    boolean isKeyDown(KeyMapping keyMapping);

    GridResourceFactory getItemGridResourceFactory();

    GridResourceFactory getFluidGridResourceFactory();

    GridInsertionStrategyFactory getDefaultGridInsertionStrategyFactory();

    FluidRenderer getFluidRenderer();

    Optional<FluidResource> convertToFluid(ItemStack stack);

    EnergyStorage createEnergyStorage(ControllerType controllerType, Runnable listener);

    void setEnergy(EnergyStorage energyStorage, long stored);

    TransferManager createTransferManager(AbstractContainerMenu containerMenu);

    long insertIntoContainer(Container container, ItemResource itemResource, long amount, Action action);

    ItemStack getCloneItemStack(BlockState state, Level level, BlockHitResult hitResult, Player player);

    NonNullList<ItemStack> getRemainingCraftingItems(Player player,
                                                     CraftingRecipe craftingRecipe,
                                                     CraftingContainer container);

    void onItemCrafted(Player player, ItemStack craftedStack, CraftingContainer container);

    Player getFakePlayer(ServerLevel level, @Nullable UUID playerId);

    boolean canBreakBlock(Level level, BlockPos pos, BlockState state, Player player);

    ItemStack getBlockAsItemStack(Block block,
                                  BlockState state,
                                  Direction direction,
                                  BlockGetter level,
                                  BlockPos pos,
                                  Player player);

    Optional<SoundEvent> getBucketPickupSound(LiquidBlock liquidBlock, BlockState state);
}
