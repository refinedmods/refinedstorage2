package com.refinedmods.refinedstorage2.platform.common.support.containermenu;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSlot extends Slot {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceSlot.class);

    private final ResourceContainer resourceContainer;
    private final Component helpText;
    private final ResourceSlotType type;
    @Nullable
    private ResourceAmount cachedResource;

    public ResourceSlot(final ResourceContainer resourceContainer,
                        final int index,
                        final Component helpText,
                        final int x,
                        final int y,
                        final ResourceSlotType type) {
        this(resourceContainer, new SimpleContainer(resourceContainer.size()), index, helpText, x, y, type);
    }

    public ResourceSlot(final ResourceContainer resourceContainer,
                        final Container resourceContainerAsContainer,
                        final int index,
                        final Component helpText,
                        final int x,
                        final int y,
                        final ResourceSlotType type) {
        super(resourceContainerAsContainer, index, x, y);
        this.resourceContainer = resourceContainer;
        this.helpText = helpText;
        this.cachedResource = resourceContainer.get(index);
        this.type = type;
    }

    public ResourceSlot forAmountScreen(final int newX, final int newY) {
        return new ResourceSlot(resourceContainer, container, index, helpText, newX, newY, type) {
            @Override
            public boolean canModifyAmount() {
                return false;
            }

            @Override
            public boolean shouldRenderAmount() {
                return false;
            }

            @Override
            public boolean isDisabled() {
                return true;
            }
        };
    }

    public boolean shouldRenderAmount() {
        return type == ResourceSlotType.FILTER_WITH_AMOUNT
            || type == ResourceSlotType.CONTAINER;
    }

    public boolean isFilter() {
        return type == ResourceSlotType.FILTER
            || type == ResourceSlotType.FILTER_WITH_AMOUNT;
    }

    public boolean canModifyAmount() {
        return type == ResourceSlotType.FILTER_WITH_AMOUNT;
    }

    public boolean supportsItemSlotInteractions() {
        return type == ResourceSlotType.CONTAINER;
    }

    public boolean isDisabled() {
        return false;
    }

    @Nullable
    public PlatformResourceKey getResource() {
        return resourceContainer.getResource(getContainerSlot());
    }

    public long getAmount() {
        return resourceContainer.getAmount(getContainerSlot());
    }

    private ItemStack getStackRepresentation() {
        return resourceContainer.getStackRepresentation(getContainerSlot());
    }

    public boolean isEmpty() {
        return resourceContainer.isEmpty(getContainerSlot());
    }

    public void change(final ItemStack stack, final boolean tryAlternatives) {
        resourceContainer.change(getContainerSlot(), stack, tryAlternatives);
    }

    public void change(@Nullable final ResourceAmount instance) {
        if (instance == null) {
            resourceContainer.remove(getContainerSlot());
        } else {
            resourceContainer.set(getContainerSlot(), instance);
        }
    }

    public void setFilter(final PlatformResourceKey resource) {
        if (!isFilter() || !isValid(resource)) {
            return;
        }
        resourceContainer.set(getContainerSlot(), new ResourceAmount(
            resource,
            resource.getResourceType().normalizeAmount(1D)
        ));
    }

    public boolean isValid(final ResourceKey resource) {
        return resourceContainer.isValid(resource);
    }

    public boolean changeIfEmpty(final ItemStack stack) {
        if (!isEmpty()) {
            return false;
        }
        resourceContainer.change(getContainerSlot(), stack, false);
        return true;
    }

    public void changeAmount(final long amount) {
        if (type != ResourceSlotType.FILTER_WITH_AMOUNT) {
            return;
        }
        resourceContainer.setAmount(getContainerSlot(), amount);
    }

    public void changeAmountOnClient(final double amount) {
        final PlatformResourceKey resource = getResource();
        if (resource == null) {
            return;
        }
        final long normalizedAmount = resource.getResourceType().normalizeAmount(amount);
        Platform.INSTANCE.getClientToServerCommunications().sendResourceSlotAmountChange(index, normalizedAmount);
    }

    public boolean contains(final ItemStack stack) {
        return ItemStack.matches(stack, getStackRepresentation());
    }

    public void broadcastChanges(final Player player) {
        final ResourceAmount currentResourceAmount = resourceContainer.get(getContainerSlot());
        if (!Objects.equals(currentResourceAmount, cachedResource)) {
            LOGGER.debug("Resource slot {} has changed", getContainerSlot());
            this.cachedResource = currentResourceAmount;
            broadcastChange((ServerPlayer) player, currentResourceAmount);
        }
    }

    private void broadcastChange(final ServerPlayer player, @Nullable final ResourceAmount contents) {
        Platform.INSTANCE.getServerToClientCommunications().sendResourceSlotUpdate(player, contents, index);
    }

    public void readFromUpdatePacket(final FriendlyByteBuf buf) {
        resourceContainer.readFromUpdatePacket(getContainerSlot(), buf);
    }

    @Override
    public boolean mayPickup(final Player player) {
        return supportsItemSlotInteractions();
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return supportsItemSlotInteractions() && container.canPlaceItem(getContainerSlot(), stack);
    }

    public double getDisplayAmount() {
        final PlatformResourceKey resource = getResource();
        if (resource == null) {
            return 0;
        }
        return resource.getResourceType().getDisplayAmount(getAmount());
    }

    public double getMaxAmountWhenModifying() {
        final ResourceKey resource = getResource();
        if (resource == null) {
            return 0;
        }
        return resourceContainer.getMaxAmount(resource);
    }

    public Component getHelpText() {
        return helpText;
    }

    public ResourceFactory getPrimaryResourceFactory() {
        return resourceContainer.getPrimaryResourceFactory();
    }

    public Set<ResourceFactory> getAlternativeResourceFactories() {
        return resourceContainer.getAlternativeResourceFactories();
    }
}
