package com.refinedmods.refinedstorage.common.support.containermenu;

import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

public abstract class AbstractSingleAmountContainerMenu extends AbstractResourceContainerMenu {
    private double clientAmount;

    private final Component filterHelpText;
    private final ResourceContainer resourceContainer;

    protected AbstractSingleAmountContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Inventory playerInventory,
                                                final SingleAmountData singleAmountData,
                                                final Component filterHelpText) {
        super(type, syncId);
        this.disabledSlot = singleAmountData.slotReference().orElse(null);
        this.clientAmount = singleAmountData.amount();
        this.filterHelpText = filterHelpText;
        this.resourceContainer = ResourceContainerImpl.createForFilter(singleAmountData.resourceContainerData());
        addSlots(playerInventory.player);
    }

    protected AbstractSingleAmountContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Player player,
                                                final ResourceContainer resourceContainer,
                                                final Component filterHelpText,
                                                @Nullable final PlayerSlotReference disabledPlayerSlotReference) {
        super(type, syncId, player);
        this.disabledSlot = disabledPlayerSlotReference;
        this.filterHelpText = filterHelpText;
        this.resourceContainer = resourceContainer;
        addSlots(player);
    }

    private void addSlots(final Player player) {
        addSlot(new ResourceSlot(resourceContainer, 0, filterHelpText, 116, 47, ResourceSlotType.FILTER));
        addPlayerInventory(player.getInventory(), 8, 106);
        transferManager.addFilterTransfer(player.getInventory());
    }

    public double getMinAmount() {
        final PlatformResourceKey resource = resourceContainer.getResource(0);
        if (resource == null) {
            return 1;
        }
        return resource.getResourceType().getDisplayAmount(1);
    }

    public double getAmount() {
        return clientAmount;
    }

    public void changeAmountOnClient(final double newAmount) {
        C2SPackets.sendSingleAmountChange(newAmount);
        this.clientAmount = newAmount;
    }

    public abstract void changeAmountOnServer(double newAmount);
}
