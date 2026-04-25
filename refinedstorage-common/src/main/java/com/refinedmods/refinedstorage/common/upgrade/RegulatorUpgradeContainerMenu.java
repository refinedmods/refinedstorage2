package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractSingleAmountContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.SingleAmountData;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

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
                                  final PlayerSlotReference playerSlotReference) {
        super(
            Menus.INSTANCE.getRegulatorUpgrade(),
            syncId,
            player,
            resourceContainer,
            FILTER_HELP,
            playerSlotReference
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

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }
}
