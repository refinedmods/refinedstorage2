package com.refinedmods.refinedstorage.neoforge.storage.portablegrid;

import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridBlock;
import com.refinedmods.refinedstorage.common.support.ComposedModelState;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.ArrayList;
import java.util.List;

import com.mojang.math.OctahedralGroup;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

class PortableGridBlockStateModel implements BlockStateModel {
    private static final Disk NO_DISK = new Disk(null, StorageState.NONE);
    private static final Vector3f MOVE_TO_DISK_LOCATION = new Vector3f(0, -12 / 16F, 9 / 16F);

    private final Material.Baked particleMaterial;
    private final ModelBaker modelBaker;

    PortableGridBlockStateModel(final Material.Baked particleMaterial, final ModelBaker modelBaker) {
        this.particleMaterial = particleMaterial;
        this.modelBaker = modelBaker;
    }

    @Override
    public void collectParts(final BlockAndTintGetter level, final BlockPos pos, final BlockState state,
                             final RandomSource random, final List<BlockStateModelPart> parts) {
        final EnumProperty<OrientedDirection> property = OrientedDirectionType.INSTANCE.getProperty();
        if (!state.hasProperty(property)) {
            return;
        }
        final OrientedDirection direction = state.getValue(property);
        final boolean active = state.hasProperty(PortableGridBlock.ACTIVE)
            && state.getValue(PortableGridBlock.ACTIVE);
        final Disk disk = level.getModelData(pos).get(ForgePortableGridBlockEntity.DISK_PROPERTY);
        if (disk == null) {
            parts.addAll(modelBaker.compute(new PortableGridOperationKey(direction, active, NO_DISK)));
            return;
        }
        parts.addAll(modelBaker.compute(new PortableGridOperationKey(direction, active, disk)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void collectParts(final RandomSource randomSource, final List<BlockStateModelPart> list) {
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

    private record PortableGridOperationKey(OrientedDirection direction, boolean active, Disk disk)
        implements ModelBaker.SharedOperationKey<List<BlockStateModelPart>> {
        @Override
        public List<BlockStateModelPart> compute(final ModelBaker modelBaker) {
            final List<BlockStateModelPart> parts = new ArrayList<>();
            final Identifier model = active
                ? PortableGridRenderingProperties.ACTIVE_MODEL
                : PortableGridRenderingProperties.INACTIVE_MODEL;
            parts.add(SimpleModelWrapper.bake(modelBaker, model, ClientPlatformUtil.getRotation(direction)));
            final BlockStateModelPart diskPart = getDisk(modelBaker);
            if (diskPart != null) {
                parts.add(diskPart);
            }
            return parts;
        }

        @Nullable
        private BlockStateModelPart getDisk(final ModelBaker modelBaker) {
            if (disk.state() == StorageState.NONE) {
                return null;
            }
            final Identifier model = RefinedStorageClientApi.INSTANCE.getDiskModelsByItem().get(disk.item());
            if (model == null) {
                return null;
            }
            final ModelState baseState = new ComposedModelState(
                ClientPlatformUtil.getRotation(direction),
                new Transformation(MOVE_TO_DISK_LOCATION, null, null, null)
            );
            return SimpleModelWrapper.bake(modelBaker, model, rotateToDiskSide(baseState));
        }

        private static ModelState rotateToDiskSide(final ModelState baseState) {
            return new ComposedModelState(
                baseState,
                BlockModelRotation.get(OctahedralGroup.ROT_90_Y_POS).transformation()
            );
        }
    }
}
