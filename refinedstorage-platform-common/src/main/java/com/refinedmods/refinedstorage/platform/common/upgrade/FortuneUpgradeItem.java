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

public class FortuneUpgradeItem extends AbstractUpgradeItem {
    private static final Component HELP = createTranslation("item", "fortune_upgrade.help");

    private final int fortuneLevel;
    private final Component name;

    public FortuneUpgradeItem(final UpgradeRegistry registry, final int fortuneLevel) {
        super(new Item.Properties(), registry);
        this.fortuneLevel = fortuneLevel;
        this.name = createTranslation("item", "fortune_upgrade." + fortuneLevel);
    }

    @Override
    public long getEnergyUsage() {
        if (fortuneLevel == 1) {
            return Platform.INSTANCE.getConfig().getUpgrade().getFortune1UpgradeEnergyUsage();
        } else if (fortuneLevel == 2) {
            return Platform.INSTANCE.getConfig().getUpgrade().getFortune2UpgradeEnergyUsage();
        }
        return Platform.INSTANCE.getConfig().getUpgrade().getFortune3UpgradeEnergyUsage();
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return true;
    }

    @Override
    public Component getName(final ItemStack stack) {
        return name;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(HELP));
    }
}
