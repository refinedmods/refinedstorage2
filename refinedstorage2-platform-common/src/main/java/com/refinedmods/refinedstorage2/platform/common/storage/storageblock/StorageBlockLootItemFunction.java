package com.refinedmods.refinedstorage2.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;

import java.util.UUID;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageBlockLootItemFunction implements LootItemFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageBlockLootItemFunction.class);

    @Override
    public LootItemFunctionType getType() {
        return LootFunctions.INSTANCE.getStorageBlock();
    }

    @Override
    public ItemStack apply(final ItemStack stack, final LootContext lootContext) {
        final BlockEntity blockEntity = lootContext.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof AbstractStorageBlockBlockEntity<?> storageBlockEntity) {
            apply(stack, storageBlockEntity);
        }
        return stack;
    }

    private void apply(final ItemStack stack, final AbstractStorageBlockBlockEntity<?> storageBlockEntity) {
        final UUID storageId = storageBlockEntity.getStorageId();
        if (storageId != null) {
            LOGGER.debug("Transferred storage {} at {} to stack", storageId, storageBlockEntity.getBlockPos());
            PlatformApi.INSTANCE.getStorageContainerItemHelper().setId(stack, storageId);
        } else {
            LOGGER.warn("Storage block {} has no associated storage ID!", storageBlockEntity.getBlockPos());
        }
    }
}
