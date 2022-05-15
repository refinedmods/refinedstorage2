package com.refinedmods.refinedstorage2.platform.common.item.block;

import com.refinedmods.refinedstorage2.platform.common.block.StorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.StorageBlockEntity;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StorageBlockItem extends BlockItem {
    private static final Logger LOGGER = LogManager.getLogger();

    public StorageBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack, BlockState state) {
        if (!level.isClientSide() && stack.hasTag() && stack.getTag().hasUUID(StorageBlock.TAG_ID)) {
            UUID id = stack.getTag().getUUID(StorageBlock.TAG_ID);
            if (level.getBlockEntity(pos) instanceof StorageBlockEntity<?> storageBlockEntity) {
                LOGGER.info("Transferred storage {} to block at {}", id, pos);
                storageBlockEntity.modifyStorageAfterAlreadyInitialized(id);
            } else {
                LOGGER.warn("Storage {} could not be set, block entity does not exist yet at {}", id, pos);
            }
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }
}
