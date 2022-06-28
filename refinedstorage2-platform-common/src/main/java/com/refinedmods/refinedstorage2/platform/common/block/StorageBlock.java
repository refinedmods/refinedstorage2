package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.api.item.StorageItemHelper;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;

import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StorageBlock extends NetworkNodeContainerBlock {
    private static final Logger LOGGER = LogManager.getLogger();

    protected StorageBlock(final Properties properties) {
        super(properties);
    }

    public static class StorageBlockLootItemFunction implements LootItemFunction {
        @Override
        public LootItemFunctionType getType() {
            return LootFunctions.INSTANCE.getStorageBlock();
        }

        @Override
        public ItemStack apply(final ItemStack stack, final LootContext lootContext) {
            final BlockEntity blockEntity = lootContext.getParam(LootContextParams.BLOCK_ENTITY);
            if (blockEntity instanceof StorageBlockBlockEntity<?> storageBlockEntity) {
                apply(stack, storageBlockEntity);
            }
            return stack;
        }

        private void apply(final ItemStack stack, final StorageBlockBlockEntity<?> storageBlockEntity) {
            final UUID storageId = storageBlockEntity.getStorageId();
            if (storageId != null) {
                LOGGER.info("Transferred storage {} at {} to stack", storageId, storageBlockEntity.getBlockPos());
                StorageItemHelper.setStorageId(stack, storageId);
            } else {
                LOGGER.warn("Storage block {} has no associated storage ID!", storageBlockEntity.getBlockPos());
            }
        }
    }

    public static class StorageBlockLootItemFunctionSerializer implements Serializer<LootItemFunction> {
        @Override
        public void serialize(final JsonObject jsonObject,
                              final LootItemFunction lootItemFunction,
                              final JsonSerializationContext jsonSerializationContext) {
            // nothing to do
        }

        @Override
        public LootItemFunction deserialize(final JsonObject jsonObject,
                                            final JsonDeserializationContext jsonDeserializationContext) {
            return new StorageBlockLootItemFunction();
        }
    }
}
