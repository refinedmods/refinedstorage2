package com.refinedmods.refinedstorage.common.support.containermenu;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;

import java.util.Objects;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationAsHeading;

public class ResourceSlot extends Slot {
    private static final Component CLICK_TO_CONFIGURE_AMOUNT =
        createTranslationAsHeading("gui", "filter_slot.click_to_configure_amount");
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceSlot.class);

    protected final ResourceContainer resourceContainer;
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
        return new DisabledResourceSlot(resourceContainer, container, getContainerSlot(), helpText, newX, newY, type);
    }

    public boolean shouldRenderAmount() {
        return type == ResourceSlotType.FILTER_WITH_AMOUNT || type == ResourceSlotType.CONTAINER;
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

    public void change(@Nullable final ResourceAmount resourceAmount) {
        if (resourceAmount == null) {
            resourceContainer.remove(getContainerSlot());
        } else {
            resourceContainer.set(getContainerSlot(), resourceAmount);
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
        C2SPackets.sendResourceSlotAmountChange(index, normalizedAmount);
    }

    public boolean contains(final ItemStack stack) {
        return ItemStack.matches(stack, getStackRepresentation());
    }

    public boolean broadcastChanges(final Player player) {
        final ResourceAmount currentResourceAmount = resourceContainer.get(getContainerSlot());
        if (!Objects.equals(currentResourceAmount, cachedResource)) {
            LOGGER.debug("Resource slot {} has changed", getContainerSlot());
            this.cachedResource = currentResourceAmount;
            broadcastChange((ServerPlayer) player, currentResourceAmount);
            return true;
        }
        return false;
    }

    private void broadcastChange(final ServerPlayer player, @Nullable final ResourceAmount contents) {
        S2CPackets.sendResourceSlotUpdate(player, contents, index);
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
        if (!(resource instanceof PlatformResourceKey platformResource)) {
            return 0;
        }
        return platformResource.getResourceType().getDisplayAmount(resourceContainer.getMaxAmount(resource));
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

    @Nullable
    public ItemStack insertInto(final ItemStack carried) {
        final ResourceAmount currentResourceAmount = resourceContainer.get(getContainerSlot());
        if (currentResourceAmount == null) {
            return null;
        }
        return RefinedStorageApi.INSTANCE.getResourceContainerInsertStrategies().stream()
            .flatMap(strategy -> strategy.insert(carried, currentResourceAmount).stream())
            .findFirst()
            .map(result -> {
                resourceContainer.shrink(getContainerSlot(), result.inserted());
                return result.container();
            })
            .orElse(null);
    }

    public Component getClickToConfigureAmountHelpTooltip() {
        return CLICK_TO_CONFIGURE_AMOUNT;
    }
}
