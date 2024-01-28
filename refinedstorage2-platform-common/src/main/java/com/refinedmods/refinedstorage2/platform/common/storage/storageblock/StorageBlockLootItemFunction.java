package com.refinedmods.refinedstorage2.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.ItemTransferableStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class StorageBlockLootItemFunction implements LootItemFunction {
    @Override
    public LootItemFunctionType getType() {
        return LootFunctions.INSTANCE.getStorageBlock();
    }

    @Override
    public ItemStack apply(final ItemStack stack, final LootContext lootContext) {
        final BlockEntity blockEntity = lootContext.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ItemTransferableStorageBlockEntity transferable) {
            PlatformApi.INSTANCE.getStorageContainerItemHelper().transferFromBlockEntity(stack, transferable);
        }
        return stack;
    }
}
