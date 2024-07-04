package com.refinedmods.refinedstorage.platform.common.upgrade;

import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.platform.common.content.Menus;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.AbstractSingleAmountContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.SingleAmountData;

import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class RegulatorUpgradeContainerMenu extends AbstractSingleAmountContainerMenu {
    private static final Component FILTER_HELP = createTranslation("gui", "regulator_upgrade.filter_help");

    @Nullable
    private Consumer<Double> amountAcceptor;

    public RegulatorUpgradeContainerMenu(final int syncId,
                                         final Inventory playerInventory,
                                         final SingleAmountData singleAmountData) {
        super(Menus.INSTANCE.getRegulatorUpgrade(), syncId, playerInventory, singleAmountData, FILTER_HELP);
    }

    RegulatorUpgradeContainerMenu(final int syncId,
                                  final Player player,
                                  final ResourceContainer resourceContainer,
                                  final Consumer<Double> amountAcceptor,
                                  final SlotReference slotReference) {
        super(
            Menus.INSTANCE.getRegulatorUpgrade(),
            syncId,
            player,
            resourceContainer,
            FILTER_HELP,
            slotReference
        );
        this.amountAcceptor = amountAcceptor;
    }

    @Override
    public void changeAmountOnServer(final double newAmount) {
        if (amountAcceptor == null) {
            return;
        }
        amountAcceptor.accept(newAmount);
    }
}
