package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainerImpl;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
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
                                                final FriendlyByteBuf buf,
                                                final Component filterHelpText) {
        super(type, syncId);
        if (buf.readBoolean()) {
            disabledPlayerInventorySlot = PlayerSlotReference.of(buf);
        }
        this.clientAmount = buf.readDouble();
        this.filterHelpText = filterHelpText;
        addSlots(playerInventory.player, ResourceContainerImpl.createForFilter(1));
        initializeResourceSlots(buf);
    }

    protected AbstractSingleAmountContainerMenu(final MenuType<?> type,
                                                final int syncId,
                                                final Player player,
                                                final ResourceContainer resourceContainer,
                                                final Component filterHelpText,
                                                @Nullable final PlayerSlotReference disabledSlotReference) {
        super(type, syncId, player);
        this.disabledPlayerInventorySlot = disabledSlotReference;
        this.filterHelpText = filterHelpText;
        addSlots(player, resourceContainer);
    }

    private void addSlots(final Player player, final ResourceContainer resourceContainer) {
        addSlot(new ResourceSlot(resourceContainer, 0, filterHelpText, 116, 47));
        addPlayerInventory(player.getInventory(), 8, 106);
        transferManager.addFilterTransfer(player.getInventory());
    }

    public double getAmount() {
        return clientAmount;
    }

    public void changeAmountOnClient(final double newAmount) {
        Platform.INSTANCE.getClientToServerCommunications().sendSingleAmountChange(newAmount);
        this.clientAmount = newAmount;
    }

    public abstract void changeAmountOnServer(double newAmount);

    public static void writeToBuf(final FriendlyByteBuf buf,
                                  final double amount,
                                  final ResourceContainer container,
                                  @Nullable final PlayerSlotReference disabledSlotReference) {
        if (disabledSlotReference != null) {
            buf.writeBoolean(true);
            disabledSlotReference.writeToBuf(buf);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeDouble(amount);
        container.writeToUpdatePacket(buf);
    }
}
