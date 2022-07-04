package com.refinedmods.refinedstorage2.platform.common.item.block;

import com.refinedmods.refinedstorage2.platform.api.item.StorageItemHelper;
import com.refinedmods.refinedstorage2.platform.api.item.block.AbstractStorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.AbstractStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FluidStorageBlockBlockItem extends AbstractStorageBlockBlockItem {
    private static final Logger LOGGER = LogManager.getLogger();

    private final FluidStorageType.Variant variant;
    private final Set<StorageTooltipHelper.TooltipOption> tooltipOptions =
        EnumSet.noneOf(StorageTooltipHelper.TooltipOption.class);

    public FluidStorageBlockBlockItem(final Block block,
                                      final CreativeModeTab tab,
                                      final FluidStorageType.Variant variant) {
        super(block, new Item.Properties().tab(tab).stacksTo(1).fireResistant());
        this.variant = variant;
        if (variant != FluidStorageType.Variant.CREATIVE) {
            this.tooltipOptions.add(StorageTooltipHelper.TooltipOption.CAPACITY_AND_PROGRESS);
        }
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                @Nullable final Level level,
                                final List<Component> tooltip,
                                final TooltipFlag context) {
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
    protected ItemStack createPrimaryDisassemblyByproduct(final int count) {
        return new ItemStack(Blocks.INSTANCE.getMachineCasing(), count);
    }

    @Override
    @Nullable
    protected ItemStack createSecondaryDisassemblyByproduct(final int count) {
        if (variant == FluidStorageType.Variant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getFluidStoragePart(variant), count);
    }

    @Override
    protected void updateBlockEntityWithStorageId(final BlockPos pos,
                                                  @Nullable final BlockEntity blockEntity,
                                                  final UUID id) {
        if (blockEntity instanceof AbstractStorageBlockBlockEntity<?> storageBlockEntity) {
            LOGGER.info("Transferred storage {} to block at {}", id, pos);
            storageBlockEntity.modifyStorageIdAfterAlreadyInitialized(id);
        } else {
            LOGGER.warn("Storage {} could not be set, block entity does not exist yet at {}", id, pos);
        }
    }
}
