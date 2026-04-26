package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.common.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSizeListener;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractSecurityCardContainerMenu extends AbstractBaseContainerMenu
    implements ScreenSizeListener {
    protected final Inventory playerInventory;
    private final List<SecurityCardData.Permission> permissions;

    protected AbstractSecurityCardContainerMenu(final MenuType<?> menuType,
                                                final int syncId,
                                                final Inventory playerInventory,
                                                final SecurityCardData securityCardData) {
        super(menuType, syncId);
        this.playerInventory = playerInventory;
        this.disabledSlot = securityCardData.playerSlotReference();
        this.permissions = securityCardData.permissions();
    }

    protected AbstractSecurityCardContainerMenu(final MenuType<?> menuType,
                                                final int syncId,
                                                final Inventory playerInventory,
                                                final PlayerSlotReference disabledSlot) {
        super(menuType, syncId);
        this.playerInventory = playerInventory;
        this.disabledSlot = disabledSlot;
        this.permissions = new ArrayList<>();
        resized(0, 0, 0);
    }

    List<SecurityCardData.Permission> getPermissions() {
        return permissions;
    }

    @Override
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        resetSlots();
        addPlayerInventory(playerInventory, 8, playerInventoryY);
    }

    public void setPermission(final PlatformPermission permission, final boolean allowed) {
        if (disabledSlot == null) {
            return;
        }
        final ItemStack stack = disabledSlot.get(playerInventory.player);
        setPermission(stack, permission, allowed);
    }

    private void setPermission(final ItemStack stack, final PlatformPermission permission, final boolean allowed) {
        if (stack.getItem() instanceof AbstractSecurityCardItem<?> securityCardItem) {
            securityCardItem.setPermission(stack, permission, allowed);
        }
    }

    public void resetPermissionServer(final PlatformPermission permission) {
        if (disabledSlot == null) {
            return;
        }
        final ItemStack stack = disabledSlot.get(playerInventory.player);
        resetPermissionServer(stack, permission);
    }

    private void resetPermissionServer(final ItemStack stack, final PlatformPermission permission) {
        if (stack.getItem() instanceof AbstractSecurityCardItem<?> securityCardItem) {
            securityCardItem.resetPermission(stack, permission);
        }
    }

    SecurityCardData.Permission resetPermission(final PlatformPermission permission) {
        final boolean allowed = permission.isAllowedByDefault();
        C2SPackets.sendSecurityCardResetPermission(permission);
        return updatePermissionLocally(permission, allowed, false);
    }

    SecurityCardData.Permission changePermission(final PlatformPermission permission, final boolean selected) {
        C2SPackets.sendSecurityCardPermission(permission, selected);
        return updatePermissionLocally(permission, selected, true);
    }

    private SecurityCardData.Permission updatePermissionLocally(final PlatformPermission permission,
                                                                final boolean allowed,
                                                                final boolean dirty) {
        final SecurityCardData.Permission
            localPermission = permissions.stream().filter(p -> p.permission() == permission)
            .findFirst()
            .orElseThrow();
        final int index = permissions.indexOf(localPermission);
        final SecurityCardData.Permission updatedLocalPermission = new SecurityCardData.Permission(
            localPermission.permission(),
            allowed,
            dirty
        );
        permissions.set(index, updatedLocalPermission);
        return updatedLocalPermission;
    }
}
