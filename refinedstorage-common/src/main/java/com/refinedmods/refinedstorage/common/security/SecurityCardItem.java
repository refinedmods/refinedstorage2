package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.api.network.security.SecurityActor;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNull;

public class SecurityCardItem extends AbstractSecurityCardItem<PlayerBoundSecurityCardData> {
    private static final Component UNBOUND_HELP = createTranslation("item", "security_card.unbound.help");
    private static final Component BOUND_HELP = createTranslation("item", "security_card.bound.help");

    public SecurityCardItem() {
        super(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, ContentIds.SECURITY_CARD)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display,
                                final Consumer<Component> builder, final TooltipFlag tooltipFlag) {
        final SecurityCardBoundPlayer boundPlayer = stack.get(DataComponents.INSTANCE.getSecurityCardBoundPlayer());
        if (boundPlayer == null) {
            builder.accept(createTranslation("item", "security_card.unbound").withStyle(ChatFormatting.GRAY));
        } else {
            builder.accept(createTranslation(
                "item",
                "security_card.bound",
                Component.literal(boundPlayer.playerName()).withStyle(ChatFormatting.YELLOW)
            ).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
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
        final PlayerSlotReference playerSlotReference,
        final SecurityPolicy policy,
        final Set<PlatformPermission> dirtyPermissions,
        final ItemStack stack
    ) {
        return new SecurityCardExtendedMenuProvider(
            stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME),
            server,
            playerSlotReference,
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
