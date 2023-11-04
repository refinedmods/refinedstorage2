package com.refinedmods.refinedstorage2.platform.fabric.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.network.impl.node.StorageState;
import com.refinedmods.refinedstorage2.platform.common.storage.Disk;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.QuadRotators;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.QuadTranslator;

import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

class DiskDriveBakedModel extends ForwardingBakedModel {
    private static final QuadTranslator[] TRANSLATORS = new QuadTranslator[8];

    static {
        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                TRANSLATORS[i++] = new QuadTranslator(
                    x == 0 ? -(2F / 16F) : -(9F / 16F),
                    -((y * 3F) / 16F) - (2F / 16F),
                    0
                );
            }
        }
    }

    private final Map<Item, BakedModel> diskModels;
    private final BakedModel inactiveLedModel;
    private final QuadRotators quadRotators;

    DiskDriveBakedModel(final BakedModel baseModel,
                        final Map<Item, BakedModel> diskModels,
                        final BakedModel inactiveLedModel,
                        final QuadRotators quadRotators) {
        this.wrapped = baseModel;
        this.diskModels = diskModels;
        this.inactiveLedModel = inactiveLedModel;
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
        wrapped.emitItemQuads(stack, randomSupplier, context);
        final CompoundTag tag = BlockItem.getBlockEntityData(stack);
        if (tag == null) {
            return;
        }
        for (int i = 0; i < TRANSLATORS.length; ++i) {
            final Item diskItem = AbstractDiskDriveBlockEntity.getDisk(tag, i);
            emitDiskQuads(stack, randomSupplier, context, diskItem, i);
        }
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
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        if (blockView instanceof RenderAttachedBlockView renderAttachedBlockView) {
            final Object renderAttachment = renderAttachedBlockView.getBlockEntityRenderAttachment(pos);
            if (renderAttachment instanceof Disk[] disks) {
                emitDiskQuads(blockView, state, pos, randomSupplier, context, disks);
            }
        }
        context.popTransform();
    }

    private void emitDiskQuads(final BlockAndTintGetter blockView,
                               final BlockState state,
                               final BlockPos pos,
                               final Supplier<RandomSource> randomSupplier,
                               final RenderContext context,
                               final Disk[] disks) {
        for (int i = 0; i < TRANSLATORS.length; ++i) {
            final Disk disk = disks[i];
            emitDiskQuads(blockView, state, pos, randomSupplier, context, disk, i);
        }
    }

    private void emitDiskQuads(final BlockAndTintGetter blockView,
                               final BlockState state,
                               final BlockPos pos,
                               final Supplier<RandomSource> randomSupplier,
                               final RenderContext context,
                               final Disk disk,
                               final int index) {
        if (disk.state() == StorageState.NONE) {
            return;
        }
        final BakedModel model = diskModels.get(disk.item());
        if (model == null) {
            return;
        }
        context.pushTransform(TRANSLATORS[index]);
        model.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
    }


    private void emitDiskQuads(final ItemStack stack,
                               final Supplier<RandomSource> randomSupplier,
                               final RenderContext context,
                               @Nullable final Item diskItem,
                               final int index) {
        if (diskItem == null) {
            return;
        }
        final BakedModel diskModel = diskModels.get(diskItem);
        if (diskModel == null) {
            return;
        }
        context.pushTransform(TRANSLATORS[index]);
        diskModel.emitItemQuads(stack, randomSupplier, context);
        inactiveLedModel.emitItemQuads(stack, randomSupplier, context);
        context.popTransform();
    }
}
