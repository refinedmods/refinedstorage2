package com.refinedmods.refinedstorage2.platform.common.item.block;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.api.item.StorageItemHelper;
import com.refinedmods.refinedstorage2.platform.api.item.block.AbstractStorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageTooltipHelper;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.AbstractStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemStorageBlockBlockItem extends AbstractStorageBlockBlockItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemStorageBlockBlockItem.class);

    private final ItemStorageType.Variant variant;
    private final Set<StorageTooltipHelper.TooltipOption> tooltipOptions =
        EnumSet.noneOf(StorageTooltipHelper.TooltipOption.class);

    public ItemStorageBlockBlockItem(final Block block,
                                     final CreativeModeTab tab,
                                     final ItemStorageType.Variant variant) {
        super(block, new Item.Properties().tab(tab).stacksTo(1).fireResistant());
        this.variant = variant;
        this.tooltipOptions.add(StorageTooltipHelper.TooltipOption.STACK_INFO);
        if (variant != ItemStorageType.Variant.CREATIVE) {
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
            QuantityFormatter::formatWithUnits,
            QuantityFormatter::format,
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
        if (variant == ItemStorageType.Variant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getItemStoragePart(variant), count);
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
