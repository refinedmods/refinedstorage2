package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class RangeUpgradeItem extends AbstractUpgradeItem {
    private static final Component HELP = createTranslation("item", "range_upgrade.help");
    private static final Component CREATIVE_HELP = createTranslation("item", "creative_range_upgrade.help");

    private final boolean creative;

    public RangeUpgradeItem(final Identifier id, final UpgradeRegistry registry, final boolean creative) {
        super(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)), registry,
            creative ? CREATIVE_HELP : HELP);
        this.creative = creative;
    }

    @Override
    public long getEnergyUsage() {
        if (creative) {
            return Platform.INSTANCE.getConfig().getUpgrade().getCreativeRangeUpgradeEnergyUsage();
        }
        return Platform.INSTANCE.getConfig().getUpgrade().getRangeUpgradeEnergyUsage();
    }
}
