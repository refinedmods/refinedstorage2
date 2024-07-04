package com.refinedmods.refinedstorage.platform.fabric.storage.diskdrive;

import com.refinedmods.refinedstorage.platform.common.storage.diskdrive.DiskDriveBlock;
import com.refinedmods.refinedstorage.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.platform.fabric.storage.AbstractDiskContainerBakedModel;
import com.refinedmods.refinedstorage.platform.fabric.support.render.QuadRotators;
import com.refinedmods.refinedstorage.platform.fabric.support.render.QuadTranslator;

import java.util.Map;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

class DiskDriveBakedModel extends AbstractDiskContainerBakedModel {
    private final QuadRotators quadRotators;

    DiskDriveBakedModel(final BakedModel baseModel,
                        final Map<Item, BakedModel> diskModels,
                        final BakedModel inactiveLedModel,
                        final QuadRotators quadRotators) {
        super(diskModels, inactiveLedModel, getDiskTranslations());
        this.wrapped = baseModel;
        this.quadRotators = quadRotators;
    }

    private static QuadTranslator[] getDiskTranslations() {
        final QuadTranslator[] translations = new QuadTranslator[8];
        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                translations[i++] = new QuadTranslator(
                    x == 0 ? -(2F / 16F) : -(9F / 16F),
                    -((y * 3F) / 16F) - (2F / 16F),
                    0
                );
            }
        }
        return translations;
    }

    @Override
    public void emitItemQuads(final ItemStack stack,
                              final Supplier<RandomSource> randomSupplier,
                              final RenderContext context) {
        wrapped.emitItemQuads(stack, randomSupplier, context);
        super.emitItemQuads(stack, randomSupplier, context);
    }

    @Override
    public void emitBlockQuads(final BlockAndTintGetter blockView,
                               final BlockState state,
                               final BlockPos pos,
                               final Supplier<RandomSource> randomSupplier,
                               final RenderContext context) {
        if (!(state.getBlock() instanceof DiskDriveBlock diskDriveBlock)) {
            return;
        }
        final BiDirection direction = diskDriveBlock.getDirection(state);
        if (direction == null) {
            return;
        }
        context.pushTransform(quadRotators.forDirection(direction));
        wrapped.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
    }
}
