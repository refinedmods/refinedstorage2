package com.refinedmods.refinedstorage2.platform.fabric.containermenu;

import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.ClientPacketUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class ResourceFilterableContainerMenu extends BaseContainerMenu implements ResourceTypeAccessor {
    private final List<ResourceFilterSlot> resourceFilterSlots = new ArrayList<>();
    private final Player player;

    private ResourceType<?> currentResourceType;

    protected ResourceFilterableContainerMenu(@Nullable MenuType<?> type, int syncId, Player player, ResourceFilterContainer container) {
        super(type, syncId);
        this.player = player;
        this.currentResourceType = container.determineDefaultType();
    }

    protected ResourceFilterableContainerMenu(@Nullable MenuType<?> type, int syncId) {
        super(type, syncId);
        this.player = null;
    }

    protected void initializeResourceFilterSlots(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readResourceLocation();
        this.currentResourceType = Rs2PlatformApiFacade.INSTANCE.getResourceTypeRegistry().get(type);
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
    public ResourceType<?> getCurrentResourceType() {
        return currentResourceType;
    }

    public void setCurrentResourceType(ResourceLocation id) {
        this.currentResourceType = Rs2PlatformApiFacade.INSTANCE.getResourceTypeRegistry().get(id);
    }

    @Override
    public void toggleResourceType() {
        this.currentResourceType = Rs2PlatformApiFacade.INSTANCE.getResourceTypeRegistry().toggle(currentResourceType);
        ClientPacketUtil.sendToServer(PacketIds.RESOURCE_TYPE_CHANGE, buf -> buf.writeResourceLocation(currentResourceType.getId()));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof ResourceFilterSlot);
    }
}
