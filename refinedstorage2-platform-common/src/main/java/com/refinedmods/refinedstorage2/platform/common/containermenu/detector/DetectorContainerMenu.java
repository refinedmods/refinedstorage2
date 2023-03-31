package com.refinedmods.refinedstorage2.platform.common.containermenu.detector;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.detector.DetectorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceFilterSlot;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.ResourceFilterContainer;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class DetectorContainerMenu extends AbstractResourceFilterContainerMenu {
    private final Player player;

    private double amount;
    @Nullable
    private DetectorBlockEntity detector;

    public DetectorContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getDetector(), syncId, playerInventory.player);
        this.amount = buf.readDouble();
        this.player = playerInventory.player;
        addSlots(new ResourceFilterContainer(1));
        initializeResourceFilterSlots(buf);
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.DETECTOR_MODE, DetectorMode.EQUAL));
    }

    public DetectorContainerMenu(final int syncId,
                                 final Player player,
                                 final DetectorBlockEntity detector,
                                 final ResourceFilterContainer resourceFilterContainer) {
        super(Menus.INSTANCE.getDetector(), syncId, player);
        this.amount = detector.getAmount();
        this.detector = detector;
        this.player = player;
        addSlots(resourceFilterContainer);
        registerProperty(new ServerProperty<>(
            PropertyTypes.FUZZY_MODE,
            detector::isFuzzyMode,
            detector::setFuzzyMode
        ));
        registerProperty(new ServerProperty<>(
            PropertyTypes.DETECTOR_MODE,
            detector::getMode,
            detector::setMode
        ));
    }

    private void addSlots(final ResourceFilterContainer config) {
        addSlot(new ResourceFilterSlot(config, 0, 107, 20));
        addPlayerInventory(player.getInventory(), 8, 55);
        transferManager.addFilterTransfer(player.getInventory());
    }

    public double getAmount() {
        return amount;
    }

    public void changeAmountOnClient(final double newAmount) {
        Platform.INSTANCE.getClientToServerCommunications().sendDetectorAmountChange(newAmount);
        this.amount = newAmount;
    }

    public void changeAmountOnServer(final double newAmount) {
        if (detector == null) {
            return;
        }
        detector.setAmount(newAmount);
        this.amount = newAmount;
    }
}
