package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemFilteredResource;
import com.refinedmods.refinedstorage2.platform.common.screen.TextureIds;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationAsHeading;

public class ResourceFilterSlot extends Slot implements SlotTooltip {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceFilterSlot.class);

    private final ResourceFilterContainer resourceFilterContainer;
    private final int containerIndex;
    @Nullable
    private FilteredResource<?> cachedResource;

    public ResourceFilterSlot(final ResourceFilterContainer resourceFilterContainer,
                              final int index,
                              final int x,
                              final int y) {
        super(createDummyContainer(), 0, x, y);
        this.resourceFilterContainer = resourceFilterContainer;
        this.containerIndex = index;
        this.cachedResource = resourceFilterContainer.get(index);
    }

    public ResourceFilterSlot atPosition(final int newX, final int newY) {
        return new ResourceFilterSlot(resourceFilterContainer, index, newX, newY);
    }

    public boolean supportsAmount() {
        return resourceFilterContainer.supportsAmount();
    }

    @Nullable
    public FilteredResource<?> getFilteredResource() {
        return resourceFilterContainer.get(containerIndex);
    }

    public boolean isEmpty() {
        return getFilteredResource() == null;
    }

    private static SimpleContainer createDummyContainer() {
        return new SimpleContainer(1);
    }

    public void change(final ItemStack stack, final boolean tryAlternatives) {
        PlatformApi.INSTANCE.getFilteredResourceFactory().create(stack, tryAlternatives).ifPresentOrElse(
            translated -> resourceFilterContainer.set(containerIndex, translated),
            () -> resourceFilterContainer.remove(containerIndex)
        );
    }

    public <T> void change(@Nullable final FilteredResource<T> filteredResource) {
        if (filteredResource == null) {
            resourceFilterContainer.remove(containerIndex);
        } else {
            resourceFilterContainer.set(containerIndex, filteredResource);
        }
    }

    public boolean changeIfEmpty(final ItemStack stack) {
        final FilteredResource<?> existing = resourceFilterContainer.get(containerIndex);
        if (existing != null) {
            return false;
        }
        PlatformApi.INSTANCE.getFilteredResourceFactory().create(stack, false).ifPresent(this::change);
        return true;
    }

    public void changeAmount(final long amount) {
        resourceFilterContainer.setAmount(containerIndex, amount);
    }

    public void changeAmountOnClient(final long amount) {
        Platform.INSTANCE.getClientToServerCommunications().sendResourceFilterSlotAmountChange(index, amount);
    }

    public boolean contains(final ItemStack stack) {
        final Optional<FilteredResource<?>> converted = PlatformApi.INSTANCE.getFilteredResourceFactory().create(
            stack,
            false
        );
        final Optional<FilteredResource<?>> current = Optional.ofNullable(resourceFilterContainer.get(containerIndex));
        return converted.equals(current);
    }

    public void broadcastChanges(final Player player) {
        final FilteredResource<?> currentResource = resourceFilterContainer.get(containerIndex);
        if (!Objects.equals(currentResource, cachedResource)) {
            LOGGER.debug("Resource filter slot {} has changed", containerIndex);
            this.cachedResource = currentResource;
            broadcastChange((ServerPlayer) player, currentResource);
        }
    }

    private <T> void broadcastChange(final ServerPlayer player, @Nullable final FilteredResource<T> filteredResource) {
        Platform.INSTANCE.getServerToClientCommunications().sendResourceFilterSlotUpdate(
            player,
            filteredResource == null ? null : filteredResource.getStorageChannelType(),
            filteredResource == null ? null : filteredResource.getValue(),
            filteredResource == null ? 0 : filteredResource.getAmount(),
            index
        );
    }

    public void readFromUpdatePacket(final FriendlyByteBuf buf) {
        resourceFilterContainer.readFromUpdatePacket(containerIndex, buf);
    }

    @Override
    public boolean mayPickup(final Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return false;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(final ItemStack carried) {
        final FilteredResource<?> filteredResource = resourceFilterContainer.get(containerIndex);
        if (filteredResource == null) {
            return getTooltipForEmptySlot(carried);
        }
        final List<ClientTooltipComponent> tooltip = filteredResource.getTooltip()
            .stream()
            .map(Component::getVisualOrderText)
            .map(ClientTooltipComponent::create)
            .collect(Collectors.toList());
        tooltip.add(ClientTooltipComponent.create(
            createTranslationAsHeading("gui", "filter_slot.click_to_clear").getVisualOrderText()
        ));
        return tooltip;
    }

    private List<ClientTooltipComponent> getTooltipForEmptySlot(final ItemStack carried) {
        final List<ClientTooltipComponent> tooltip = new ArrayList<>();
        tooltip.add(ClientTooltipComponent.create(
            createTranslationAsHeading("gui", "filter_slot.empty_filter").getVisualOrderText()
        ));
        if (!carried.isEmpty()) {
            addCarriedHints(carried, tooltip);
        }
        return tooltip;
    }

    private void addCarriedHints(final ItemStack carried, final List<ClientTooltipComponent> tooltip) {
        PlatformApi.INSTANCE.getFilteredResourceFactory().create(carried, false)
            .ifPresent(asItem -> tooltip.add(new FilteredResourceTooltip(true, asItem)));
        addCarriedHintsForAlternatives(carried, tooltip);
    }

    private void addCarriedHintsForAlternatives(final ItemStack carried, final List<ClientTooltipComponent> tooltip) {
        PlatformApi.INSTANCE.getFilteredResourceFactory().create(carried, true).ifPresent(asAlternative -> {
            if (asAlternative instanceof ItemFilteredResource) {
                return;
            }
            tooltip.add(new FilteredResourceTooltip(false, asAlternative));
        });
    }

    private static class FilteredResourceTooltip implements ClientTooltipComponent {
        private static final int MOUSE_ICON_WIDTH = 9;
        private static final int MOUSE_ICON_HEIGHT = 16;

        private final boolean left;
        private final FilteredResource<?> filteredResource;
        private final Component name;

        private FilteredResourceTooltip(final boolean left, final FilteredResource<?> filteredResource) {
            this.left = left;
            this.filteredResource = filteredResource;
            this.name = filteredResource.getDisplayName();
        }

        @Override
        public int getHeight() {
            return 18;
        }

        @Override
        public int getWidth(final Font font) {
            return MOUSE_ICON_WIDTH + 4 + 16 + 4 + font.width(name);
        }

        @Override
        public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
            final int u = left ? 238 : 247;
            final int v = 178;
            graphics.blit(TextureIds.ICONS, x, y, u, v, MOUSE_ICON_WIDTH, MOUSE_ICON_HEIGHT);
            filteredResource.render(graphics, x + MOUSE_ICON_WIDTH + 4, y);
            graphics.drawString(
                font,
                name,
                x + MOUSE_ICON_WIDTH + 4 + 16 + 4,
                y + 4,
                Objects.requireNonNullElse(ChatFormatting.WHITE.getColor(), 15)
            );
        }
    }
}
