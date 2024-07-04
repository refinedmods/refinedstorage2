package com.refinedmods.refinedstorage.platform.neoforge.storage.diskdrive;

import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.platform.common.storage.Disk;
import com.refinedmods.refinedstorage.platform.common.storage.diskdrive.DiskDriveBlock;
import com.refinedmods.refinedstorage.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.platform.neoforge.storage.DiskContainerItemOverrides;
import com.refinedmods.refinedstorage.platform.neoforge.support.render.DiskModelBaker;
import com.refinedmods.refinedstorage.platform.neoforge.support.render.RotationTranslationModelBaker;
import com.refinedmods.refinedstorage.platform.neoforge.support.render.TransformationBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Vector3f;

class DiskDriveBakedModel extends BakedModelWrapper<BakedModel> {
    private final LoadingCache<CacheKey, List<BakedQuad>> cache;
    private final DiskContainerItemOverrides<CacheKey> itemOverrides;
    private final Vector3f[] diskTranslations = new Vector3f[8];

    DiskDriveBakedModel(final BakedModel baseModel,
                        final RotationTranslationModelBaker baseModelBaker,
                        final DiskModelBaker diskModelBaker,
                        final RotationTranslationModelBaker ledInactiveModelBaker) {
        super(baseModel);
        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                this.diskTranslations[i++] = getDiskTranslation(x, y);
            }
        }
        this.cache = CacheBuilder.newBuilder().build(CacheLoader.from(cacheKey -> {
            final Transformation rotation = TransformationBuilder.create().rotate(cacheKey.direction).build();
            final RandomSource rand = RandomSource.create();
            final List<BakedQuad> quads = baseModelBaker.bake(rotation, cacheKey.side, rand);
            for (int j = 0; j < diskTranslations.length; ++j) {
                final Disk disk = cacheKey.disks[j];
                quads.addAll(getDiskQuads(diskModelBaker, cacheKey, disk, j));
                if (cacheKey.withInactiveLed && disk.state() == StorageState.INACTIVE) {
                    final Transformation ledTransform = TransformationBuilder
                        .create()
                        .rotate(cacheKey.direction)
                        .translate(diskTranslations[j])
                        .build();
                    quads.addAll(ledInactiveModelBaker.bake(ledTransform, cacheKey.side, rand));
                }
            }
            return quads;
        }));
        this.itemOverrides = new DiskContainerItemOverrides<>(
            cache,
            baseModel,
            diskTranslations,
            disks -> new CacheKey(null, true, BiDirection.NORTH, disks),
            CacheKey::withSide
        );
    }

    @Override
    public ItemOverrides getOverrides() {
        return itemOverrides;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state,
                                    @Nullable final Direction side,
                                    final RandomSource rand,
                                    final ModelData extraData,
                                    @Nullable final RenderType renderType) {
        if (state == null || !(state.getBlock() instanceof DiskDriveBlock diskDriveBlock)) {
            return super.getQuads(state, side, rand);
        }
        final BiDirection direction = diskDriveBlock.getDirection(state);
        if (direction == null) {
            return super.getQuads(state, side, rand);
        }
        final Disk[] disks = extraData.get(ForgeDiskDriveBlockEntity.DISKS_PROPERTY);
        if (disks == null) {
            return super.getQuads(state, side, rand);
        }
        return cache.getUnchecked(new CacheKey(side, false, direction, disks));
    }

    private List<BakedQuad> getDiskQuads(final DiskModelBaker diskBakers,
                                         final CacheKey cacheKey,
                                         final Disk disk,
                                         final int index) {
        if (disk.state() == StorageState.NONE || disk.item() == null) {
            return Collections.emptyList();
        }
        final RotationTranslationModelBaker diskBaker = diskBakers.forDisk(disk.item());
        if (diskBaker == null) {
            return Collections.emptyList();
        }
        final Transformation diskTransform = TransformationBuilder.create()
            .rotate(cacheKey.direction)
            .translate(diskTranslations[index])
            .build();
        return diskBaker.bake(diskTransform, cacheKey.side, RandomSource.create());
    }

    private static Vector3f getDiskTranslation(final int x, final int y) {
        return new Vector3f(
            x == 0 ? -(2F / 16F) : -(9F / 16F),
            -((y * 3F) / 16F) - (2F / 16F),
            0
        );
    }

    private record CacheKey(@Nullable Direction side, boolean withInactiveLed, BiDirection direction, Disk[] disks) {
        CacheKey withSide(final Direction newSide) {
            return new CacheKey(newSide, withInactiveLed, direction, disks);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final CacheKey cacheKey = (CacheKey) o;
            return side == cacheKey.side && direction == cacheKey.direction && Arrays.equals(disks, cacheKey.disks)
                && withInactiveLed == cacheKey.withInactiveLed;
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(side, direction, withInactiveLed);
            result = 31 * result + Arrays.hashCode(disks);
            return result;
        }

        @Override
        public String toString() {
            return "CacheKey{"
                + "side=" + side
                + ", withInactiveLed=" + withInactiveLed
                + ", direction=" + direction
                + ", disks=" + Arrays.toString(disks)
                + '}';
        }
    }
}
