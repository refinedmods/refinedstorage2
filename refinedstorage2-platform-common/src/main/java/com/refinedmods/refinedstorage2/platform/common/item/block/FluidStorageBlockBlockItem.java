package com.refinedmods.refinedstorage2.platform.common.item.block;

import com.refinedmods.refinedstorage2.platform.api.item.StorageItemHelper;
import com.refinedmods.refinedstorage2.platform.api.item.block.StorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FluidStorageBlockBlockItem extends StorageBlockBlockItem {
    private static final Logger LOGGER = LogManager.getLogger();

    private final FluidStorageType.Variant variant;
    private final Set<StorageTooltipHelper.TooltipOption> tooltipOptions = EnumSet.noneOf(StorageTooltipHelper.TooltipOption.class);

    public FluidStorageBlockBlockItem(Block block, Properties properties, FluidStorageType.Variant variant) {
        super(block, properties);
        this.variant = variant;
        if (variant != FluidStorageType.Variant.CREATIVE) {
            this.tooltipOptions.add(StorageTooltipHelper.TooltipOption.CAPACITY_AND_PROGRESS);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, level, tooltip, context);
        StorageItemHelper.appendToTooltip(
                stack,
                level,
                tooltip,
                context,
                Platform.INSTANCE.getBucketQuantityFormatter()::formatWithUnits,
                Platform.INSTANCE.getBucketQuantityFormatter()::format,
                tooltipOptions
        );
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
    protected void updateBlockEntityWithStorageId(BlockPos pos, BlockEntity blockEntity, UUID id) {
        if (blockEntity instanceof StorageBlockBlockEntity<?> storageBlockEntity) {
            LOGGER.info("Transferred storage {} to block at {}", id, pos);
            storageBlockEntity.modifyStorageIdAfterAlreadyInitialized(id);
        } else {
            LOGGER.warn("Storage {} could not be set, block entity does not exist yet at {}", id, pos);
        }
    }
}
