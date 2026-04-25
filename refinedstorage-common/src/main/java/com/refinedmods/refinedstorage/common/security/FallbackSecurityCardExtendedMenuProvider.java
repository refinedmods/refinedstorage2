package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.common.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.content.ContentNames;

import java.util.Set;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

class FallbackSecurityCardExtendedMenuProvider extends AbstractSecurityCardExtendedMenuProvider<SecurityCardData> {
    @Nullable
    private final Component name;
    private final PlayerSlotReference playerSlotReference;

    FallbackSecurityCardExtendedMenuProvider(
        @Nullable final Component name,
        final PlayerSlotReference playerSlotReference,
        final SecurityPolicy securityPolicy,
        final Set<PlatformPermission> dirtyPermissions
    ) {
        super(securityPolicy, dirtyPermissions);
        this.name = name;
        this.playerSlotReference = playerSlotReference;
    }

    @Override
    public SecurityCardData getMenuData() {
        return new SecurityCardData(playerSlotReference, getDataPermissions());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, SecurityCardData> getMenuCodec() {
        return SecurityCardData.STREAM_CODEC;
    }

    @Override
    public Component getDisplayName() {
        return name == null ? ContentNames.FALLBACK_SECURITY_CARD : name;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new FallbackSecurityCardContainerMenu(syncId, inventory, playerSlotReference);
    }
}
