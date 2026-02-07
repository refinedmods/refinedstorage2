package com.refinedmods.refinedstorage.fabric.networking;

import com.refinedmods.refinedstorage.common.constructordestructor.AbstractConstructorDestructorBlock;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

class ActiveInactiveCablePart implements CablePart {
    private final Identifier activeModel;
    private final Identifier inactiveModel;

    ActiveInactiveCablePart(final Identifier activeModel, final Identifier inactiveModel) {
        this.activeModel = activeModel;
        this.inactiveModel = inactiveModel;
    }

    @Override
    public ModelBaker.@Nullable SharedOperationKey<BlockStateModelPart> getKey(final BlockState state) {
        if (state.getBlock() instanceof AbstractDirectionalBlock<?> directionalBlock
            && directionalBlock.getDirection(state) instanceof Direction direction) {
            return new OperationKey(direction, isActive(state) ? activeModel : inactiveModel);
        }
        return null;
    }

    private boolean isActive(final BlockState state) {
        if (state.hasProperty(AbstractConstructorDestructorBlock.ACTIVE)) {
            return state.getValue(AbstractConstructorDestructorBlock.ACTIVE);
        }
        return false;
    }

    private record OperationKey(Direction direction, Identifier model)
        implements ModelBaker.SharedOperationKey<BlockStateModelPart> {
        @Override
        public BlockStateModelPart compute(final ModelBaker modelBaker) {
            return SimpleModelWrapper.bake(modelBaker, model, CableBlockStateModel.getRotation(direction));
        }
    }
}
