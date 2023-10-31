package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FortuneUpgradeItem extends AbstractUpgradeItem {
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
}
