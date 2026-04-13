package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockEntity;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class StorageBlockLootItemFunction implements LootItemFunction {
    public static final MapCodec<? extends LootItemFunction> FUNCTION_CODEC =
        MapCodec.unit(StorageBlockLootItemFunction::new);

    @Override
    public ItemStack apply(final ItemStack stack, final LootContext lootContext) {
        final BlockEntity blockEntity = lootContext.getParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof StorageBlockEntity transferable) {
            RefinedStorageApi.INSTANCE.getStorageContainerItemHelper().transferFromBlockEntity(stack, transferable);
        }
        return stack;
    }

    @Override
    public MapCodec<? extends LootItemFunction> codec() {
        return FUNCTION_CODEC;
    }
}
