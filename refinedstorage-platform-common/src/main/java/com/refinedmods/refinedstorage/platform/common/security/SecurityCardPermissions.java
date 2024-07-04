package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SecurityCardPermissions(Map<PlatformPermission, Boolean> permissions) {
    public static final SecurityCardPermissions EMPTY = new SecurityCardPermissions(Collections.emptyMap());

    public static final Codec<SecurityCardPermissions> CODEC = Codec.unboundedMap(
        PlatformApi.INSTANCE.getPermissionRegistry().codec(),
        Codec.BOOL
    ).xmap(SecurityCardPermissions::new, SecurityCardPermissions::permissions);

    public static final StreamCodec<RegistryFriendlyByteBuf, SecurityCardPermissions> STREAM_CODEC = StreamCodec
        .composite(
            ByteBufCodecs.map(
                HashMap::new,
                PlatformApi.INSTANCE.getPermissionRegistry().streamCodec(),
                ByteBufCodecs.BOOL
            ), SecurityCardPermissions::permissions,
            SecurityCardPermissions::new
        );

    boolean isDirty(final PlatformPermission permission) {
        return permissions.containsKey(permission);
    }

    boolean isAllowed(final PlatformPermission permission) {
        return permissions.getOrDefault(permission, false);
    }

    SecurityCardPermissions withPermission(final PlatformPermission permission, final boolean allowed) {
        final Map<PlatformPermission, Boolean> newPermissions = new HashMap<>(permissions);
        newPermissions.put(permission, allowed);
        return new SecurityCardPermissions(newPermissions);
    }

    SecurityCardPermissions forgetPermission(final PlatformPermission permission) {
        final Map<PlatformPermission, Boolean> newPermissions = new HashMap<>(permissions);
        newPermissions.remove(permission);
        return new SecurityCardPermissions(newPermissions);
    }
}
