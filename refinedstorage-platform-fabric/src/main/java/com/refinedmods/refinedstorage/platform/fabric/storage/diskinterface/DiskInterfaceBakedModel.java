package com.refinedmods.refinedstorage.platform.fabric.storage.diskinterface;

import com.refinedmods.refinedstorage.platform.common.storage.diskinterface.DiskInterfaceBlock;
import com.refinedmods.refinedstorage.platform.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.platform.fabric.storage.AbstractDiskContainerBakedModel;
import com.refinedmods.refinedstorage.platform.fabric.support.render.EmissiveTransform;
import com.refinedmods.refinedstorage.platform.fabric.support.render.QuadRotators;
import com.refinedmods.refinedstorage.platform.fabric.support.render.QuadTranslator;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

class DiskInterfaceBakedModel extends AbstractDiskContainerBakedModel {
    private final BakedModel inactiveModel;
    private final QuadRotators quadRotators;
    private final EmissiveTransform emissiveTransform;

    DiskInterfaceBakedModel(final BakedModel baseModel,
                            final BakedModel inactiveModel,
                            final Map<Item, BakedModel> diskModels,
                            final BakedModel inactiveLedModel,
                            final QuadRotators quadRotators,
                            final ResourceLocation emissiveSprite) {
        super(diskModels, inactiveLedModel, getDiskTranslations());
        this.wrapped = baseModel;
        this.inactiveModel = inactiveModel;
        this.quadRotators = quadRotators;
        this.emissiveTransform = new EmissiveTransform(Set.of(emissiveSprite));
    }

    private static QuadTranslator[] getDiskTranslations() {
        final QuadTranslator[] translations = new QuadTranslator[6];
        for (int i = 0; i < translations.length; ++i) {
            final int x = i < 3 ? 0 : 1;
            final int y = i % 3;
            translations[i] = new QuadTranslator(
                x == 0 ? -(2F / 16F) : -(9F / 16F),
                -((y * 3F) / 16F) - (6F / 16F),
                0
            );
        }
        return translations;
    }

    @Override
    public void emitItemQuads(final ItemStack stack,
                              final Supplier<RandomSource> randomSupplier,
                              final RenderContext context) {
        context.pushTransform(emissiveTransform);
        wrapped.emitItemQuads(stack, randomSupplier, context);
        context.popTransform();
        super.emitItemQuads(stack, randomSupplier, context);
    }

    @Override
    public void emitBlockQuads(final BlockAndTintGetter blockView,
                               final BlockState state,
                               final BlockPos pos,
                               final Supplier<RandomSource> randomSupplier,
                               final RenderContext context) {
        if (!(state.getBlock() instanceof DiskInterfaceBlock diskInterfaceBlock)) {
            return;
        }
        final BiDirection direction = diskInterfaceBlock.getDirection(state);
        if (direction == null) {
            return;
        }
        context.pushTransform(quadRotators.forDirection(direction));
        final boolean active = state.getValue(AbstractActiveColoredDirectionalBlock.ACTIVE);
        if (active) {
            context.pushTransform(emissiveTransform);
        }
        (active ? wrapped : inactiveModel).emitBlockQuads(blockView, state, pos, randomSupplier, context);
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        if (active) {
            context.popTransform();
        }
        context.popTransform();
    }
}
