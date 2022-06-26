package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class ResourceFilterableContainerMenu extends BaseContainerMenu implements ResourceTypeAccessor {
    private final List<ResourceFilterSlot> resourceFilterSlots = new ArrayList<>();
    private final Player player;

    private ResourceType currentResourceType;

    protected ResourceFilterableContainerMenu(MenuType<?> type, int syncId, Player player, ResourceFilterContainer container) {
        super(type, syncId);
        this.player = player;
        this.currentResourceType = container.determineDefaultType();
    }

    protected ResourceFilterableContainerMenu(MenuType<?> type, int syncId) {
        super(type, syncId);
        this.player = null;
    }

    protected void initializeResourceFilterSlots(FriendlyByteBuf buf) {
        OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry = PlatformApi.INSTANCE.getResourceTypeRegistry();
        ResourceLocation type = buf.readResourceLocation();
        this.currentResourceType = resourceTypeRegistry.get(type).orElse(resourceTypeRegistry.getDefault());
        for (ResourceFilterSlot resourceFilterSlot : resourceFilterSlots) {
            resourceFilterSlot.readFromUpdatePacket(buf);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        for (ResourceFilterSlot resourceFilterSlot : resourceFilterSlots) {
            resourceFilterSlot.broadcastChanges(player);
        }
    }

    @Override
    protected Slot addSlot(Slot slot) {
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
    public void clicked(int id, int dragType, ClickType actionType, Player player) {
        Slot slot = id >= 0 ? getSlot(id) : null;

        if (slot instanceof ResourceFilterSlot resourceFilterSlot) {
            resourceFilterSlot.change(getCarried(), currentResourceType);
        } else {
            super.clicked(id, dragType, actionType, player);
        }
    }

    public void readResourceFilterSlotUpdate(int slotIndex, FriendlyByteBuf buf) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return;
        }
        if (slots.get(slotIndex) instanceof ResourceFilterSlot resourceFilterSlot) {
            resourceFilterSlot.readFromUpdatePacket(buf);
        }
    }

    @Override
    public Component getCurrentResourceTypeName() {
        return currentResourceType.getName();
    }

    public void setCurrentResourceType(ResourceLocation id) {
        OrderedRegistry<ResourceLocation, ResourceType> resourceTypeRegistry = PlatformApi.INSTANCE.getResourceTypeRegistry();
        this.currentResourceType = resourceTypeRegistry.get(id).orElse(resourceTypeRegistry.getDefault());
    }

    @Override
    public void toggleResourceType() {
        this.currentResourceType = PlatformApi.INSTANCE.getResourceTypeRegistry().next(currentResourceType);
        Platform.INSTANCE.getClientToServerCommunications().sendResourceTypeChange(this.currentResourceType);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof ResourceFilterSlot);
    }
}
