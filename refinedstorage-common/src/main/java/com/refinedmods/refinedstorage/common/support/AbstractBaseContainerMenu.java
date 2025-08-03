package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.DisabledSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.FilterSlot;
import com.refinedmods.refinedstorage.common.support.containermenu.Property;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.TransferManager;

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

import static java.util.Objects.requireNonNull;

public abstract class AbstractBaseContainerMenu extends AbstractContainerMenu {
    protected final TransferManager transferManager;
    @Nullable
    protected SlotReference disabledSlot;
    private final Map<PropertyType<?>, Property<?>> propertyMap = new HashMap<>();

    protected AbstractBaseContainerMenu(@Nullable final MenuType<?> type, final int syncId) {
        super(type, syncId);
        this.transferManager = Platform.INSTANCE.createTransferManager(this);
    }

    public <T> boolean hasProperty(final PropertyType<T> type) {
        return propertyMap.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    public <T> ClientProperty<T> getProperty(final PropertyType<T> type) {
        return (ClientProperty<T>) requireNonNull(propertyMap.get(type), "Property not found");
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

    protected final void addPlayerInventory(final Inventory inventory,
                                            final int inventoryX,
                                            final int inventoryY) {
        addPlayerInventory(inventory, inventoryX, inventoryY, null);
    }

    protected final void addPlayerInventory(final Inventory inventory,
                                            final int inventoryX,
                                            final int inventoryY,
                                            @Nullable final PlayerInventoryListener listener) {
        int id = 9;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                final int slotX = inventoryX + x * 18;
                final int slotY = inventoryY + y * 18;
                final boolean disabled = disabledSlot != null
                    && disabledSlot.isDisabledSlot(id);
                final Slot slot = disabled
                    ? new DisabledSlot(inventory, id, slotX, slotY)
                    : new PlayerInventorySlot(inventory, id, slotX, slotY, listener);
                addSlot(slot);
                id++;
            }
        }

        id = 0;
        for (int i = 0; i < 9; i++) {
            final int x = inventoryX + i * 18;
            final int y = inventoryY + 4 + (3 * 18);
            final boolean disabled = disabledSlot != null
                && disabledSlot.isDisabledSlot(id);
            final Slot slot = disabled
                ? new DisabledSlot(inventory, id, x, y)
                : new PlayerInventorySlot(inventory, id, x, y, listener);
            addSlot(slot);
            id++;
        }
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
        if (slot instanceof FilterSlot) {
            final ItemStack carried = player.containerMenu.getCarried();
            if (carried.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else if (slot.mayPlace(carried)) {
                slot.set(carried.copy());
            }
            return;
        }
        if (slot instanceof DisabledSlot) {
            return;
        }
        super.clicked(id, dragType, clickType, player);
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        if (slot instanceof FilterSlot || slot instanceof DisabledSlot) {
            return false;
        }
        return super.canTakeItemForPickAll(stack, slot);
    }

    private boolean isSwappingDisabledSlotWithNumberKeys(final int dragType, final ClickType clickType) {
        return disabledSlot != null
            && clickType == ClickType.SWAP
            && disabledSlot.isDisabledSlot(dragType);
    }

    public void handleFilterSlotChange(final int slotIndex, final ItemStack stack) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return;
        }
        if (slots.get(slotIndex) instanceof FilterSlot filterSlot && filterSlot.mayPlace(stack)) {
            filterSlot.set(stack);
        }
    }
}
