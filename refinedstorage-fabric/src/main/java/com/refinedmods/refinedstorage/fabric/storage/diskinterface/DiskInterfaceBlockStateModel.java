package com.refinedmods.refinedstorage.fabric.storage.diskinterface;

import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.storage.Disk;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.ComposedModelState;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

class DiskInterfaceBlockStateModel implements BlockStateModel {
    private static final Disk[] NO_DISKS = new Disk[0];

    private final Material.Baked particleMaterial;
    private final ModelBaker modelBaker;
    private final DyeColor color;

    DiskInterfaceBlockStateModel(final Material.Baked particleMaterial,
                                 final ModelBaker modelBaker,
                                 final DyeColor color) {
        this.particleMaterial = particleMaterial;
        this.modelBaker = modelBaker;
        this.color = color;
    }

    @Override
    public void emitQuads(final QuadEmitter emitter, final BlockAndTintGetter level, final BlockPos pos,
                          final BlockState state,
                          final RandomSource random, final Predicate<@Nullable Direction> cullTest) {
        final boolean active = state.hasProperty(AbstractActiveColoredDirectionalBlock.ACTIVE)
            && state.getValue(AbstractActiveColoredDirectionalBlock.ACTIVE);
        final EnumProperty<OrientedDirection> property = OrientedDirectionType.INSTANCE.getProperty();
        if (!state.hasProperty(property)) {
            return;
        }
        final OrientedDirection direction = state.getValue(property);
        if (!(level.getBlockEntityRenderData(pos) instanceof Disk[] disks)) {
            modelBaker.compute(new DiskInterfaceOperationKey(active, color, direction, NO_DISKS))
                .forEach(part -> part.emitQuads(emitter, cullTest));
            return;
        }
        modelBaker.compute(new DiskInterfaceOperationKey(active, color, direction, disks))
            .forEach(part -> part.emitQuads(emitter, cullTest));
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

    private record DiskInterfaceOperationKey(boolean active, DyeColor color, OrientedDirection direction, Disk[] disks)
        implements ModelBaker.SharedOperationKey<List<BlockStateModelPart>> {
        @Override
        public List<BlockStateModelPart> compute(final ModelBaker modelBaker) {
            final List<BlockStateModelPart> parts = new ArrayList<>();
            final Identifier baseModel = active
                ? DiskInterfaceRenderingProperties.getActiveBaseModel(color)
                : DiskInterfaceRenderingProperties.INACTIVE_BASE_MODEL;
            parts.add(SimpleModelWrapper.bake(modelBaker, baseModel, ClientPlatformUtil.getRotation(direction)));
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
                new Transformation(DiskInterfaceRenderingProperties.TRANSLATIONS[i], null, null, null)
            );
            return SimpleModelWrapper.bake(modelBaker, model, modelState);
        }

        @Override
        public boolean equals(@Nullable final Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final DiskInterfaceOperationKey that = (DiskInterfaceOperationKey) o;
            return active == that.active
                && Objects.deepEquals(disks, that.disks)
                && color == that.color
                && direction == that.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(active, color, direction, Arrays.hashCode(disks));
        }

        @Override
        public String toString() {
            return "DiskInterfaceOperationKey{"
                + "active=" + active
                + ", color=" + color
                + ", direction=" + direction
                + ", disks=" + Arrays.toString(disks)
                + '}';
        }
    }
}
