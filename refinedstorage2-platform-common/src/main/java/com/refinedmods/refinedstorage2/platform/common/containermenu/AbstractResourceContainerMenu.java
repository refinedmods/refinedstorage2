package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractResourceContainerMenu extends AbstractBaseContainerMenu {
    private final List<ResourceSlot> resourceSlots = new ArrayList<>();
    @Nullable
    private final Player player;

    protected AbstractResourceContainerMenu(final MenuType<?> type, final int syncId, final Player player) {
        super(type, syncId);
        this.player = player;
    }

    protected AbstractResourceContainerMenu(final MenuType<?> type, final int syncId) {
        super(type, syncId);
        this.player = null;
    }

    protected void initializeResourceSlots(final FriendlyByteBuf buf) {
        for (final ResourceSlot resourceSlot : resourceSlots) {
            resourceSlot.readFromUpdatePacket(buf);
        }
    }

    private Optional<ResourceSlot> getResourceSlot(final int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return Optional.empty();
        }
        if (slots.get(slotIndex) instanceof ResourceSlot resourceSlot) {
            return Optional.of(resourceSlot);
        }
        return Optional.empty();
    }

    public <T> void handleResourceSlotUpdate(final int slotIndex,
                                             @Nullable final ResourceAmountTemplate<T> resourceAmount) {
        getResourceSlot(slotIndex).ifPresent(slot -> slot.change(resourceAmount));
    }


    public <T> void handleResourceFilterSlotUpdate(final int slotIndex,
                                                   final PlatformStorageChannelType<T> storageChannelType,
                                                   final T resource) {
        getResourceSlot(slotIndex).ifPresent(slot -> slot.change(new ResourceAmountTemplate<>(
            resource,
            1,
            storageChannelType
        )));
    }

    public void handleResourceSlotChange(final int slotIndex, final boolean tryAlternatives) {
        getResourceSlot(slotIndex).ifPresent(slot -> slot.change(getCarried(), tryAlternatives));
    }

    public void sendResourceSlotChange(final int slotIndex, final boolean tryAlternatives) {
        Platform.INSTANCE.getClientToServerCommunications().sendResourceSlotChange(slotIndex, tryAlternatives);
    }

    public void handleResourceSlotAmountChange(final int slotIndex, final long amount) {
        getResourceSlot(slotIndex).ifPresent(slot -> slot.changeAmount(amount));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (player == null) {
            return;
        }
        for (final ResourceSlot resourceSlot : resourceSlots) {
            resourceSlot.broadcastChanges(player);
        }
    }

    public List<ResourceSlot> getResourceSlots() {
        return resourceSlots;
    }

    @Override
    protected Slot addSlot(final Slot slot) {
        if (slot instanceof ResourceSlot resourceSlot) {
            resourceSlots.add(resourceSlot);
        }
        return super.addSlot(slot);
    }

    @Override
    protected void resetSlots() {
        super.resetSlots();
        resourceSlots.clear();
    }

    public void addToResourceSlotIfNotExisting(final ItemStack stack) {
        for (final ResourceSlot resourceSlot : resourceSlots) {
            if (resourceSlot.contains(stack)) {
                return;
            }
        }
        for (final ResourceSlot resourceSlot : resourceSlots) {
            if (resourceSlot.changeIfEmpty(stack)) {
                return;
            }
        }
    }

    protected final boolean areAllResourceSlotsEmpty() {
        for (final ResourceSlot resourceSlot : resourceSlots) {
            if (!resourceSlot.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        if (slot instanceof ResourceSlot resourceSlot) {
            return resourceSlot.supportsItemSlotInteractions();
        }
        return super.canTakeItemForPickAll(stack, slot);
    }
}
