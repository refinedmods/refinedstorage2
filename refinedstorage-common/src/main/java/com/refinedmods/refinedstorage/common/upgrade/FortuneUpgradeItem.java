package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class FortuneUpgradeItem extends AbstractUpgradeItem {
    private static final Component HELP = createTranslation("item", "fortune_upgrade.help");

    private final int fortuneLevel;
    private final Component name;

    public FortuneUpgradeItem(final Identifier id, final UpgradeRegistry registry, final int fortuneLevel) {
        super(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)), registry, HELP);
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
