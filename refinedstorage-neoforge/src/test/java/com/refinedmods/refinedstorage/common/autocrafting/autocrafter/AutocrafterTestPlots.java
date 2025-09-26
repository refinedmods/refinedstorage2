package com.refinedmods.refinedstorage.common.autocrafting.autocrafter;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.autocrafting.CraftingPatternState;
import com.refinedmods.refinedstorage.common.autocrafting.ProcessingPatternState;
import com.refinedmods.refinedstorage.common.autocrafting.SmithingTablePatternState;
import com.refinedmods.refinedstorage.common.autocrafting.StonecutterPatternState;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternType;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.RSBLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.createCraftingMatrix;
import static com.refinedmods.refinedstorage.common.GameTestUtil.requireBlockEntity;
import static net.minecraft.core.BlockPos.ZERO;
import static net.minecraft.world.item.Items.COAL;

final class AutocrafterTestPlots {
    private AutocrafterTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final boolean furnace,
                            final TriConsumer<AutocrafterBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), RSBLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), RSBLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        helper.setBlock(
            ZERO.above().above().south(),
            RSBLOCKS.getFluidStorageBlock(FluidStorageVariant.SIXTY_FOUR_B)
        );

        final BlockPos furnacePos = ZERO.above().east();
        if (furnace) {
            helper.setBlock(furnacePos, Blocks.BLAST_FURNACE);
            final AbstractFurnaceBlockEntity furnaceBlockEntity =
                requireBlockEntity(helper, furnacePos, AbstractFurnaceBlockEntity.class);
            furnaceBlockEntity.setItem(1, new ItemStack(COAL, 64));

            helper.setBlock(furnacePos.below(), RSBLOCKS.getImporter().getDefault().rotated(Direction.UP));
            helper.setBlock(furnacePos.below().west(), RSBLOCKS.getCable().getDefault());
        }

        final BlockPos autocrafterPos = furnacePos.above();
        helper.setBlock(autocrafterPos, RSBLOCKS.getAutocrafter().getDefault().rotated(Direction.DOWN));
        consumer.accept(
            requireBlockEntity(helper, autocrafterPos, AutocrafterBlockEntity.class),
            autocrafterPos,
            helper.startSequence()
        );
    }

    static ItemStack createCraftingPattern(final List<Item> items,
                                           final List<Integer> itemIndices) {
        final ItemStack pattern = PatternGridBlockEntity.createPatternStack(PatternType.CRAFTING);
        pattern.set(DataComponents.INSTANCE.getCraftingPatternState(),
            new CraftingPatternState(false, createCraftingMatrix(items, itemIndices)));
        return pattern;
    }

    static ItemStack createProcessingPattern(final List<ProcessingPatternState.ProcessingIngredient> ingredients,
                                             final List<ResourceAmount> outputs) {
        final List<Optional<ProcessingPatternState.ProcessingIngredient>> optionalIngredients = ingredients.stream()
            .map(Optional::ofNullable)
            .toList();
        final List<Optional<ResourceAmount>> optionalOutputs = outputs.stream()
            .map(Optional::ofNullable)
            .toList();
        final ItemStack pattern = PatternGridBlockEntity.createPatternStack(PatternType.PROCESSING);
        pattern.set(
            DataComponents.INSTANCE.getProcessingPatternState(),
            new ProcessingPatternState(optionalIngredients, optionalOutputs)
        );
        return pattern;
    }

    static ItemStack createStoneCutterPattern(final ItemResource input, final ItemResource selectedOutput) {
        final ItemStack pattern = PatternGridBlockEntity.createPatternStack(PatternType.STONECUTTER);
        pattern.set(DataComponents.INSTANCE.getStonecutterPatternState(),
            new StonecutterPatternState(input, selectedOutput));
        return pattern;
    }

    static ItemStack createSmithingTablePattern(final ItemResource template,
                                                final ItemResource base,
                                                final ItemResource addition) {
        final ItemStack pattern = PatternGridBlockEntity.createPatternStack(PatternType.SMITHING_TABLE);
        pattern.set(DataComponents.INSTANCE.getSmithingTablePatternState(),
            new SmithingTablePatternState(template, base, addition));
        return pattern;
    }
}
