package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.support.RecipeMatrixContainer;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;

class ClientCraftingGrid implements CraftingGrid {
    private final RecipeMatrixContainer craftingMatrix;
    private final ResultContainer craftingResult;

    ClientCraftingGrid() {
        this.craftingMatrix = new RecipeMatrixContainer(null, 3, 3);
        this.craftingResult = new ResultContainer();
    }

    @Override
    public RecipeMatrixContainer getCraftingMatrix() {
        return craftingMatrix;
    }

    @Override
    public ResultContainer getCraftingResult() {
        return craftingResult;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final Player player, final CraftingInput input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExtractTransaction startExtractTransaction(final Player player, final boolean directCommit) {
        return ExtractTransaction.NOOP;
    }

    @Override
    public boolean clearMatrix(final Player player, final boolean toPlayerInventory) {
        C2SPackets.sendCraftingGridClear(toPlayerInventory);
        return true;
    }

    @Override
    public void transferRecipe(final Player player, final List<List<ItemResource>> recipe) {
        C2SPackets.sendCraftingGridRecipeTransfer(recipe);
    }

    @Override
    public void acceptQuickCraft(final Player player, final ItemStack craftedStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Storage getItemStorage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGridActive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<PlatformResourceKey> getAutocraftableResources() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GridOperations createOperations(final ResourceType resourceType, final ServerPlayer player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource) {
        return CompletableFuture.completedFuture(0L);
    }

    @Override
    public CompletableFuture<Optional<TaskId>> startTask(final ResourceKey resource,
                                                         final long amount,
                                                         final Actor actor,
                                                         final boolean notify) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException();
    }
}
