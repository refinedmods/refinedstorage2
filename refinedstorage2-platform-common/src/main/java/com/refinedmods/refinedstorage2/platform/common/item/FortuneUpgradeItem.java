package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.api.item.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FortuneUpgradeItem extends AbstractUpgradeItem {
    private static final Component NAME = createTranslation("item", "fortune_upgrade");

    private final int fortuneLevel;

    public FortuneUpgradeItem(final UpgradeRegistry registry, final int fortuneLevel) {
        super(new Item.Properties(), registry);
        this.fortuneLevel = fortuneLevel;
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                @Nullable final Level level,
                                final List<Component> tooltip,
                                final TooltipFlag flag) {
        tooltip.add(Component.translatable("enchantment.minecraft.fortune")
            .append(" ")
            .append(Component.translatable("enchantment.level." + fortuneLevel))
            .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
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
        return NAME;
    }
}
