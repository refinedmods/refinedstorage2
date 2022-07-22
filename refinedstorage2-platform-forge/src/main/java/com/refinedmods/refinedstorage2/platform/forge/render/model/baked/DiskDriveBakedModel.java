package com.refinedmods.refinedstorage2.platform.forge.render.model.baked;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.platform.common.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;
import com.refinedmods.refinedstorage2.platform.forge.block.entity.ForgeDiskDriveBlockEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.math.Vector3f;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBakedModel extends BakedModelWrapper<BakedModel> {
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
    private final BiFunction<BiDirection, Vector3f, BakedModel> diskModelBakery;
    private final Function<Vector3f, BakedModel> diskItemModelBakery;

    private final ItemOverrides overrides = new DiskDriveItemOverrides();

    private final LoadingCache<DiskDriveStateCacheKey, List<BakedQuad>> cache = CacheBuilder
        .newBuilder()
        .build(new DiskDriveCacheLoader());
    private final Map<Long, DiskDriveItemBakedModel> itemCache = new HashMap<>();

    public DiskDriveBakedModel(final Function<BiDirection, BakedModel> baseModelBakery,
                               final BakedModel baseModel,
                               final BiFunction<BiDirection, Vector3f, BakedModel> diskModelBakery,
                               final Function<Vector3f, BakedModel> diskItemModelBakery) {
        super(baseModel);
        this.baseModel = baseModel;
        this.baseModelBakery = baseModelBakery;
        this.diskModelBakery = diskModelBakery;
        this.diskItemModelBakery = diskItemModelBakery;
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state,
                                    @Nullable final Direction side,
                                    @NotNull final RandomSource rand,
                                    @NotNull final ModelData extraData,
                                    @Nullable final RenderType renderType) {
        if (state == null || !(state.getBlock() instanceof DiskDriveBlock diskDriveBlock)) {
            return super.getQuads(state, side, rand);
        }
        final BiDirection direction = diskDriveBlock.getDirection(state);
        if (direction == null) {
            return super.getQuads(state, side, rand);
        }
        final DiskDriveState driveState = extraData.get(ForgeDiskDriveBlockEntity.STATE_PROPERTY);
        if (driveState == null) {
            return super.getQuads(state, side, rand);
        }
        final DiskDriveStateCacheKey cacheKey = new DiskDriveStateCacheKey(
            state,
            side,
            driveState.getStates(),
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
            long disks = 0;
            for (int i = 0; i < TRANSLATORS.length; ++i) {
                if (AbstractDiskDriveBlockEntity.hasDisk(tag, i)) {
                    disks |= 1 << i;
                }
            }
            return itemCache.computeIfAbsent(disks, key -> new DiskDriveItemBakedModel(
                bakedModel,
                diskItemModelBakery,
                TRANSLATORS,
                key
            ));
        }
    }

    private static final class DiskDriveStateCacheKey {
        private final BlockState state;
        @Nullable
        private final Direction side;
        private final StorageDiskState[] diskStates;
        private final RandomSource random;
        private final BiDirection direction;

        DiskDriveStateCacheKey(final BlockState state,
                               @Nullable final Direction side,
                               final StorageDiskState[] diskStates,
                               final RandomSource random,
                               final BiDirection direction) {
            this.state = state;
            this.side = side;
            this.diskStates = diskStates;
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
            return state.equals(that.state) && side == that.side && Arrays.equals(diskStates, that.diskStates);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(state, side);
            result = 31 * result + Arrays.hashCode(diskStates);
            return result;
        }
    }

    private class DiskDriveCacheLoader extends CacheLoader<DiskDriveStateCacheKey, List<BakedQuad>> {
        @Override
        public List<BakedQuad> load(final DiskDriveStateCacheKey key) {
            final List<BakedQuad> quads = new ArrayList<>(getBaseQuads(key.state, key.random, key.side, key.direction));
            for (int i = 0; i < TRANSLATORS.length; ++i) {
                final StorageDiskState diskState = key.diskStates[i];
                if (diskState != StorageDiskState.NONE) {
                    quads.addAll(getDiskModel(key.state, key.random, key.side, key.direction, TRANSLATORS[i]));
                }
            }
            return quads;
        }

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

        private List<BakedQuad> getDiskModel(final BlockState state,
                                             final RandomSource rand,
                                             @Nullable final Direction side,
                                             final BiDirection direction,
                                             final Vector3f translation) {
            return diskModelBakery.apply(direction, translation).getQuads(
                state,
                side,
                rand
            );
        }
    }
}
