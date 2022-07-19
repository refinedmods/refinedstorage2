package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractResourceFilterContainerMenu extends AbstractBaseContainerMenu
    implements ResourceTypeAccessor {
    private final OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry;
    private final List<ResourceFilterSlot> resourceFilterSlots = new ArrayList<>();
    @Nullable
    private final Player player;
    @Nullable
    private ResourceType currentResourceType;

    protected AbstractResourceFilterContainerMenu(final MenuType<?> type,
                                                  final int syncId,
                                                  final OrderedRegistry<ResourceLocation, ResourceType> rtr,
                                                  final Player player,
                                                  final ResourceFilterContainer container) {
        super(type, syncId);
        this.resourceTypeRegistry = rtr;
        this.player = player;
        this.currentResourceType = container.determineDefaultType();
    }

    protected AbstractResourceFilterContainerMenu(final MenuType<?> type,
                                                  final int syncId,
                                                  final OrderedRegistry<ResourceLocation, ResourceType> rtr) {
        super(type, syncId);
        this.resourceTypeRegistry = rtr;
        this.player = null;
    }

    protected void initializeResourceFilterSlots(final FriendlyByteBuf buf) {
        final ResourceLocation type = buf.readResourceLocation();
        this.currentResourceType = resourceTypeRegistry.get(type).orElse(resourceTypeRegistry.getDefault());
        for (final ResourceFilterSlot resourceFilterSlot : resourceFilterSlots) {
            resourceFilterSlot.readFromUpdatePacket(buf);
        }
    }

    public void readResourceFilterSlotUpdate(final int slotIndex, final FriendlyByteBuf buf) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return;
        }
        if (slots.get(slotIndex) instanceof ResourceFilterSlot resourceFilterSlot) {
            resourceFilterSlot.readFromUpdatePacket(buf);
        }
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

    @Override
    public void clicked(final int id, final int dragType, final ClickType actionType, final Player actor) {
        final Slot slot = id >= 0 && id < slots.size() ? getSlot(id) : null;
        if (currentResourceType != null && slot instanceof ResourceFilterSlot resourceFilterSlot) {
            resourceFilterSlot.change(getCarried(), currentResourceType);
        } else {
            super.clicked(id, dragType, actionType, actor);
        }
    }

    @Override
    public Component getCurrentResourceTypeName() {
        return currentResourceType != null ? currentResourceType.getName() : Component.empty();
    }

    public void setCurrentResourceType(final ResourceLocation id) {
        this.currentResourceType = resourceTypeRegistry.get(id).orElse(resourceTypeRegistry.getDefault());
    }

    @Override
    public void toggleResourceType() {
        if (currentResourceType == null) {
            return;
        }
        this.currentResourceType = resourceTypeRegistry.next(currentResourceType);
        Platform.INSTANCE.getClientToServerCommunications().sendResourceTypeChange(this.currentResourceType);
    }

    @Override
    public boolean canTakeItemForPickAll(final ItemStack stack, final Slot slot) {
        return !(slot instanceof ResourceFilterSlot);
    }
}
