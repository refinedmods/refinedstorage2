package com.refinedmods.refinedstorage.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.apache.commons.lang3.function.TriConsumer;

import static com.refinedmods.refinedstorage.common.GameTestUtil.MOD_BLOCKS;
import static net.minecraft.core.BlockPos.ZERO;

final class DiskInterfaceTestPlots {
    private DiskInterfaceTestPlots() {
    }

    static void preparePlot(final GameTestHelper helper,
                            final Direction direction,
                            final TriConsumer<AbstractDiskInterfaceBlockEntity, BlockPos, GameTestSequence> consumer) {
        helper.setBlock(ZERO.above(), MOD_BLOCKS.getCreativeController().getDefault());
        helper.setBlock(ZERO.above().above(), MOD_BLOCKS.getItemStorageBlock(ItemStorageVariant.ONE_K));
        final BlockPos diskInterfacePos = ZERO.above().above().above();
        helper.setBlock(diskInterfacePos, MOD_BLOCKS.getDiskInterface().getDefault()
            .rotated(OrientedDirection.forDirection(direction)));
        consumer.accept(
            helper.getBlockEntity(diskInterfacePos, AbstractDiskInterfaceBlockEntity.class),
            diskInterfacePos,
            helper.startSequence()
        );
    }

    static void addDiskToDiskInterface(final GameTestHelper helper,
                                       final BlockPos diskInterfacePos,
                                       final ResourceAmount... resources) {
        final ItemStack diskItem = new ItemStack(Items.INSTANCE.getItemStorageDisk(ItemStorageVariant.SIXTY_FOUR_K));
        diskItem.inventoryTick(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL), EquipmentSlot.BODY);

        final Storage storage = getStorageFromDisk(helper, diskItem);
        if (resources.length > 0) {
            final PlayerActor actor = new PlayerActor(helper.makeMockPlayer(GameType.SURVIVAL));
            for (final ResourceAmount resource : resources) {
                if (resource != null) {
                    storage.insert(resource.resource(), resource.amount(), Action.EXECUTE, actor);
                }
            }
        }

        final var diskInterfaceBlockEntity = helper.getBlockEntity(diskInterfacePos,
            AbstractDiskInterfaceBlockEntity.class);
        final FilteredContainer diskInterfaceContainer = diskInterfaceBlockEntity.getDiskInventory();
        diskInterfaceContainer.addItem(diskItem);
    }

    static void isDiskInOutputWithAmount(final GameTestHelper helper,
                                         final BlockPos diskInterfacePos,
                                         final int storedAmount) {
        final var diskInterfaceBlockEntity = helper.getBlockEntity(diskInterfacePos,
            AbstractDiskInterfaceBlockEntity.class);
        final FilteredContainer diskInterfaceContainer = diskInterfaceBlockEntity.getDiskInventory();

        final ItemStack diskItem = diskInterfaceContainer.getItem(3);
        final Storage storage = getStorageFromDisk(helper, diskItem);

        helper.assertTrue(!diskItem.isEmpty(), "Could not find a Storage Disk in the output slot");
        helper.assertTrue(storage.getStored() == storedAmount, "Expected Storage Disk stored amount "
            + storedAmount + ", but was " + storage.getStored());
    }

    private static Storage getStorageFromDisk(final GameTestHelper helper,
                                              final ItemStack diskItem) {
        return RefinedStorageApi.INSTANCE
            .getStorageContainerItemHelper()
            .resolveStorage(RefinedStorageApi.INSTANCE.getStorageRepository(helper.getLevel()), diskItem)
            .orElseThrow(() -> helper.assertionException("Couldn't find SerializableStorage from Storage Disk"));
    }
}
