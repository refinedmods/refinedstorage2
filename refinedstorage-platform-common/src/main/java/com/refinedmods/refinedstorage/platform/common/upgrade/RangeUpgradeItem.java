package com.refinedmods.refinedstorage.platform.common.upgrade;

import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage.platform.common.Platform;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class RangeUpgradeItem extends AbstractUpgradeItem {
    private static final Component HELP = createTranslation("item", "range_upgrade.help");
    private static final Component CREATIVE_HELP = createTranslation("item", "creative_range_upgrade.help");

    private final boolean creative;

    public RangeUpgradeItem(final UpgradeRegistry registry, final boolean creative) {
        super(new Item.Properties(), registry);
        this.creative = creative;
    }

    @Override
    public long getEnergyUsage() {
        if (creative) {
            return Platform.INSTANCE.getConfig().getUpgrade().getCreativeRangeUpgradeEnergyUsage();
        }
        return Platform.INSTANCE.getConfig().getUpgrade().getRangeUpgradeEnergyUsage();
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(creative ? CREATIVE_HELP : HELP));
    }
}
