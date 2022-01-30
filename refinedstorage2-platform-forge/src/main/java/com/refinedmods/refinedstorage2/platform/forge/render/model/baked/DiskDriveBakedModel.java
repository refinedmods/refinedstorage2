package com.refinedmods.refinedstorage2.platform.forge.render.model.baked;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.platform.common.block.BaseBlock;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;
import com.refinedmods.refinedstorage2.platform.forge.block.entity.ForgeDiskDriveBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBakedModel extends ForwardingBakedModel {
    private final BakedModel diskModel;
    private final Vector3f[] translators = new Vector3f[8];

    public DiskDriveBakedModel(BakedModel baseModel, BakedModel diskModel) {
        super(baseModel);
        this.diskModel = diskModel;

        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                translators[i++] = new Vector3f(x == 0 ? -(2F / 16F) : -(9F / 16F), -((y * 3F) / 16F) - (2F / 16F), 0);
            }
        }
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData extraData) {
        if (state == null || !state.hasProperty(BaseBlock.DIRECTION)) {
            return super.getQuads(state, side, rand);
        }
        BiDirection direction = state.getValue(BaseBlock.DIRECTION);
        return QuadTransformer.transformSideAndRotate((resultingSide) -> getQuads(state, rand, extraData, resultingSide), direction, side);
    }

    @NotNull
    private List<BakedQuad> getQuads(@NotNull BlockState state, @NotNull Random rand, @NotNull IModelData extraData, Direction side) {
        List<BakedQuad> quads = new ArrayList<>(baseModel.getQuads(state, side, rand));
        DiskDriveState driveState = extraData.getData(ForgeDiskDriveBlockEntity.STATE_PROPERTY);
        if (driveState == null) {
            return quads;
        }
        for (int i = 0; i < translators.length; ++i) {
            StorageDiskState diskState = driveState.getState(i);
            if (diskState != StorageDiskState.NONE) {
                quads.addAll(getDiskModel(state, rand, side, translators[i]));
            }
        }
        return quads;
    }

    private List<BakedQuad> getDiskModel(@NotNull BlockState state, @NotNull Random rand, Direction side, Vector3f translation) {
        List<BakedQuad> diskQuads = diskModel.getQuads(state, side, rand);
        return QuadTransformer.translate(diskQuads, translation);
    }
}
