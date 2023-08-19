package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainerType;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
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
    @Nullable
    private ResourceAmountTemplate<?> cachedResource;

    public ResourceSlot(final ResourceContainer resourceContainer,
                        final int index,
                        final Component helpText,
                        final int x,
                        final int y) {
        this(resourceContainer, new SimpleContainer(resourceContainer.size()), index, helpText, x, y);
    }

    public ResourceSlot(final ResourceContainer resourceContainer,
                        final Container resourceContainerAsContainer,
                        final int index,
                        final Component helpText,
                        final int x,
                        final int y) {
        super(resourceContainerAsContainer, index, x, y);
        this.resourceContainer = resourceContainer;
        this.helpText = helpText;
        this.cachedResource = resourceContainer.get(index);
    }

    public ResourceSlot forAmountScreen(final int newX, final int newY) {
        return new ResourceSlot(resourceContainer, container, index, helpText, newX, newY) {
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
        return resourceContainer.getType() == ResourceContainerType.FILTER_WITH_AMOUNT
            || resourceContainer.getType() == ResourceContainerType.CONTAINER;
    }

    public boolean canModifyAmount() {
        return resourceContainer.getType() == ResourceContainerType.FILTER_WITH_AMOUNT;
    }

    public boolean supportsItemSlotInteractions() {
        return resourceContainer.getType() == ResourceContainerType.CONTAINER;
    }

    public boolean isDisabled() {
        return false;
    }

    @Nullable
    public ResourceAmountTemplate<?> getResourceAmount() {
        return resourceContainer.get(getContainerSlot());
    }

    public boolean isEmpty() {
        return getResourceAmount() == null;
    }

    public void change(final ItemStack stack, final boolean tryAlternatives) {
        resourceContainer.change(getContainerSlot(), stack, tryAlternatives);
    }

    public <T> void change(@Nullable final ResourceAmountTemplate<T> instance) {
        if (instance == null) {
            resourceContainer.remove(getContainerSlot());
        } else {
            resourceContainer.set(getContainerSlot(), instance);
        }
    }

    public boolean changeIfEmpty(final ItemStack stack) {
        if (!isEmpty()) {
            return false;
        }
        resourceContainer.change(getContainerSlot(), stack, false);
        return true;
    }

    public void changeAmount(final long amount) {
        if (resourceContainer.getType() != ResourceContainerType.FILTER_WITH_AMOUNT) {
            return;
        }
        resourceContainer.setAmount(getContainerSlot(), amount);
    }

    public void changeAmountOnClient(final double amount) {
        final ResourceAmountTemplate<?> resourceAmount = getResourceAmount();
        if (resourceAmount == null) {
            return;
        }
        final long normalizedAmount = resourceAmount.getStorageChannelType().normalizeAmount(amount);
        Platform.INSTANCE.getClientToServerCommunications().sendResourceSlotAmountChange(index, normalizedAmount);
    }

    public boolean contains(final ItemStack stack) {
        final ResourceAmountTemplate<?> resourceAmount = getResourceAmount();
        return resourceAmount != null && ItemStack.matches(stack, resourceAmount.getStackRepresentation());
    }

    public void broadcastChanges(final Player player) {
        final ResourceAmountTemplate<?> resourceAmount = getResourceAmount();
        if (!Objects.equals(resourceAmount, cachedResource)) {
            LOGGER.debug("Resource slot {} has changed", getContainerSlot());
            this.cachedResource = resourceAmount;
            broadcastChange((ServerPlayer) player, resourceAmount);
        }
    }

    private <T> void broadcastChange(final ServerPlayer player,
                                     @Nullable final ResourceAmountTemplate<T> contents) {
        Platform.INSTANCE.getServerToClientCommunications()
            .sendResourceSlotUpdate(player, contents, index);
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
        final ResourceAmountTemplate<?> resourceAmount = getResourceAmount();
        if (resourceAmount == null) {
            return 0;
        }
        return resourceAmount.getStorageChannelType().getDisplayAmount(resourceAmount.getAmount());
    }

    public double getMaxAmountWhenModifying() {
        final ResourceAmountTemplate<?> resourceAmount = getResourceAmount();
        if (resourceAmount == null) {
            return 0;
        }
        return resourceContainer.getMaxAmount(resourceAmount);
    }

    public Component getHelpText() {
        return helpText;
    }

    public ResourceFactory<?> getPrimaryResourceFactory() {
        return resourceContainer.getPrimaryResourceFactory();
    }

    public Set<ResourceFactory<?>> getAlternativeResourceFactories() {
        return resourceContainer.getAlternativeResourceFactories();
    }
}
