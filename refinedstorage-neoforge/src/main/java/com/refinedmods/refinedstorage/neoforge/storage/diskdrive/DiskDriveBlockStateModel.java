package com.refinedmods.refinedstorage.neoforge.storage.diskdrive;

import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.support.ComposedModelState;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
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
import org.jspecify.annotations.Nullable;

class DiskDriveBlockStateModel implements BlockStateModel {
    private static final Disk[] NO_DISKS = new Disk[0];

    private final Material.Baked particleMaterial;
    private final ModelBaker modelBaker;

    DiskDriveBlockStateModel(final Material.Baked particleMaterial, final ModelBaker modelBaker) {
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
        final var disks = level.getModelData(pos).get(ForgeDiskDriveBlockEntity.DISKS_PROPERTY);
        if (disks == null) {
            parts.addAll(modelBaker.compute(new DiskDriveOperationKey(direction, NO_DISKS)));
            return;
        }
        parts.addAll(modelBaker.compute(new DiskDriveOperationKey(direction, disks)));
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

    private record DiskDriveOperationKey(OrientedDirection direction, Disk[] disks)
        implements ModelBaker.SharedOperationKey<List<BlockStateModelPart>> {
        @Override
        public List<BlockStateModelPart> compute(final ModelBaker modelBaker) {
            final List<BlockStateModelPart> parts = new ArrayList<>();
            parts.add(SimpleModelWrapper.bake(modelBaker, DiskDriveRenderingProperties.BASE_MODEL,
                ClientPlatformUtil.getRotation(direction)));
            for (int i = 0; i < disks.length; ++i) {
                final BlockStateModelPart diskPart = getDisk(modelBaker, i);
                if (diskPart != null) {
                    parts.add(diskPart);
                }
            }
            return parts;
        }

        @Nullable
        private BlockStateModelPart getDisk(final ModelBaker modelBaker, final int i) {
            if (disks[i].state() == StorageState.NONE) {
                return null;
            }
            final Identifier model = RefinedStorageClientApi.INSTANCE.getDiskModelsByItem().get(disks[i].item());
            if (model == null) {
                return null;
            }
            final ModelState modelState = new ComposedModelState(
                ClientPlatformUtil.getRotation(direction),
                new Transformation(DiskDriveRenderingProperties.TRANSLATIONS[i], null, null, null)
            );
            return SimpleModelWrapper.bake(modelBaker, model, modelState);
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final DiskDriveOperationKey that = (DiskDriveOperationKey) o;
            if (direction != that.direction) {
                return false;
            }
            return Arrays.equals(disks, that.disks);
        }

        @Override
        public int hashCode() {
            int result = direction.hashCode();
            result = 31 * result + Arrays.hashCode(disks);
            return result;
        }

        @Override
        public String toString() {
            return "DiskDriveOperationKey{"
                + "direction=" + direction
                + ", disks=" + Arrays.toString(disks)
                + '}';
        }
    }
}
