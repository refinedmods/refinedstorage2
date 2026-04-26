package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SecurityCardData(PlayerSlotReference playerSlotReference, List<Permission> permissions) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SecurityCardData> STREAM_CODEC = StreamCodec.composite(
        PlayerSlotReference.STREAM_CODEC, SecurityCardData::playerSlotReference,
        ByteBufCodecs.collection(
            ArrayList::new,
            StreamCodec.composite(
                RefinedStorageApi.INSTANCE.getPermissionRegistry().streamCodec(), Permission::permission,
                ByteBufCodecs.BOOL, Permission::allowed,
                ByteBufCodecs.BOOL, Permission::dirty,
                Permission::new
            )
        ), SecurityCardData::permissions,
        SecurityCardData::new
    );

    record Permission(PlatformPermission permission, boolean allowed, boolean dirty) {
    }
}
