package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.service.GridService;
import com.refinedmods.refinedstorage2.api.grid.view.GridResourceFactory;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.transfer.TransferManager;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.FluidGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ItemGridEventHandler;
import com.refinedmods.refinedstorage2.platform.common.menu.MenuOpener;
import com.refinedmods.refinedstorage2.platform.common.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.packet.ServerToClientCommunications;
import com.refinedmods.refinedstorage2.platform.common.render.FluidRenderer;
import com.refinedmods.refinedstorage2.platform.common.util.BucketQuantityFormatter;

import java.util.Optional;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public interface Platform {
    Platform INSTANCE = new PlatformProxy();

    ServerToClientCommunications getServerToClientCommunications();

    ClientToServerCommunications getClientToServerCommunications();

    MenuOpener getMenuOpener();

    long getBucketAmount();

    TagKey<Item> getWrenchTag();

    BucketQuantityFormatter getBucketQuantityFormatter();

    Config getConfig();

    boolean canEditBoxLoseFocus(EditBox editBox);

    boolean isKeyDown(KeyMapping keyMapping);

    ItemGridEventHandler createItemGridEventHandler(AbstractContainerMenu containerMenu,
                                                    GridService<ItemResource> gridService,
                                                    Inventory playerInventory);

    FluidGridEventHandler createFluidGridEventHandler(AbstractContainerMenu containerMenu,
                                                      GridService<FluidResource> gridService,
                                                      Inventory playerInventory,
                                                      ExtractableStorage<ItemResource> bucketStorage);

    GridResourceFactory getItemGridResourceFactory();

    GridResourceFactory getFluidGridResourceFactory();

    FluidRenderer getFluidRenderer();

    Optional<FluidResource> convertToFluid(ItemStack stack);

    EnergyStorage createEnergyStorage(ControllerType controllerType, Runnable listener);

    void setEnergy(EnergyStorage energyStorage, long stored);

    TransferManager createTransferManager(AbstractContainerMenu containerMenu);

    long insertIntoContainer(Container container, ItemResource itemResource, long amount, Action action);

    ItemStack getCloneItemStack(BlockState state, Level level, BlockHitResult hitResult, Player player);
}
