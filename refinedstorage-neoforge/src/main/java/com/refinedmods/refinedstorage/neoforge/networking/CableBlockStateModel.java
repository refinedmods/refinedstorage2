package com.refinedmods.refinedstorage.neoforge.networking;

import com.refinedmods.refinedstorage.common.networking.CableConnections;
import com.refinedmods.refinedstorage.neoforge.support.render.ModelProperties;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.OctahedralGroup;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

class CableBlockStateModel implements BlockStateModel {
    private final Material.Baked particleMaterial;
    private final ModelBaker modelBaker;
    private final DyeColor color;
    @Nullable
    private final CablePart part;

    CableBlockStateModel(final Material.Baked particleMaterial, final ModelBaker modelBaker,
                         final DyeColor color, @Nullable final CablePart part) {
        this.particleMaterial = particleMaterial;
        this.modelBaker = modelBaker;
        this.color = color;
        this.part = part;
    }

    @Override
    public void collectParts(final BlockAndTintGetter level, final BlockPos pos, final BlockState state,
                             final RandomSource random, final List<BlockStateModelPart> parts) {
        final ModelData data = level.getModelData(pos);
        final CableConnections connections = data.get(ModelProperties.CABLE_CONNECTIONS);
        if (connections == null) {
            collectCorePart(parts);
            return;
        }
        parts.addAll(modelBaker.compute(new SharedOperationKey(color, connections)));
        if (part != null) {
            final ModelBaker.SharedOperationKey<BlockStateModelPart> partKey = part.getKey(state);
            if (partKey != null) {
                parts.add(modelBaker.compute(partKey));
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void collectParts(final RandomSource randomSource, final List<BlockStateModelPart> parts) {
        collectCorePart(parts);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Material.Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @BakedQuad.MaterialFlags int materialFlags() {
        return 0;
    }

    private void collectCorePart(final List<BlockStateModelPart> parts) {
        parts.add(modelBaker.compute(new CoreSharedOperationKey(color)));
    }

    public static BlockModelRotation getRotation(final Direction direction) {
        return switch (direction) {
            case NORTH -> BlockModelRotation.get(OctahedralGroup.IDENTITY);
            case SOUTH -> BlockModelRotation.get(OctahedralGroup.ROT_180_FACE_XZ);
            case EAST -> BlockModelRotation.get(OctahedralGroup.ROT_90_Y_NEG);
            case WEST -> BlockModelRotation.get(OctahedralGroup.ROT_90_Y_POS);
            case UP -> BlockModelRotation.get(OctahedralGroup.ROT_90_X_POS);
            case DOWN -> BlockModelRotation.get(OctahedralGroup.ROT_90_X_NEG);
        };
    }

    private record SharedOperationKey(DyeColor color, CableConnections connections)
        implements ModelBaker.SharedOperationKey<List<BlockStateModelPart>> {
        @Override
        public List<BlockStateModelPart> compute(final ModelBaker modelBaker) {
            final List<BlockStateModelPart> parts = new ArrayList<>();
            parts.add(modelBaker.compute(new CoreSharedOperationKey(color)));
            final Identifier extensionModel = createIdentifier("block/cable/extension/" + color.getName());
            for (final Direction direction : Direction.values()) {
                if (connections.isConnected(direction)) {
                    parts.add(SimpleModelWrapper.bake(modelBaker, extensionModel, getRotation(direction)));
                }
            }
            return parts;
        }
    }

    private record CoreSharedOperationKey(DyeColor color)
        implements ModelBaker.SharedOperationKey<BlockStateModelPart> {
        @Override
        public BlockStateModelPart compute(final ModelBaker modelBaker) {
            return SimpleModelWrapper.bake(modelBaker, createIdentifier("block/cable/core/" + color.getName()),
                BlockModelRotation.IDENTITY);
        }
    }
}
