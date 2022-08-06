package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

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
    @Nullable
    private FilteredResource cachedResource;

    public ResourceFilterSlot(final ResourceFilterContainer resourceFilterContainer,
                              final int index,
                              final int x,
                              final int y) {
        super(createDummyContainer(), 0, x, y);
        this.resourceFilterContainer = resourceFilterContainer;
        this.containerIndex = index;
        this.cachedResource = resourceFilterContainer.get(index);
    }

    private static SimpleContainer createDummyContainer() {
        return new SimpleContainer(1);
    }

    public void change(final ItemStack stack, final ResourceType type) {
        type.translate(stack).ifPresentOrElse(
            resource -> resourceFilterContainer.set(containerIndex, resource),
            () -> resourceFilterContainer.remove(containerIndex)
        );
    }

    public boolean changeIfEmpty(final ItemStack stack, final ResourceType type) {
        final FilteredResource existing = resourceFilterContainer.get(containerIndex);
        if (existing != null) {
            return false;
        }
        change(stack, type);
        return true;
    }

    public boolean contains(final ItemStack stack, final ResourceType type) {
        final Optional<FilteredResource> translated = type.translate(stack);
        final Optional<FilteredResource> current = Optional.ofNullable(resourceFilterContainer.get(containerIndex));
        return translated.equals(current);
    }

    public void broadcastChanges(final Player player) {
        final FilteredResource currentResource = resourceFilterContainer.get(containerIndex);
        if (!Objects.equals(currentResource, cachedResource)) {
            LOGGER.info("Resource filter slot {} has changed", containerIndex);
            this.cachedResource = currentResource;
            Platform.INSTANCE.getServerToClientCommunications().sendResourceFilterSlotUpdate(
                (ServerPlayer) player,
                resourceFilterContainer,
                index,
                containerIndex
            );
        }
    }

    public void readFromUpdatePacket(final FriendlyByteBuf buf) {
        resourceFilterContainer.readFromUpdatePacket(containerIndex, buf);
    }

    @Override
    public boolean mayPickup(final Player player) {
        return false;
    }

    public void render(final PoseStack poseStack, final int x, final int y, final int z) {
        final FilteredResource filteredResource = resourceFilterContainer.get(containerIndex);
        if (filteredResource == null) {
            return;
        }
        filteredResource.render(poseStack, x, y, z);
    }

    public List<Component> getTooltipLines(@Nullable final Player player) {
        final FilteredResource filteredResource = resourceFilterContainer.get(containerIndex);
        if (filteredResource == null) {
            return Collections.emptyList();
        }
        return filteredResource.getTooltipLines(player);
    }
}
