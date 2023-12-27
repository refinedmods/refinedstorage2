package com.refinedmods.refinedstorage2.platform.forge.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.network.impl.node.multistorage.MultiStorageStorageState;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.DiskDriveDisk;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Vector3f;

class DiskDriveBakedModel extends BakedModelWrapper<BakedModel> {
    private static final Vector3f[] TRANSLATORS = new Vector3f[8];

    static {
        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                TRANSLATORS[i++] = new Vector3f(
                    x == 0 ? -(2F / 16F) : -(9F / 16F),
                    -((y * 3F) / 16F) - (2F / 16F),
                    0
                );
            }
        }
    }

    private final BakedModel baseModel;
    private final Function<BiDirection, BakedModel> baseModelBakery;
    private final Map<Item, BiFunction<BiDirection, Vector3f, BakedModel>> diskModelBakeries;
    private final Map<Item, Function<Vector3f, BakedModel>> diskItemModelBakeries;
    private final Function<Vector3f, BakedModel> ledInactiveModelBakery;

    private final ItemOverrides overrides = new DiskDriveItemOverrides();

    private final LoadingCache<DiskDriveStateCacheKey, List<BakedQuad>> cache = CacheBuilder
        .newBuilder()
        .build(new DiskDriveCacheLoader());
    private final Map<List<Item>, DiskDriveItemBakedModel> itemCache = new HashMap<>();

    DiskDriveBakedModel(final Function<BiDirection, BakedModel> baseModelBakery,
                        final BakedModel baseModel,
                        final Map<Item, BiFunction<BiDirection, Vector3f, BakedModel>> diskModelBakeries,
                        final Map<Item, Function<Vector3f, BakedModel>> diskItemModelBakeries,
                        final Function<Vector3f, BakedModel> ledInactiveModelBakery) {
        super(baseModel);
        this.baseModel = baseModel;
        this.baseModelBakery = baseModelBakery;
        this.diskModelBakeries = diskModelBakeries;
        this.diskItemModelBakeries = diskItemModelBakeries;
        this.ledInactiveModelBakery = ledInactiveModelBakery;
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state,
                                    @Nullable final Direction side,
                                    @Nonnull final RandomSource rand,
                                    @Nonnull final ModelData extraData,
                                    @Nullable final RenderType renderType) {
        if (state == null || !(state.getBlock() instanceof DiskDriveBlock diskDriveBlock)) {
            return super.getQuads(state, side, rand);
        }
        final BiDirection direction = diskDriveBlock.getDirection(state);
        if (direction == null) {
            return super.getQuads(state, side, rand);
        }
        final DiskDriveDisk[] disks = extraData.get(ForgeDiskDriveBlockEntity.DISKS_PROPERTY);
        if (disks == null) {
            return super.getQuads(state, side, rand);
        }
        final DiskDriveStateCacheKey cacheKey = new DiskDriveStateCacheKey(
            state,
            side,
            disks,
            rand,
            direction
        );
        return cache.getUnchecked(cacheKey);
    }

    private class DiskDriveItemOverrides extends ItemOverrides {
        @Nullable
        @Override
        public BakedModel resolve(final BakedModel bakedModel,
                                  final ItemStack stack,
                                  @Nullable final ClientLevel level,
                                  @Nullable final LivingEntity entity,
                                  final int seed) {
            final CompoundTag tag = BlockItem.getBlockEntityData(stack);
            if (tag == null) {
                return baseModel.getOverrides().resolve(bakedModel, stack, level, entity, seed);
            }
            final List<Item> disks = new ArrayList<>();
            for (int i = 0; i < TRANSLATORS.length; ++i) {
                final Item disk = AbstractDiskDriveBlockEntity.getDisk(tag, i);
                disks.add(disk);
            }
            return itemCache.computeIfAbsent(disks, key -> new DiskDriveItemBakedModel(
                bakedModel,
                diskItemModelBakeries,
                ledInactiveModelBakery,
                TRANSLATORS,
                disks
            ));
        }
    }

    private static final class DiskDriveStateCacheKey {
        private final BlockState state;
        @Nullable
        private final Direction side;
        private final DiskDriveDisk[] disks;
        private final RandomSource random;
        private final BiDirection direction;

        DiskDriveStateCacheKey(final BlockState state,
                               @Nullable final Direction side,
                               final DiskDriveDisk[] disks,
                               final RandomSource random,
                               final BiDirection direction) {
            this.state = state;
            this.side = side;
            this.disks = disks;
            this.random = random;
            this.direction = direction;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final DiskDriveStateCacheKey that = (DiskDriveStateCacheKey) o;
            return state.equals(that.state) && side == that.side && Arrays.equals(disks, that.disks);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(state, side);
            result = 31 * result + Arrays.hashCode(disks);
            return result;
        }
    }

    private class DiskDriveCacheLoader extends CacheLoader<DiskDriveStateCacheKey, List<BakedQuad>> {
        @Override
        public List<BakedQuad> load(final DiskDriveStateCacheKey key) {
            final List<BakedQuad> quads = new ArrayList<>(getBaseQuads(key.state, key.random, key.side, key.direction));
            for (int i = 0; i < TRANSLATORS.length; ++i) {
                final DiskDriveDisk disk = key.disks[i];
                quads.addAll(getDiskQuads(key, disk, i));
            }
            return quads;
        }

        @SuppressWarnings("deprecation")
        private List<BakedQuad> getDiskQuads(final DiskDriveStateCacheKey key,
                                             final DiskDriveDisk disk,
                                             final int index) {
            if (disk.state() == MultiStorageStorageState.NONE) {
                return Collections.emptyList();
            }
            final var diskModelBakery = diskModelBakeries.get(disk.item());
            if (diskModelBakery == null) {
                return Collections.emptyList();
            }
            return diskModelBakery.apply(key.direction, TRANSLATORS[index]).getQuads(
                key.state,
                key.side,
                key.random
            );
        }

        @SuppressWarnings("deprecation")
        private List<BakedQuad> getBaseQuads(final BlockState state,
                                             final RandomSource rand,
                                             @Nullable final Direction side,
                                             final BiDirection direction) {
            return baseModelBakery.apply(direction).getQuads(
                state,
                side,
                rand
            );
        }
    }
}
