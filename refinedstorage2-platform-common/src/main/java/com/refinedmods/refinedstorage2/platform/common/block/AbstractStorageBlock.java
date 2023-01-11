package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.AbstractStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;

import java.util.UUID;
import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStorageBlock<T extends AbstractStorageBlockBlockEntity<?>>
    extends AbstractBaseBlock
    implements EntityBlock {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageBlock.class);

    private final AbstractBlockEntityTicker<T> ticker;

    protected AbstractStorageBlock(final Properties properties, final AbstractBlockEntityTicker<T> ticker) {
        super(properties);
        this.ticker = ticker;
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return ticker.get(level, type);
    }

    public static class StorageBlockLootItemFunction implements LootItemFunction {
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
                LOGGER.info("Transferred storage {} at {} to stack", storageId, storageBlockEntity.getBlockPos());
                PlatformApi.INSTANCE.getStorageContainerHelper().setId(stack, storageId);
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
