package com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.AbstractStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageConfigurationContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerImpl;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractStorageBlockContainerMenu extends AbstractStorageContainerMenu
    implements StorageAccessor {
    private static final int FILTER_SLOT_X = 8;
    private static final int FILTER_SLOT_Y = 20;

    private long stored;
    private long capacity;

    protected <T> AbstractStorageBlockContainerMenu(final MenuType<?> type,
                                                    final int syncId,
                                                    final Player player,
                                                    final FriendlyByteBuf buf,
                                                    final ResourceFactory<T> resourceFactory) {
        super(type, syncId);
        this.stored = buf.readLong();
        this.capacity = buf.readLong();
        addSlots(player, ResourceContainerImpl.createForFilter(resourceFactory));
        initializeResourceSlots(buf);
    }

    protected AbstractStorageBlockContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Player player,
                                                final ResourceContainer resourceContainer,
                                                final StorageConfigurationContainer configContainer) {
        super(type, syncId, player, configContainer);
        addSlots(player, resourceContainer);
    }

    private void addSlots(final Player player, final ResourceContainer resourceContainer) {
        for (int i = 0; i < resourceContainer.size(); ++i) {
            addSlot(createFilterSlot(resourceContainer, i));
        }
        addPlayerInventory(player.getInventory(), 8, 141);

        transferManager.addFilterTransfer(player.getInventory());
    }

    private Slot createFilterSlot(final ResourceContainer resourceContainer, final int i) {
        final int x = FILTER_SLOT_X + (18 * i);
        return new ResourceSlot(
            resourceContainer,
            i,
            createTranslation("gui", "storage.filter_help"),
            x,
            FILTER_SLOT_Y
        );
    }

    @Override
    public double getProgress() {
        if (capacity == 0) {
            return 0;
        }
        return (double) getStored() / (double) getCapacity();
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getStored() {
        return stored;
    }
}
