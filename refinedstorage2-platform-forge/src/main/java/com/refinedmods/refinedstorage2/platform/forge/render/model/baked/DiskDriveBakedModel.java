package com.refinedmods.refinedstorage2.platform.forge.render.model.baked;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;
import com.refinedmods.refinedstorage2.platform.forge.block.entity.ForgeDiskDriveBlockEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.math.Vector3f;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBakedModel extends AbstractForwardingBakedModel {
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

    private final BakedModel diskDisconnectedModel;
    private final BakedModel diskModel;
    private final ItemOverrides overrides = new DiskDriveItemOverrides();
    private final LoadingCache<DiskDriveStateCacheKey, List<BakedQuad>> cache = CacheBuilder
        .newBuilder()
        .build(new DiskDriveCacheLoader());
    private final Map<Long, DiskDriveItemBakedModel> itemModelCache = new HashMap<>();

    public DiskDriveBakedModel(final BakedModel baseModel,
                               final BakedModel diskModel,
                               final BakedModel diskDisconnectedModel) {
        super(baseModel);
        this.diskModel = diskModel;
        this.diskDisconnectedModel = diskDisconnectedModel;
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
                                    @NotNull final IModelData extraData) {
        if (state == null || !state.hasProperty(AbstractBaseBlock.DIRECTION)) {
            return super.getQuads(state, side, rand);
        }
        final DiskDriveState driveState = extraData.getData(ForgeDiskDriveBlockEntity.STATE_PROPERTY);
        if (driveState == null) {
            return super.getQuads(state, side, rand);
        }
        final DiskDriveStateCacheKey cacheKey = new DiskDriveStateCacheKey(state, side, driveState.getStates(), rand);
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
            return itemModelCache.computeIfAbsent(disks, key -> new DiskDriveItemBakedModel(
                baseModel,
                diskDisconnectedModel,
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

        DiskDriveStateCacheKey(final BlockState state,
                               @Nullable final Direction side,
                               final StorageDiskState[] diskStates,
                               final RandomSource random) {
            this.state = state;
            this.side = side;
            this.diskStates = diskStates;
            this.random = random;
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
            final BiDirection direction = key.state.getValue(AbstractBaseBlock.DIRECTION);
            return QuadTransformer.transformSideAndRotate(resultingSide -> getQuads(
                key.state,
                key.random,
                key.diskStates,
                resultingSide
            ), direction, key.side);
        }

        @NotNull
        private List<BakedQuad> getQuads(@NotNull final BlockState state,
                                         @NotNull final RandomSource rand,
                                         final StorageDiskState[] diskStates,
                                         @Nullable final Direction side) {
            final List<BakedQuad> quads = new ArrayList<>(baseModel.getQuads(
                state,
                side,
                rand,
                EmptyModelData.INSTANCE
            ));
            for (int i = 0; i < TRANSLATORS.length; ++i) {
                final StorageDiskState diskState = diskStates[i];
                if (diskState != StorageDiskState.NONE) {
                    quads.addAll(getDiskModel(state, rand, side, TRANSLATORS[i]));
                }
            }
            return quads;
        }

        private List<BakedQuad> getDiskModel(final BlockState state,
                                             final RandomSource rand,
                                             @Nullable final Direction side,
                                             final Vector3f translation) {
            final List<BakedQuad> diskQuads = diskModel.getQuads(state, side, rand, EmptyModelData.INSTANCE);
            return QuadTransformer.translate(diskQuads, translation);
        }
    }
}
