package com.refinedmods.refinedstorage2.platform.common.block.entity.grid;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.util.ContainerUtil;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class CraftingGridBlockEntity extends AbstractGridBlockEntity {
    private static final String TAG_CRAFTING_MATRIX = "matrix";

    @Nullable
    private CraftingRecipe currentRecipe;

    private final CraftingMatrix craftingMatrix = new CraftingMatrix(this::setOutput);
    private final ResultContainer craftingResult = new ResultContainer();

    public CraftingGridBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getCraftingGrid(), pos, state);
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
        if (currentRecipe == null || !currentRecipe.matches(craftingMatrix, level)) {
            currentRecipe = loadRecipe(level);
        }
        if (currentRecipe == null) {
            setResult(ItemStack.EMPTY);
        } else {
            setResult(currentRecipe.assemble(craftingMatrix));
        }
    }

    private void setResult(final ItemStack result) {
        craftingResult.setItem(0, result);
    }

    @Nullable
    private CraftingRecipe loadRecipe(final Level level) {
        return level
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftingMatrix, level)
            .orElse(null);
    }

    public CraftingMatrix getCraftingMatrix() {
        return craftingMatrix;
    }

    public ResultContainer getCraftingResult() {
        return craftingResult;
    }

    public NonNullList<ItemStack> getRemainingItems(final Player player) {
        if (level == null || currentRecipe == null) {
            return NonNullList.create();
        }
        return Platform.INSTANCE.getRemainingCraftingItems(player, currentRecipe, craftingMatrix);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "crafting_grid");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new CraftingGridContainerMenu(syncId, inventory, this);
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_CRAFTING_MATRIX, ContainerUtil.write(craftingMatrix));
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_CRAFTING_MATRIX)) {
            ContainerUtil.read(tag.getCompound(TAG_CRAFTING_MATRIX), craftingMatrix);
        }
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        setOutputSilently(level);
    }
}
