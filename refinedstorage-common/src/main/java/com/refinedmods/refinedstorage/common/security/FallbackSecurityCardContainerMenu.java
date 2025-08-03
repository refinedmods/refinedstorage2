package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.content.Menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

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

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }
}
