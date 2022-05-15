package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;

import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.nbt.CompoundTag;
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
    public static final String TAG_ID = "id";

    private static final Logger LOGGER = LogManager.getLogger();

    protected StorageBlock(Properties properties) {
        super(properties);
    }

    public static class StorageBlockLootItemFunction implements LootItemFunction {
        @Override
        public LootItemFunctionType getType() {
            return LootFunctions.INSTANCE.getStorageBlock();
        }

        @Override
        public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
            BlockEntity blockEntity = lootContext.getParam(LootContextParams.BLOCK_ENTITY);
            if (blockEntity instanceof StorageBlockEntity<?> storageBlockEntity) {
                CompoundTag tag = itemStack.hasTag() ? itemStack.getTag() : new CompoundTag();
                apply(tag, storageBlockEntity);
                itemStack.setTag(tag);
            }
            return itemStack;
        }

        private void apply(CompoundTag tag, StorageBlockEntity<?> storageBlockEntity) {
            UUID storageId = storageBlockEntity.getStorageId();
            if (storageId != null) {
                LOGGER.info("Transferred storage {} at {} to stack", storageId, storageBlockEntity.getBlockPos());
                tag.putUUID(TAG_ID, storageId);
            } else {
                LOGGER.warn("Storage block {} has no associated storage ID!", storageBlockEntity.getBlockPos());
            }
        }
    }

    public static class StorageBlockLootItemFunctionSerializer implements Serializer<LootItemFunction> {
        @Override
        public void serialize(JsonObject jsonObject, LootItemFunction lootItemFunction, JsonSerializationContext jsonSerializationContext) {
        }

        @Override
        public LootItemFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new StorageBlockLootItemFunction();
        }
    }
}
