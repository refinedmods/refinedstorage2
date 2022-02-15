package com.refinedmods.refinedstorage2.platform.forge.render.model.baked;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiskDriveItemBakedModel extends ForwardingBakedModel {
    private final BakedModel diskDisconnectedModel;
    private final Vector3f[] translators;
    private final boolean[] disks;

    public DiskDriveItemBakedModel(BakedModel baseModel, BakedModel diskDisconnectedModel, Vector3f[] translators, boolean[] disks) {
        super(baseModel);
        this.diskDisconnectedModel = diskDisconnectedModel;
        this.translators = translators;
        this.disks = disks;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand) {
        List<BakedQuad> quads = new ArrayList<>(baseModel.getQuads(state, side, rand));
        for (int i = 0; i < translators.length; ++i) {
            if (disks[i]) {
                quads.addAll(getDiskModel(side, rand, translators[i]));
            }
        }
        return quads;
    }

    private List<BakedQuad> getDiskModel(Direction side, Random rand, Vector3f translation) {
        List<BakedQuad> diskQuads = diskDisconnectedModel.getQuads(null, side, rand);
        return QuadTransformer.translate(diskQuads, translation);
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
        return ForgeHooksClient.handlePerspective(this, cameraTransformType, poseStack);
    }
}
