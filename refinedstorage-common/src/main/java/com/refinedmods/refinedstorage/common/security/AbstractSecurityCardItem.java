package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.common.api.security.SecurityPolicyContainerItem;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

abstract class AbstractSecurityCardItem<T> extends Item implements SecurityPolicyContainerItem {
    protected AbstractSecurityCardItem(final Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display,
                                final Consumer<Component> builder, final TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
        getPolicy(stack).ifPresent(policy -> appendHoverText(builder, policy, getDirtyPermissions(stack)));
    }

    private void appendHoverText(final Consumer<Component> builder,
                                 final SecurityPolicy policy,
                                 final Set<PlatformPermission> dirtyPermissions) {
        final List<PlatformPermission> allPermissions = RefinedStorageApi.INSTANCE.getPermissionRegistry().getAll();
        allPermissions.forEach(permission -> {
            final boolean allowed = policy.isAllowed(permission);
            final boolean dirty = dirtyPermissions.contains(permission);
            final Style style = Style.EMPTY
                .withColor(allowed ? ChatFormatting.GREEN : ChatFormatting.RED)
                .withItalic(dirty);
            final Component permissionTooltip = Component.literal(allowed ? "✓ " : "❌ ")
                .append(permission.getName())
                .append(dirty ? " (*)" : "")
                .withStyle(style);
            builder.accept(permissionTooltip);
        });
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            doUse(hand, serverPlayer, stack);
        }
        return InteractionResult.CONSUME.heldItemTransformedTo(stack);
    }

    private void doUse(final InteractionHand hand, final ServerPlayer player, final ItemStack stack) {
        if (player.isCrouching()) {
            clearConfiguration(player, stack);
            return;
        }
        getPolicy(stack).ifPresent(policy -> {
            final Set<PlatformPermission> dirtyPermissions = getDirtyPermissions(stack);
            Platform.INSTANCE.getMenuOpener().openMenu(player, createMenuProvider(
                player.level().getServer(),
                RefinedStorageApi.INSTANCE.createPlayerInventorySlotReference(player, hand),
                policy,
                dirtyPermissions,
                stack
            ));
        });
    }

    private void clearConfiguration(final ServerPlayer player, final ItemStack stack) {
        stack.remove(DataComponents.INSTANCE.getSecurityCardPermissions());
        player.sendSystemMessage(createTranslation("item", "security_card.cleared_configuration"));
    }

    abstract AbstractSecurityCardExtendedMenuProvider<T> createMenuProvider(
        MinecraftServer server,
        PlayerSlotReference playerSlotReference,
        SecurityPolicy policy,
        Set<PlatformPermission> dirtyPermissions,
        ItemStack stack
    );

    @Override
    public Optional<SecurityPolicy> getPolicy(final ItemStack stack) {
        if (!isValid(stack)) {
            return Optional.empty();
        }
        final SecurityCardPermissions permissions = stack.get(DataComponents.INSTANCE.getSecurityCardPermissions());
        if (permissions == null) {
            return Optional.of(RefinedStorageApi.INSTANCE.createDefaultSecurityPolicy());
        }
        return Optional.of(createPolicy(permissions));
    }

    private SecurityPolicy createPolicy(final SecurityCardPermissions permissions) {
        final Set<Permission> allowedPermissions = new HashSet<>();
        for (final PlatformPermission permission : RefinedStorageApi.INSTANCE.getPermissionRegistry().getAll()) {
            final boolean dirty = permissions.isDirty(permission);
            final boolean didExplicitlyAllow = dirty && permissions.isAllowed(permission);
            final boolean isAllowedByDefault = !dirty && permission.isAllowedByDefault();
            if (didExplicitlyAllow || isAllowedByDefault) {
                allowedPermissions.add(permission);
            }
        }
        return new SecurityPolicy(allowedPermissions);
    }

    @Override
    public boolean isValid(final ItemStack stack) {
        return true;
    }

    Set<PlatformPermission> getDirtyPermissions(final ItemStack stack) {
        return stack.getOrDefault(DataComponents.INSTANCE.getSecurityCardPermissions(), SecurityCardPermissions.EMPTY)
            .permissions()
            .keySet();
    }

    void setPermission(final ItemStack stack, final PlatformPermission permission, final boolean allowed) {
        final SecurityCardPermissions permissions = stack.getOrDefault(
            DataComponents.INSTANCE.getSecurityCardPermissions(),
            SecurityCardPermissions.EMPTY
        );
        stack.set(
            DataComponents.INSTANCE.getSecurityCardPermissions(),
            permissions.withPermission(permission, allowed)
        );
    }

    void resetPermission(final ItemStack stack, final PlatformPermission permission) {
        final SecurityCardPermissions permissions = stack.getOrDefault(
            DataComponents.INSTANCE.getSecurityCardPermissions(),
            SecurityCardPermissions.EMPTY
        );
        stack.set(DataComponents.INSTANCE.getSecurityCardPermissions(), permissions.forgetPermission(permission));
    }
}
