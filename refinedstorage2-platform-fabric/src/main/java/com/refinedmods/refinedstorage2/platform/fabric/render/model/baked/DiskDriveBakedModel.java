package com.refinedmods.refinedstorage2.platform.fabric.render.model.baked;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.platform.common.block.BaseBlock;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.baked.transform.QuadRotator;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.baked.transform.QuadTranslator;

import java.util.Random;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class DiskDriveBakedModel extends ForwardingBakedModel {
    private final BakedModel diskModel;
    private final QuadTranslator[] translators = new QuadTranslator[8];

    public DiskDriveBakedModel(BakedModel baseModel, BakedModel diskModel) {
        this.wrapped = baseModel;
        this.diskModel = diskModel;

        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                translators[i++] = new QuadTranslator(x == 0 ? -(2F / 16F) : -(9F / 16F), -((y * 3F) / 16F) - (2F / 16F), 0);
            }
        }
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        QuadRotator rotator = new QuadRotator(state.getValue(BaseBlock.DIRECTION));

        context.pushTransform(rotator);

        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        if (blockView instanceof RenderAttachedBlockView renderAttachedBlockView) {
            Object renderAttachment = renderAttachedBlockView.getBlockEntityRenderAttachment(pos);
            if (renderAttachment instanceof DiskDriveState states) {
                for (int i = 0; i < translators.length; ++i) {
                    if (states.getState(i) != StorageDiskState.NONE) {
                        context.pushTransform(translators[i]);
                        context.fallbackConsumer().accept(diskModel);
                        context.popTransform();
                    }
                }
            }
        }

        context.popTransform();
    }
}
