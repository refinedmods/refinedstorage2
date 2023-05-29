package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeInDestination;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.2")
public abstract class AbstractUpgradeItem extends Item implements UpgradeItem {
    private final UpgradeRegistry registry;

    protected AbstractUpgradeItem(final Properties properties, final UpgradeRegistry registry) {
        super(properties);
        this.registry = registry;
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                @Nullable final Level level,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        final Set<UpgradeInDestination> destinations = registry.getDestinations(this);
        if (destinations.isEmpty()) {
            return;
        }
        lines.add(PlatformApi.INSTANCE.createTranslation(
            "item",
            "upgrade.supported_by"
        ).withStyle(ChatFormatting.WHITE));
        for (final UpgradeInDestination upgradeInDestination : destinations) {
            final MutableComponent name = upgradeInDestination.destination().getName().copy();
            final MutableComponent amount = Component.literal("(" + upgradeInDestination.maxAmount() + ")");
            lines.add(name.append(" ").append(amount).withStyle(ChatFormatting.GRAY));
        }
    }
}

