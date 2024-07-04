package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.content.Menus;

import net.minecraft.world.entity.player.Inventory;

public class FallbackSecurityCardContainerMenu extends AbstractSecurityCardContainerMenu {
    public FallbackSecurityCardContainerMenu(final int syncId,
                                             final Inventory playerInventory,
                                             final SecurityCardData securityCardData) {
        super(Menus.INSTANCE.getFallbackSecurityCard(), syncId, playerInventory, securityCardData);
    }

    FallbackSecurityCardContainerMenu(final int syncId,
                                      final Inventory playerInventory,
                                      final SlotReference disabledSlot) {
        super(Menus.INSTANCE.getFallbackSecurityCard(), syncId, playerInventory, disabledSlot);
    }
}
