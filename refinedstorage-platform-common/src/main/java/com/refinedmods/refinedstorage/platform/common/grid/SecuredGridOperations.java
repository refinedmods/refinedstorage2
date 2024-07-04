package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.platform.common.security.BuiltinPermission;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class SecuredGridOperations implements GridOperations {
    private static final MutableComponent CANNOT_EXTRACT_MESSAGE = createTranslation("misc", "no_permission.extract");
    private static final MutableComponent CANNOT_INSERT_MESSAGE = createTranslation("misc", "no_permission.insert");

    private final ServerPlayer player;
    private final PlatformSecurityNetworkComponent securityNetworkComponent;
    private final GridOperations delegate;

    public SecuredGridOperations(final ServerPlayer player,
                                 final PlatformSecurityNetworkComponent securityNetworkComponent,
                                 final GridOperations delegate) {
        this.player = player;
        this.securityNetworkComponent = securityNetworkComponent;
        this.delegate = delegate;
    }

    @Override
    public boolean extract(final ResourceKey resource,
                           final GridExtractMode extractMode,
                           final InsertableStorage destination) {
        if (!securityNetworkComponent.isAllowed(BuiltinPermission.EXTRACT, player)) {
            PlatformApi.INSTANCE.sendNoPermissionMessage(player, CANNOT_EXTRACT_MESSAGE);
            return false;
        }
        return delegate.extract(resource, extractMode, destination);
    }

    @Override
    public boolean insert(final ResourceKey resource, final GridInsertMode insertMode,
                          final ExtractableStorage source) {
        if (!securityNetworkComponent.isAllowed(BuiltinPermission.INSERT, player)) {
            PlatformApi.INSTANCE.sendNoPermissionMessage(player, CANNOT_INSERT_MESSAGE);
            return false;
        }
        return delegate.insert(resource, insertMode, source);
    }
}
