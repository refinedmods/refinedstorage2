package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.api.security.SecurityPolicyContainerItem;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.DataComponents;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

abstract class AbstractSecurityCardItem<T> extends Item implements SecurityPolicyContainerItem {
    protected AbstractSecurityCardItem(final Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        getPolicy(stack).ifPresent(policy -> appendHoverText(lines, policy, getDirtyPermissions(stack)));
    }

    private void appendHoverText(final List<Component> lines,
                                 final SecurityPolicy policy,
                                 final Set<PlatformPermission> dirtyPermissions) {
        final List<PlatformPermission> allPermissions = PlatformApi.INSTANCE.getPermissionRegistry().getAll();
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
            lines.add(permissionTooltip);
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            doUse(hand, serverPlayer, stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    private void doUse(final InteractionHand hand, final ServerPlayer player, final ItemStack stack) {
        if (player.isCrouching()) {
            clearConfiguration(player, stack);
            return;
        }
        getPolicy(stack).ifPresent(policy -> {
            final Set<PlatformPermission> dirtyPermissions = getDirtyPermissions(stack);
            Platform.INSTANCE.getMenuOpener().openMenu(player, createMenuProvider(
                player.server,
                PlatformApi.INSTANCE.createInventorySlotReference(player, hand),
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
        SlotReference slotReference,
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
            return Optional.of(PlatformApi.INSTANCE.createDefaultSecurityPolicy());
        }
        return Optional.of(createPolicy(permissions));
    }

    private SecurityPolicy createPolicy(final SecurityCardPermissions permissions) {
        final Set<Permission> allowedPermissions = new HashSet<>();
        for (final PlatformPermission permission : PlatformApi.INSTANCE.getPermissionRegistry().getAll()) {
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
