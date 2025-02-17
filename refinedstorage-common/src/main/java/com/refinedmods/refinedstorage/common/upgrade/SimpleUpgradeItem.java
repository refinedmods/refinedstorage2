package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;

import java.util.function.LongSupplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public final class SimpleUpgradeItem extends AbstractUpgradeItem {
    private final LongSupplier energyUsageResolver;
    private final boolean foil;

    private SimpleUpgradeItem(final UpgradeRegistry registry,
                              final LongSupplier energyUsageResolver,
                              final boolean foil,
                              final Component helpText) {
        super(new Item.Properties(), registry, helpText);
        this.energyUsageResolver = energyUsageResolver;
        this.foil = foil;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsageResolver.getAsLong();
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return foil;
    }

    public static SimpleUpgradeItem speedUpgrade() {
        return new SimpleUpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            Platform.INSTANCE.getConfig().getUpgrade()::getSpeedUpgradeEnergyUsage,
            false,
            createTranslation("item", "speed_upgrade.help")
        );
    }

    public static SimpleUpgradeItem stackUpgrade() {
        return new SimpleUpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            Platform.INSTANCE.getConfig().getUpgrade()::getStackUpgradeEnergyUsage,
            false,
            createTranslation("item", "stack_upgrade.help")
        );
    }

    public static SimpleUpgradeItem silkTouchUpgrade() {
        return new SimpleUpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            Platform.INSTANCE.getConfig().getUpgrade()::getSilkTouchUpgradeEnergyUsage,
            true,
            createTranslation("item", "silk_touch_upgrade.help")
        );
    }

    // TODO: it appears like the constructor is constantly starting jobs?
    // TODO: more than 1 regulator in exporter and importer?
    public static SimpleUpgradeItem autocraftingUpgrade() {
        return new SimpleUpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            Platform.INSTANCE.getConfig().getUpgrade()::getAutocraftingUpgradeEnergyUsage,
            false,
            createTranslation("item", "autocrafting_upgrade.help")
        );
    }
}
