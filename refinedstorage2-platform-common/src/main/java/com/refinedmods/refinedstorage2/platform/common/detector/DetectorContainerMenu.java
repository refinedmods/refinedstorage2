package com.refinedmods.refinedstorage2.platform.common.detector;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractSingleAmountContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ServerProperty;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DetectorContainerMenu extends AbstractSingleAmountContainerMenu {
    private static final Component FILTER_HELP = createTranslation("gui", "detector.filter_help");

    @Nullable
    private DetectorBlockEntity detector;

    public DetectorContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getDetector(), syncId, playerInventory, buf, FILTER_HELP);
        registerProperty(new ClientProperty<>(PropertyTypes.FUZZY_MODE, false));
        registerProperty(new ClientProperty<>(PropertyTypes.DETECTOR_MODE, DetectorMode.EQUAL));
    }

    public DetectorContainerMenu(final int syncId,
                                 final Player player,
                                 final DetectorBlockEntity detector,
                                 final ResourceContainer resourceContainer) {
        super(Menus.INSTANCE.getDetector(), syncId, player, resourceContainer, FILTER_HELP, null);
        this.detector = detector;
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

    @Override
    public void changeAmountOnServer(final double newAmount) {
        if (detector == null) {
            return;
        }
        detector.setAmount(newAmount);
    }
}
