package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.platform.api.constructordestructor.DestructorStrategy;
import com.refinedmods.refinedstorage.platform.api.constructordestructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.upgrade.UpgradeItem;
import com.refinedmods.refinedstorage.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage.platform.common.content.Items;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class BlockBreakDestructorStrategyFactory implements DestructorStrategyFactory {
    private static final ItemStack DEFAULT_TOOL = new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
    private static final List<UpgradeMapping> UPGRADE_MAPPINGS = List.of(
        new UpgradeMapping(Items.INSTANCE::getSilkTouchUpgrade, Enchantments.SILK_TOUCH, 1),
        new UpgradeMapping(Items.INSTANCE::getFortune3Upgrade, Enchantments.FORTUNE, 3),
        new UpgradeMapping(Items.INSTANCE::getFortune2Upgrade, Enchantments.FORTUNE, 2),
        new UpgradeMapping(Items.INSTANCE::getFortune1Upgrade, Enchantments.FORTUNE, 1)
    );

    @Override
    public Optional<DestructorStrategy> create(final ServerLevel level,
                                               final BlockPos pos,
                                               final Direction direction,
                                               final UpgradeState upgradeState,
                                               final boolean pickupItems) {
        final ItemStack tool = DEFAULT_TOOL.copy();
        enchantTool(level, upgradeState, tool);
        return Optional.of(new BlockBreakDestructorStrategy(level, pos, direction, tool));
    }

    private static void enchantTool(final ServerLevel level, final UpgradeState upgradeState, final ItemStack tool) {
        for (final UpgradeMapping upgradeMapping : UPGRADE_MAPPINGS) {
            if (upgradeState.has(upgradeMapping.upgradeItemSupplier.get())) {
                level.holderLookup(Registries.ENCHANTMENT).get(upgradeMapping.enchantment).ifPresent(
                    enchantment -> tool.enchant(enchantment, upgradeMapping.level)
                );
                return;
            }
        }
    }

    private record UpgradeMapping(
        Supplier<UpgradeItem> upgradeItemSupplier,
        ResourceKey<Enchantment> enchantment,
        int level
    ) {
    }
}
