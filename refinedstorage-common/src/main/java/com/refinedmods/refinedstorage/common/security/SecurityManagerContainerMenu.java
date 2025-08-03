package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.ValidatedSlot;

import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class SecurityManagerContainerMenu extends AbstractBaseContainerMenu {
    private final Predicate<Player> stillValid;

    @Nullable
    private Slot fallbackSecurityCardSlot;

    public SecurityManagerContainerMenu(final int syncId, final Inventory playerInventory) {
        super(Menus.INSTANCE.getSecurityManager(), syncId);
        addSlots(
            playerInventory,
            new FilteredContainer(SecurityManagerBlockEntity.CARD_AMOUNT,
                SecurityManagerBlockEntity::isValidSecurityCard),
            new FilteredContainer(1, SecurityManagerBlockEntity::isValidFallbackSecurityCard)
        );
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.stillValid = player -> true;
    }

    SecurityManagerContainerMenu(final int syncId,
                                 final Inventory playerInventory,
                                 final SecurityManagerBlockEntity securityManager) {
        super(Menus.INSTANCE.getSecurityManager(), syncId);
        addSlots(playerInventory, securityManager.getSecurityCards(), securityManager.getFallbackSecurityCard());
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            securityManager::getRedstoneMode,
            securityManager::setRedstoneMode
        ));
        this.stillValid = player -> Container.stillValidBlockEntity(securityManager, player);
    }

    private void addSlots(final Inventory playerInventory,
                          final FilteredContainer securityCards,
                          final FilteredContainer fallbackSecurityCard) {
        for (int i = 0; i < SecurityManagerBlockEntity.CARD_AMOUNT; ++i) {
            final int column = i % 9;
            final int x = 8 + (column * 18);
            final int row = i / 9;
            final int y = 20 + (row * 18);
            addSlot(new ValidatedSlot(securityCards, i, x, y, SecurityManagerBlockEntity::isValidSecurityCard));
        }
        fallbackSecurityCardSlot = new ValidatedSlot(
            fallbackSecurityCard,
            0,
            174,
            20,
            SecurityManagerBlockEntity::isValidFallbackSecurityCard
        );
        addSlot(fallbackSecurityCardSlot);
        addPlayerInventory(playerInventory, 8, 72);
        transferManager.addBiTransfer(playerInventory, securityCards);
        transferManager.addBiTransfer(playerInventory, fallbackSecurityCard);
    }

    @Nullable
    Slot getFallbackSecurityCardSlot() {
        return fallbackSecurityCardSlot;
    }

    @Override
    public boolean stillValid(final Player player) {
        return stillValid.test(player);
    }
}
