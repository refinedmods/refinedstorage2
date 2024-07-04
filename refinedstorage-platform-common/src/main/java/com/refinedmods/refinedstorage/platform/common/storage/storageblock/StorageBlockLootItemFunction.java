package com.refinedmods.refinedstorage.platform.common.storage.storageblock;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.storage.StorageBlockEntity;
import com.refinedmods.refinedstorage.platform.common.content.LootFunctions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class StorageBlockLootItemFunction implements LootItemFunction {
    @Override
    public LootItemFunctionType<StorageBlockLootItemFunction> getType() {
        return LootFunctions.INSTANCE.getStorageBlock();
    }

    @Override
    public ItemStack apply(final ItemStack stack, final LootContext lootContext) {
        final BlockEntity blockEntity = lootContext.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof StorageBlockEntity transferable) {
            PlatformApi.INSTANCE.getStorageContainerItemHelper().transferFromBlockEntity(stack, transferable);
        }
        return stack;
    }
}
