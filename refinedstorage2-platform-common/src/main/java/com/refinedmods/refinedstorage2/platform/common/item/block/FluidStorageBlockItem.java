package com.refinedmods.refinedstorage2.platform.common.item.block;

import com.refinedmods.refinedstorage2.platform.api.item.block.StorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FluidStorageBlockItem extends StorageBlockBlockItem {
    private static final Logger LOGGER = LogManager.getLogger();

    private final FluidStorageType.Variant variant;

    public FluidStorageBlockItem(Block block, Properties properties, FluidStorageType.Variant variant) {
        super(block, properties);
        this.variant = variant;
    }

    @Override
    protected ItemStack createPrimaryDisassemblyByproduct(int count) {
        return new ItemStack(Blocks.INSTANCE.getMachineCasing(), count);
    }

    @Override
    protected ItemStack createSecondaryDisassemblyByproduct(int count) {
        if (variant == FluidStorageType.Variant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getFluidStoragePart(variant), count);
    }

    @Override
    protected String formatQuantity(long qty) {
        return Platform.INSTANCE.getBucketQuantityFormatter().formatWithUnits(qty);
    }

    @Override
    protected void updateBlockEntityWithStorageId(BlockPos pos, BlockEntity blockEntity, UUID id) {
        if (blockEntity instanceof StorageBlockBlockEntity<?> storageBlockEntity) {
            LOGGER.info("Transferred storage {} to block at {}", id, pos);
            storageBlockEntity.modifyStorageIdAfterAlreadyInitialized(id);
        } else {
            LOGGER.warn("Storage {} could not be set, block entity does not exist yet at {}", id, pos);
        }
    }
}
