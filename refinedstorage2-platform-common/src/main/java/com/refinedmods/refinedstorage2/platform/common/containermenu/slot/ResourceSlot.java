package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceInstance;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.MouseWithIconClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.SmallTextClientTooltipComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationAsHeading;

public class ResourceSlot extends Slot implements SlotTooltip {
    private static final SmallTextClientTooltipComponent CLICK_TO_CLEAR = new SmallTextClientTooltipComponent(
        createTranslationAsHeading("gui", "filter_slot.click_to_clear")
    );

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceSlot.class);

    private final ResourceContainer resourceContainer;
    private final Component helpText;
    @Nullable
    private ResourceInstance<?> cachedResource;

    public ResourceSlot(final ResourceContainer resourceContainer,
                        final int index,
                        final Component helpText,
                        final int x,
                        final int y) {
        super(resourceContainer, index, x, y);
        this.resourceContainer = resourceContainer;
        this.helpText = helpText;
        this.cachedResource = resourceContainer.get(index);
    }

    public ResourceSlot forAmountScreen(final int newX, final int newY) {
        return new ResourceSlot(resourceContainer, index, helpText, newX, newY) {
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
        return resourceContainer.supportsAmount();
    }

    public boolean canModifyAmount() {
        return resourceContainer.canModifyAmount();
    }

    public boolean supportsItemSlotInteractions() {
        return resourceContainer.supportsItemSlotInteractions();
    }

    public boolean isDisabled() {
        return false;
    }

    @Nullable
    public ResourceInstance<?> getContents() {
        return resourceContainer.get(getContainerSlot());
    }

    public long getMaxAmount() {
        final ResourceInstance<?> contents = getContents();
        if (contents == null) {
            return 0;
        }
        return resourceContainer.getMaxAmount(contents);
    }

    public boolean isEmpty() {
        return getContents() == null;
    }

    public void change(final ItemStack stack, final boolean tryAlternatives) {
        PlatformApi.INSTANCE.createResource(stack, tryAlternatives).ifPresentOrElse(
            translated -> resourceContainer.set(getContainerSlot(), translated),
            () -> resourceContainer.remove(getContainerSlot())
        );
    }

    public <T> void change(@Nullable final ResourceInstance<T> instance) {
        if (instance == null) {
            resourceContainer.remove(getContainerSlot());
        } else {
            resourceContainer.set(getContainerSlot(), instance);
        }
    }

    public boolean changeIfEmpty(final ItemStack stack) {
        final ResourceInstance<?> existing = getContents();
        if (existing != null) {
            return false;
        }
        PlatformApi.INSTANCE.createResource(stack, false).ifPresent(this::change);
        return true;
    }

    public void changeAmount(final long amount) {
        resourceContainer.setAmount(getContainerSlot(), amount);
    }

    public void changeAmountOnClient(final long amount) {
        Platform.INSTANCE.getClientToServerCommunications().sendResourceSlotAmountChange(index, amount);
    }

    public boolean contains(final ItemStack stack) {
        final ResourceInstance<?> instance = getContents();
        return instance != null && ItemStack.matches(stack, instance.getStackRepresentation());
    }

    public void broadcastChanges(final Player player) {
        final ResourceInstance<?> currentResource = getContents();
        if (!Objects.equals(currentResource, cachedResource)) {
            LOGGER.debug("Resource slot {} has changed", getContainerSlot());
            this.cachedResource = currentResource;
            broadcastChange((ServerPlayer) player, currentResource);
        }
    }

    private <T> void broadcastChange(final ServerPlayer player, @Nullable final ResourceInstance<T> resourceInstance) {
        Platform.INSTANCE.getServerToClientCommunications().sendResourceSlotUpdate(player, resourceInstance, index);
    }

    public void readFromUpdatePacket(final FriendlyByteBuf buf) {
        resourceContainer.readFromUpdatePacket(getContainerSlot(), buf);
    }

    @Override
    public boolean mayPickup(final Player player) {
        return resourceContainer.supportsItemSlotInteractions();
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return resourceContainer.supportsItemSlotInteractions();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(final ItemStack carried) {
        if (resourceContainer.supportsItemSlotInteractions()) {
            return Collections.emptyList();
        }
        final ResourceInstance<?> resourceInstance = getContents();
        if (resourceInstance == null) {
            return getTooltipForEmptySlot(carried);
        }
        return getClientTooltipComponents(resourceInstance);
    }

    private <T> List<ClientTooltipComponent> getClientTooltipComponents(final ResourceInstance<T> resourceInstance) {
        final List<ClientTooltipComponent> tooltip = PlatformApi.INSTANCE
            .getResourceRendering(resourceInstance.getResource())
            .getTooltip(resourceInstance.getResource())
            .stream()
            .map(Component::getVisualOrderText)
            .map(ClientTooltipComponent::create)
            .collect(Collectors.toList());
        if (!isDisabled()) {
            tooltip.add(CLICK_TO_CLEAR);
        }
        return tooltip;
    }

    private List<ClientTooltipComponent> getTooltipForEmptySlot(final ItemStack carried) {
        if (isDisabled()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> tooltip = new ArrayList<>();
        tooltip.add(ClientTooltipComponent.create(
            createTranslationAsHeading("gui", "filter_slot.empty_filter").getVisualOrderText()
        ));
        tooltip.addAll(MouseWithIconClientTooltipComponent.createForFilter(carried));
        tooltip.add(HelpClientTooltipComponent.create(helpText));
        return tooltip;
    }
}
