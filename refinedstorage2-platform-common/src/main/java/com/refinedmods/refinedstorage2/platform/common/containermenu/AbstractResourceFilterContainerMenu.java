package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractResourceFilterContainerMenu extends AbstractBaseContainerMenu {
    private final List<ResourceFilterSlot> resourceFilterSlots = new ArrayList<>();
    @Nullable
    private final Player player;

    protected AbstractResourceFilterContainerMenu(final MenuType<?> type, final int syncId, final Player player) {
        super(type, syncId);
        this.player = player;
    }

    protected AbstractResourceFilterContainerMenu(final MenuType<?> type, final int syncId) {
        super(type, syncId);
        this.player = null;
    }

    protected void initializeResourceFilterSlots(final FriendlyByteBuf buf) {
        for (final ResourceFilterSlot resourceFilterSlot : resourceFilterSlots) {
            resourceFilterSlot.readFromUpdatePacket(buf);
        }
    }

    private Optional<ResourceFilterSlot> getResourceFilterSlot(final int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return Optional.empty();
        }
        if (slots.get(slotIndex) instanceof ResourceFilterSlot resourceFilterSlot) {
            return Optional.of(resourceFilterSlot);
        }
        return Optional.empty();
    }

    public <T> void handleResourceFilterSlotUpdate(final int slotIndex,
                                                   @Nullable final FilteredResource<T> filteredResource) {
        getResourceFilterSlot(slotIndex).ifPresent(slot -> slot.change(filteredResource));
    }

    public void handleResourceFilterSlotChange(final int slotIndex, final boolean tryAlternatives) {
        getResourceFilterSlot(slotIndex).ifPresent(slot -> slot.change(getCarried(), tryAlternatives));
    }

    public void sendResourceFilterSlotChange(final int slotIndex, final boolean tryAlternatives) {
        Platform.INSTANCE.getClientToServerCommunications().sendResourceFilterSlotChange(slotIndex, tryAlternatives);
    }

    public void handleResourceFilterSlotAmountChange(final int slotIndex, final long amount) {
        getResourceFilterSlot(slotIndex).ifPresent(slot -> slot.changeAmount(amount));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (player == null) {
            return;
        }
        for (final ResourceFilterSlot resourceFilterSlot : resourceFilterSlots) {
            resourceFilterSlot.broadcastChanges(player);
        }
    }

    @Override
    protected Slot addSlot(final Slot slot) {
        if (slot instanceof ResourceFilterSlot resourceFilterSlot) {
            resourceFilterSlots.add(resourceFilterSlot);
        }
        return super.addSlot(slot);
    }

    @Override
    protected void resetSlots() {
        super.resetSlots();
        resourceFilterSlots.clear();
    }

    public void addToFilterIfNotExisting(final ItemStack stack) {
        for (final ResourceFilterSlot resourceFilterSlot : resourceFilterSlots) {
            if (resourceFilterSlot.contains(stack)) {
                return;
            }
        }
        for (final ResourceFilterSlot resourceFilterSlot : resourceFilterSlots) {
            if (resourceFilterSlot.changeIfEmpty(stack)) {
                return;
            }
        }
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        return !(slot instanceof ResourceFilterSlot);
    }
}
