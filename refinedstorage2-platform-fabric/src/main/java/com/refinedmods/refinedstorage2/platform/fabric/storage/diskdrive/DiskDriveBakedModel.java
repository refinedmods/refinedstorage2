package com.refinedmods.refinedstorage2.platform.fabric.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.network.impl.node.multistorage.MultiStorageStorageState;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.DiskDriveDisk;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.QuadRotator;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.QuadTranslator;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

class DiskDriveBakedModel extends ForwardingBakedModel {
    private static final QuadTranslator[] TRANSLATORS = new QuadTranslator[8];
    private static final Map<BiDirection, QuadRotator> ROTATORS = new EnumMap<>(BiDirection.class);

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

        for (final BiDirection direction : BiDirection.values()) {
            ROTATORS.put(direction, new QuadRotator(direction));
        }
    }

    private final BakedModel diskModel;
    private final BakedModel inactiveLedModel;

    DiskDriveBakedModel(final BakedModel baseModel,
                        final BakedModel diskModel,
                        final BakedModel inactiveLedModel) {
        this.wrapped = baseModel;
        this.diskModel = diskModel;
        this.inactiveLedModel = inactiveLedModel;
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
            if (!AbstractDiskDriveBlockEntity.hasDisk(tag, i)) {
                continue;
            }
            context.pushTransform(TRANSLATORS[i]);
            diskModel.emitItemQuads(stack, randomSupplier, context);
            inactiveLedModel.emitItemQuads(stack, randomSupplier, context);
            context.popTransform();
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

        final QuadRotator rotator = ROTATORS.get(direction);
        context.pushTransform(rotator);

        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        if (blockView instanceof RenderAttachedBlockView renderAttachedBlockView) {
            final Object renderAttachment = renderAttachedBlockView.getBlockEntityRenderAttachment(pos);
            if (renderAttachment instanceof DiskDriveDisk[] disks) {
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
                               final DiskDriveDisk[] disks) {
        for (int i = 0; i < TRANSLATORS.length; ++i) {
            if (disks[i].state() == MultiStorageStorageState.NONE) {
                continue;
            }
            context.pushTransform(TRANSLATORS[i]);
            diskModel.emitBlockQuads(blockView, state, pos, randomSupplier, context);
            context.popTransform();
        }
    }
}
