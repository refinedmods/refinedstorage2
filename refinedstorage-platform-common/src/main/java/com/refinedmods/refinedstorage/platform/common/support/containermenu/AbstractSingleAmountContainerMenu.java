package com.refinedmods.refinedstorage.platform.common.support.containermenu;

import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerImpl;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public abstract class AbstractSingleAmountContainerMenu extends AbstractResourceContainerMenu {
    private double clientAmount;

    private final Component filterHelpText;

    protected AbstractSingleAmountContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Inventory playerInventory,
                                                final SingleAmountData singleAmountData,
                                                final Component filterHelpText) {
        super(type, syncId);
        this.disabledSlot = singleAmountData.slotReference().orElse(null);
        this.clientAmount = singleAmountData.amount();
        this.filterHelpText = filterHelpText;
        addSlots(
            playerInventory.player,
            ResourceContainerImpl.createForFilter(singleAmountData.resourceContainerData())
        );
    }

    protected AbstractSingleAmountContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Player player,
                                                final ResourceContainer resourceContainer,
                                                final Component filterHelpText,
                                                @Nullable final SlotReference disabledSlotReference) {
        super(type, syncId, player);
        this.disabledSlot = disabledSlotReference;
        this.filterHelpText = filterHelpText;
        addSlots(player, resourceContainer);
    }

    private void addSlots(final Player player,
                          final ResourceContainer resourceContainer) {
        addSlot(new ResourceSlot(resourceContainer, 0, filterHelpText, 116, 47, ResourceSlotType.FILTER));
        addPlayerInventory(player.getInventory(), 8, 106);
        transferManager.addFilterTransfer(player.getInventory());
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
