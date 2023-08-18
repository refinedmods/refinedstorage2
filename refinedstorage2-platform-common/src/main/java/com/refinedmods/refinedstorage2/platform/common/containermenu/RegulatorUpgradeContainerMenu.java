package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;

import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class RegulatorUpgradeContainerMenu extends AbstractSingleAmountContainerMenu {
    private static final Component FILTER_HELP = createTranslation("gui", "regulator_upgrade.filter_help");

    @Nullable
    private Consumer<Double> amountAcceptor;

    public RegulatorUpgradeContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getRegulatorUpgrade(), syncId, playerInventory, buf, FILTER_HELP);
    }

    public RegulatorUpgradeContainerMenu(final int syncId,
                                         final Player player,
                                         final ResourceContainer resourceContainer,
                                         final Consumer<Double> amountAcceptor,
                                         final PlayerSlotReference slotReference) {
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
