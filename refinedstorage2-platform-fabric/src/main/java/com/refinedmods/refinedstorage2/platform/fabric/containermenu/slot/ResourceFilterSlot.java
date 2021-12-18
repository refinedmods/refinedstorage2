package com.refinedmods.refinedstorage2.platform.fabric.containermenu.slot;

import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ResourceFilterSlot extends Slot {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ResourceFilterContainer resourceFilterContainer;
    private final int containerIndex;
    private Object cachedResource;

    public ResourceFilterSlot(ResourceFilterContainer resourceFilterContainer, int index, int x, int y) {
        super(createDummyContainer(), 0, x, y);
        this.resourceFilterContainer = resourceFilterContainer;
        this.containerIndex = index;
        this.cachedResource = resourceFilterContainer.getFilter(index);
    }

    private static SimpleContainer createDummyContainer() {
        return new SimpleContainer(1);
    }

    public <T> void change(ItemStack carried, ResourceType<T> type) {
        type.translate(carried).ifPresentOrElse(
                resource -> resourceFilterContainer.set(containerIndex, type, resource),
                () -> resourceFilterContainer.remove(containerIndex)
        );
    }

    public void broadcastChanges(Player player) {
        Object current = resourceFilterContainer.getFilter(containerIndex);
        if (!Objects.equals(current, cachedResource)) {
            LOGGER.info("Resource filter slot {} has changed", containerIndex);
            cachedResource = current;
            ServerPacketUtil.sendToPlayer((ServerPlayer) player, PacketIds.RESOURCE_FILTER_SLOT_UPDATE, buf -> {
                buf.writeInt(index);
                resourceFilterContainer.writeToUpdatePacket(containerIndex, buf);
            });
        }
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    public void render(PoseStack poseStack, int x, int y, int z) {
        ResourceType<Object> type = (ResourceType<Object>) resourceFilterContainer.getType(containerIndex);
        if (type == null) {
            return;
        }
        Object value = resourceFilterContainer.getFilter(containerIndex);
        type.render(poseStack, value, x, y, z);
    }

    public List<Component> getTooltipLines() {
        ResourceType<Object> type = (ResourceType<Object>) resourceFilterContainer.getType(containerIndex);
        if (type == null) {
            return Collections.emptyList();
        }
        Object value = resourceFilterContainer.getFilter(containerIndex);
        return type.getTooltipLines(value);
    }

    public void readFromUpdatePacket(FriendlyByteBuf buf) {
        resourceFilterContainer.readFromUpdatePacket(containerIndex, buf);
    }
}
