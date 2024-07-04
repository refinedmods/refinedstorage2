package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.util.ContainerUtil;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CraftingGridBlockEntity extends AbstractGridBlockEntity implements BlockEntityWithDrops {
    private static final String TAG_CRAFTING_MATRIX = "matrix";

    @Nullable
    private CraftingRecipe currentRecipe;

    private final CraftingMatrix craftingMatrix = new CraftingMatrix(this::setOutput);
    private final ResultContainer craftingResult = new ResultContainer();

    public CraftingGridBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getCraftingGrid(),
            pos,
            state,
            Platform.INSTANCE.getConfig().getCraftingGrid().getEnergyUsage()
        );
    }

    private void setOutput() {
        if (level == null) {
            return;
        }
        setOutputSilently(level);
        setChanged();
    }

    private void setOutputSilently(final Level level) {
        if (level.isClientSide()) {
            return;
        }
        final CraftingInput input = craftingMatrix.asCraftInput();
        if (currentRecipe == null || !currentRecipe.matches(input, level)) {
            currentRecipe = loadRecipe(level);
        }
        if (currentRecipe == null) {
            setResult(ItemStack.EMPTY);
        } else {
            setResult(currentRecipe.assemble(input, level.registryAccess()));
        }
    }

    private void setResult(final ItemStack result) {
        craftingResult.setItem(0, result);
    }

    @Nullable
    private CraftingRecipe loadRecipe(final Level level) {
        return level
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftingMatrix.asCraftInput(), level)
            .map(RecipeHolder::value)
            .orElse(null);
    }

    CraftingMatrix getCraftingMatrix() {
        return craftingMatrix;
    }

    ResultContainer getCraftingResult() {
        return craftingResult;
    }

    NonNullList<ItemStack> getRemainingItems(final Player player, final CraftingInput input) {
        if (level == null || currentRecipe == null) {
            return NonNullList.create();
        }
        return Platform.INSTANCE.getRemainingCraftingItems(player, currentRecipe, input);
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.CRAFTING_GRID;
    }

    @Override
    @Nullable
    public AbstractGridContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new CraftingGridContainerMenu(syncId, inventory, this);
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_CRAFTING_MATRIX, ContainerUtil.write(craftingMatrix, provider));
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(TAG_CRAFTING_MATRIX)) {
            ContainerUtil.read(tag.getCompound(TAG_CRAFTING_MATRIX), craftingMatrix, provider);
        }
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        setOutputSilently(level);
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < craftingMatrix.getContainerSize(); ++i) {
            drops.add(craftingMatrix.getItem(i));
        }
        return drops;
    }

    Optional<Network> getNetwork() {
        if (!mainNode.isActive()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mainNode.getNetwork());
    }

    Optional<StorageChannel> getStorageChannel() {
        return getNetwork().map(network -> network.getComponent(StorageNetworkComponent.class));
    }

    ItemStack insert(final ItemStack stack, final Player player) {
        return getStorageChannel().map(storageChannel -> doInsert(stack, player, storageChannel)).orElse(stack);
    }

    private ItemStack doInsert(final ItemStack stack,
                               final Player player,
                               final StorageChannel storageChannel) {
        final long inserted = storageChannel.insert(
            ItemResource.ofItemStack(stack),
            stack.getCount(),
            Action.EXECUTE,
            new PlayerActor(player)
        );
        final long remainder = stack.getCount() - inserted;
        if (remainder == 0) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount((int) remainder);
    }

    long extract(final ItemResource resource, final Player player) {
        return getStorageChannel().map(storageChannel -> storageChannel.extract(
            resource,
            1,
            Action.EXECUTE,
            new PlayerActor(player)
        )).orElse(0L);
    }
}
