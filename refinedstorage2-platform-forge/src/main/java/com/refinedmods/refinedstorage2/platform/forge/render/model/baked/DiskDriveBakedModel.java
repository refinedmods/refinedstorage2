package com.refinedmods.refinedstorage2.platform.forge.render.model.baked;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.platform.common.block.BaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.DiskDriveBlockEntity;
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
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBakedModel extends ForwardingBakedModel {
    private static final Vector3f[] TRANSLATORS = new Vector3f[8];

    static {
        int i = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 2; ++x) {
                TRANSLATORS[i++] = new Vector3f(x == 0 ? -(2F / 16F) : -(9F / 16F), -((y * 3F) / 16F) - (2F / 16F), 0);
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

    public DiskDriveBakedModel(BakedModel baseModel, BakedModel diskModel, BakedModel diskDisconnectedModel) {
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
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull IModelData extraData) {
        if (state == null || !state.hasProperty(BaseBlock.DIRECTION)) {
            return super.getQuads(state, side, rand);
        }
        DiskDriveState driveState = extraData.getData(ForgeDiskDriveBlockEntity.STATE_PROPERTY);
        if (driveState == null) {
            return super.getQuads(state, side, rand);
        }
        DiskDriveStateCacheKey cacheKey = new DiskDriveStateCacheKey(state, side, driveState.getStates(), rand);
        return cache.getUnchecked(cacheKey);
    }

    private class DiskDriveItemOverrides extends ItemOverrides {
        @Nullable
        @Override
        public BakedModel resolve(BakedModel bakedModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            CompoundTag tag = BlockItem.getBlockEntityData(stack);
            if (tag == null) {
                return baseModel.getOverrides().resolve(bakedModel, stack, level, entity, seed);
            }
            long disks = 0;
            for (int i = 0; i < TRANSLATORS.length; ++i) {
                if (DiskDriveBlockEntity.hasDisk(tag, i)) {
                    disks |= 1 << i;
                }
            }
            return itemModelCache.computeIfAbsent(disks, key -> new DiskDriveItemBakedModel(baseModel, diskDisconnectedModel, TRANSLATORS, key));
        }
    }

    private static final class DiskDriveStateCacheKey {
        private final BlockState state;
        private final Direction side;
        private final StorageDiskState[] diskStates;
        private final RandomSource random;

        public DiskDriveStateCacheKey(BlockState state, Direction side, StorageDiskState[] diskStates, RandomSource random) {
            this.state = state;
            this.side = side;
            this.diskStates = diskStates;
            this.random = random;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DiskDriveStateCacheKey that = (DiskDriveStateCacheKey) o;
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
        public List<BakedQuad> load(DiskDriveStateCacheKey key) {
            BiDirection direction = key.state.getValue(BaseBlock.DIRECTION);
            return QuadTransformer.transformSideAndRotate(resultingSide -> getQuads(key.state, key.random, key.diskStates, resultingSide), direction, key.side);
        }

        @NotNull
        private List<BakedQuad> getQuads(@NotNull BlockState state, @NotNull RandomSource rand, StorageDiskState[] diskStates, Direction side) {
            List<BakedQuad> quads = new ArrayList<>(baseModel.getQuads(state, side, rand));
            for (int i = 0; i < TRANSLATORS.length; ++i) {
                StorageDiskState diskState = diskStates[i];
                if (diskState != StorageDiskState.NONE) {
                    quads.addAll(getDiskModel(state, rand, side, TRANSLATORS[i]));
                }
            }
            return quads;
        }

        private List<BakedQuad> getDiskModel(@NotNull BlockState state, @NotNull RandomSource rand, Direction side, Vector3f translation) {
            List<BakedQuad> diskQuads = diskModel.getQuads(state, side, rand);
            return QuadTransformer.translate(diskQuads, translation);
        }
    }
}
