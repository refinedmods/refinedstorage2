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

    private final ResourceFilterContainer container;
    private final int containerIndex;
    private Object cachedResource;

    public ResourceFilterSlot(ResourceFilterContainer container, int index, int x, int y) {
        super(new SimpleContainer(1), 0, x, y);
        this.container = container;
        this.containerIndex = index;
        this.cachedResource = container.getFilter(index);
    }

    public <T> void change(ItemStack carried, ResourceType<T> type) {
        type.translate(carried).ifPresentOrElse(
                resource -> container.set(containerIndex, type, resource),
                () -> container.remove(containerIndex)
        );
    }

    public void broadcastChanges(Player player) {
        Object current = container.getFilter(containerIndex);
        if (!Objects.equals(current, cachedResource)) {
            LOGGER.info("Resource filter slot {} has changed", containerIndex);
            cachedResource = current;
            ServerPacketUtil.sendToPlayer((ServerPlayer) player, PacketIds.RESOURCE_FILTER_SLOT_UPDATE, buf -> {
                buf.writeInt(index);
                container.writeToUpdatePacket(containerIndex, buf);
            });
        }
    }

    @Override
    public boolean mayPickup(Player playerEntity) {
        return false;
    }

    public void render(PoseStack poseStack, int x, int y, int z) {
        ResourceType<Object> type = (ResourceType<Object>) container.getType(containerIndex);
        if (type == null) {
            return;
        }
        Object value = container.getFilter(containerIndex);
        type.render(poseStack, value, x, y, z);
    }

    public List<Component> getTooltipLines() {
        ResourceType<Object> type = (ResourceType<Object>) container.getType(containerIndex);
        if (type == null) {
            return Collections.emptyList();
        }
        Object value = container.getFilter(containerIndex);
        return type.getTooltipLines(value);
    }

    public void readFromUpdatePacket(FriendlyByteBuf buf) {
        container.readFromUpdatePacket(containerIndex, buf);
    }
}
