package com.refinedmods.refinedstorage.common.storage.portablegrid;

import com.refinedmods.refinedstorage.common.support.energy.AbstractEnergyLootItemFunction;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class PortableGridLootItemFunction extends AbstractEnergyLootItemFunction {
    public static final MapCodec<? extends LootItemFunction> FUNCTION_CODEC =
        MapCodec.unit(PortableGridLootItemFunction::new);

    @Override
    public ItemStack apply(final ItemStack itemStack, final LootContext lootContext) {
        final BlockEntity blockEntity = lootContext.getParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof AbstractPortableGridBlockEntity portableGrid) {
            PortableGridBlockItem.setDiskInventory(
                itemStack,
                portableGrid.getDiskInventory(),
                lootContext.getLevel().registryAccess()
            );
        }
        return super.apply(itemStack, lootContext);
    }

    @Override
    public MapCodec<? extends LootItemFunction> codec() {
        return FUNCTION_CODEC;
    }
}
