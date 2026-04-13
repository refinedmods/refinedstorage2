package com.refinedmods.refinedstorage.common.storage.diskdrive;

import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_ITEMS;
import static net.minecraft.core.BlockPos.ZERO;

final class DiskDriveTestPlots {
    private DiskDriveTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final boolean itemStorage,
                            final TriConsumer<AbstractDiskDriveBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), MOD_BLOCKS.getCreativeController().getDefault());
        final BlockPos storagePos = ZERO.above().above();
        helper.setBlock(storagePos, MOD_BLOCKS.getDiskDrive());

        final AbstractDiskDriveBlockEntity diskDriveBlockEntity =
            helper.getBlockEntity(storagePos, AbstractDiskDriveBlockEntity.class);
        final ItemStack storageDisk;
        if (itemStorage) {
            storageDisk = new ItemStack(MOD_ITEMS.getItemStorageDisk(ItemStorageVariant.ONE_K), 1);
        } else {
            storageDisk = MOD_ITEMS.getFluidStorageDisk(FluidStorageVariant.SIXTY_FOUR_B).getDefaultInstance();
        }
        storageDisk.inventoryTick(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL), EquipmentSlot.BODY);
        diskDriveBlockEntity.getDiskInventory().setItem(0, storageDisk);
        diskDriveBlockEntity.setChanged();

        consumer.accept(
            diskDriveBlockEntity,
            storagePos,
            helper.startSequence()
        );
    }
}
