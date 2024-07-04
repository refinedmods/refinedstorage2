package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.api.network.security.SecurityActor;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.DataComponents;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNull;

public class SecurityCardItem extends AbstractSecurityCardItem<PlayerBoundSecurityCardData> {
    private static final Component UNBOUND_HELP = createTranslation("item", "security_card.unbound.help");
    private static final Component BOUND_HELP = createTranslation("item", "security_card.bound.help");

    public SecurityCardItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        final SecurityCardBoundPlayer boundPlayer = stack.get(DataComponents.INSTANCE.getSecurityCardBoundPlayer());
        if (boundPlayer == null) {
            lines.add(createTranslation("item", "security_card.unbound").withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(createTranslation(
                "item",
                "security_card.bound",
                Component.literal(boundPlayer.playerName()).withStyle(ChatFormatting.YELLOW)
            ).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, lines, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer
            && !stack.has(DataComponents.INSTANCE.getSecurityCardBoundPlayer())) {
            setBoundPlayer(serverPlayer, stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(isValid(stack) ? BOUND_HELP : UNBOUND_HELP));
    }

    @Override
    AbstractSecurityCardExtendedMenuProvider<PlayerBoundSecurityCardData> createMenuProvider(
        final MinecraftServer server,
        final SlotReference slotReference,
        final SecurityPolicy policy,
        final Set<PlatformPermission> dirtyPermissions,
        final ItemStack stack
    ) {
        return new SecurityCardExtendedMenuProvider(
            server,
            slotReference,
            policy,
            dirtyPermissions,
            requireNonNull(stack.get(DataComponents.INSTANCE.getSecurityCardBoundPlayer()))
        );
    }

    @Override
    public boolean isValid(final ItemStack stack) {
        return stack.has(DataComponents.INSTANCE.getSecurityCardBoundPlayer());
    }

    @Override
    public Optional<SecurityActor> getActor(final ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.INSTANCE.getSecurityCardBoundPlayer()))
            .map(SecurityCardBoundPlayer::toSecurityActor);
    }

    @Override
    public long getEnergyUsage() {
        return Platform.INSTANCE.getConfig().getSecurityCard().getEnergyUsage();
    }

    void setBoundPlayer(final ServerPlayer player, final ItemStack stack) {
        final SecurityCardBoundPlayer boundPlayer = SecurityCardBoundPlayer.of(player);
        stack.set(DataComponents.INSTANCE.getSecurityCardBoundPlayer(), boundPlayer);
    }
}
