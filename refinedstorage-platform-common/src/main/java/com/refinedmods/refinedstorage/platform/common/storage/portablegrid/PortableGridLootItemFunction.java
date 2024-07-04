package com.refinedmods.refinedstorage.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage.platform.common.content.LootFunctions;
import com.refinedmods.refinedstorage.platform.common.support.energy.EnergyLootItemFunction;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class PortableGridLootItemFunction extends EnergyLootItemFunction {
    @Override
    public LootItemFunctionType<? extends EnergyLootItemFunction> getType() {
        return LootFunctions.INSTANCE.getPortableGrid();
    }

    @Override
    public ItemStack apply(final ItemStack itemStack, final LootContext lootContext) {
        final BlockEntity blockEntity = lootContext.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof AbstractPortableGridBlockEntity portableGrid) {
            PortableGridBlockItem.setDiskInventory(
                itemStack,
                portableGrid.getDiskInventory(),
                lootContext.getLevel().registryAccess()
            );
        }
        return super.apply(itemStack, lootContext);
    }
}
