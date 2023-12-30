package com.refinedmods.refinedstorage2.platform.fabric.storage.portablegrid;

import com.refinedmods.refinedstorage2.platform.common.storage.Disk;
import com.refinedmods.refinedstorage2.platform.common.storage.portablegrid.PortableGridBlock;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.QuadRotators;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.QuadTranslator;

import java.util.Map;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class PortableGridBakedModel extends ForwardingBakedModel {
    private static final QuadTranslator MOVE_TO_DISK_LOCATION = new QuadTranslator(0, -12 / 16F, 9 / 16F);

    private final BakedModel activeModel;
    private final BakedModel inactiveModel;
    private final Map<Item, BakedModel> diskModels;
    private final QuadRotators quadRotators;

    public PortableGridBakedModel(final BakedModel activeModel,
                                  final BakedModel inactiveModel,
                                  final Map<Item, BakedModel> diskModels,
                                  final QuadRotators quadRotators) {
        this.wrapped = inactiveModel;
        this.activeModel = activeModel;
        this.inactiveModel = inactiveModel;
        this.diskModels = diskModels;
        this.quadRotators = quadRotators;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitItemQuads(final ItemStack stack,
                              final Supplier<RandomSource> randomSupplier,
                              final RenderContext context) {
        inactiveModel.emitItemQuads(stack, randomSupplier, context);
    }

    @Override
    public void emitBlockQuads(final BlockAndTintGetter blockView,
                               final BlockState state,
                               final BlockPos pos,
                               final Supplier<RandomSource> randomSupplier,
                               final RenderContext context) {
        if (!(state.getBlock() instanceof PortableGridBlock portableGridBlock)) {
            return;
        }
        final BiDirection direction = portableGridBlock.getDirection(state);
        if (direction == null) {
            return;
        }
        context.pushTransform(quadRotators.forDirection(direction));
        if (blockView instanceof RenderAttachedBlockView renderAttachedBlockView) {
            final Object renderAttachment = renderAttachedBlockView.getBlockEntityRenderAttachment(pos);
            if (renderAttachment instanceof Disk disk) {
                emitDiskQuads(blockView, state, pos, randomSupplier, context, disk);
            }
        }
        final boolean active = state.getValue(PortableGridBlock.ACTIVE);
        (active ? activeModel : inactiveModel).emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
    }

    private void emitDiskQuads(
        final BlockAndTintGetter blockView,
        final BlockState state,
        final BlockPos pos,
        final Supplier<RandomSource> randomSupplier,
        final RenderContext context,
        final Disk disk
    ) {
        final BakedModel diskModel = diskModels.get(disk.item());
        if (diskModel == null) {
            return;
        }
        context.pushTransform(MOVE_TO_DISK_LOCATION);
        context.pushTransform(quadRotators.forDirection(BiDirection.WEST));
        diskModel.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
        context.popTransform();
    }
}
