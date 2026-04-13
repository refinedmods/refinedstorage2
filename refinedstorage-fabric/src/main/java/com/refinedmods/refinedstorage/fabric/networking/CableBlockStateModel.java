package com.refinedmods.refinedstorage.fabric.networking;

import com.refinedmods.refinedstorage.common.networking.CableConnections;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.math.OctahedralGroup;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
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
    public void emitQuads(final QuadEmitter emitter, final BlockAndTintGetter level, final BlockPos pos,
                          final BlockState state,
                          final RandomSource random, final Predicate<@Nullable Direction> cullTest) {
        if (!(level.getBlockEntityRenderData(pos) instanceof CableConnections connections)) {
            collectCorePart(emitter, cullTest);
            return;
        }
        modelBaker.compute(new SharedOperationKey(color, connections))
            .forEach(p -> p.emitQuads(emitter, cullTest));
        if (part != null) {
            final ModelBaker.SharedOperationKey<BlockStateModelPart> partKey = part.getKey(state);
            if (partKey != null) {
                modelBaker.compute(partKey).emitQuads(emitter, cullTest);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void collectParts(final RandomSource randomSource, final List<BlockStateModelPart> parts) {
        // no op
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

    private void collectCorePart(final QuadEmitter emitter, final Predicate<@Nullable Direction> cullTest) {
        modelBaker.compute(new CoreSharedOperationKey(color)).emitQuads(emitter, cullTest);
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
