package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.Property;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.DisabledSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;
import com.refinedmods.refinedstorage2.platform.common.containermenu.transfer.TransferManager;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractBaseContainerMenu extends AbstractContainerMenu {
    protected final TransferManager transferManager;
    @Nullable
    protected PlayerSlotReference disabledPlayerInventorySlot;
    private final Map<PropertyType<?>, Property<?>> propertyMap = new HashMap<>();

    protected AbstractBaseContainerMenu(final MenuType<?> type, final int syncId) {
        super(type, syncId);
        this.transferManager = Platform.INSTANCE.createTransferManager(this);
    }

    @SuppressWarnings("unchecked")
    public <T> ClientProperty<T> getProperty(final PropertyType<T> type) {
        return (ClientProperty<T>) propertyMap.get(type);
    }

    public void receivePropertyChangeFromClient(final ResourceLocation id, final int newValue) {
        for (final Map.Entry<PropertyType<?>, Property<?>> entry : propertyMap.entrySet()) {
            final PropertyType<?> type = entry.getKey();
            if (!type.id().equals(id)) {
                continue;
            }
            final Property<?> property = entry.getValue();
            ((ServerProperty<?>) property).set(newValue);
        }
    }

    protected <T> void registerProperty(final Property<T> property) {
        propertyMap.put(property.getType(), property);
        addDataSlot(property.getDataSlot());
    }

    protected void resetSlots() {
        slots.clear();
    }

    protected void addPlayerInventory(final Inventory inventory, final int inventoryX, final int inventoryY) {
        int id = 9;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(inventory, id++, inventoryX + x * 18, inventoryY + y * 18));
            }
        }

        id = 0;
        for (int i = 0; i < 9; i++) {
            final int x = inventoryX + i * 18;
            final int y = inventoryY + 4 + (3 * 18);
            final boolean disabled = disabledPlayerInventorySlot != null
                && disabledPlayerInventorySlot.getSlotIndex() == id;
            addSlot(disabled ? new DisabledSlot(inventory, id, x, y) : new Slot(inventory, id, x, y));
            id++;
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(final Player player, final int index) {
        transferManager.transfer(index);
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(final int id, final int dragType, final ClickType clickType, final Player player) {
        final Slot slot = id >= 0 ? getSlot(id) : null;
        if (isSwappingDisabledSlotWithNumberKeys(dragType, clickType)) {
            return;
        }
        if (slot instanceof DisabledSlot) {
            return;
        }
        super.clicked(id, dragType, clickType, player);
    }

    private boolean isSwappingDisabledSlotWithNumberKeys(final int dragType, final ClickType clickType) {
        return disabledPlayerInventorySlot != null
            && clickType == ClickType.SWAP
            && dragType == disabledPlayerInventorySlot.getSlotIndex();
    }
}
