package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.api.network.security.SecurityActor;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.content.ContentIds;

import java.util.Optional;
import java.util.Set;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class FallbackSecurityCardItem extends AbstractSecurityCardItem<SecurityCardData> {
    private static final Component HELP = createTranslation("item", "fallback_security_card.help");

    public FallbackSecurityCardItem() {
        super(new Item.Properties().stacksTo(1)
            .setId(ResourceKey.create(Registries.ITEM, ContentIds.FALLBACK_SECURITY_CARD)));
    }

    @Override
    AbstractSecurityCardExtendedMenuProvider<SecurityCardData> createMenuProvider(
        final MinecraftServer server,
        final PlayerSlotReference playerSlotReference,
        final SecurityPolicy policy,
        final Set<PlatformPermission> dirtyPermissions,
        final ItemStack stack
    ) {
        return new FallbackSecurityCardExtendedMenuProvider(
            stack.get(DataComponents.CUSTOM_NAME),
            playerSlotReference,
            policy,
            dirtyPermissions
        );
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(HELP));
    }

    @Override
    public Optional<SecurityActor> getActor(final ItemStack stack) {
        return Optional.empty();
    }

    @Override
    public long getEnergyUsage() {
        return Platform.INSTANCE.getConfig().getFallbackSecurityCard().getEnergyUsage();
    }
}
