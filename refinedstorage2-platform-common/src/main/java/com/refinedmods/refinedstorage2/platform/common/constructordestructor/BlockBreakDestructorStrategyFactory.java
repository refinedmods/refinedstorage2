package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.platform.api.constructordestructor.DestructorStrategy;
import com.refinedmods.refinedstorage2.platform.api.constructordestructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeState;
import com.refinedmods.refinedstorage2.platform.common.content.Items;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class BlockBreakDestructorStrategyFactory implements DestructorStrategyFactory {
    private static final ItemStack DEFAULT_TOOL = new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
    private static final List<Tool> TOOLS = List.of(
        Tool.of(Items.INSTANCE::getSilkTouchUpgrade, Enchantments.SILK_TOUCH, 1),
        Tool.of(Items.INSTANCE::getFortune3Upgrade, Enchantments.BLOCK_FORTUNE, 3),
        Tool.of(Items.INSTANCE::getFortune2Upgrade, Enchantments.BLOCK_FORTUNE, 2),
        Tool.of(Items.INSTANCE::getFortune1Upgrade, Enchantments.BLOCK_FORTUNE, 1)
    );

    @Override
    public Optional<DestructorStrategy> create(final ServerLevel level,
                                               final BlockPos pos,
                                               final Direction direction,
                                               final UpgradeState upgradeState,
                                               final boolean pickupItems) {
        final ItemStack tool = createTool(upgradeState);
        return Optional.of(new BlockBreakDestructorStrategy(level, pos, direction, tool));
    }

    private ItemStack createTool(final UpgradeState state) {
        for (final Tool tool : TOOLS) {
            if (state.has(tool.itemSupplier.get())) {
                return tool.tool;
            }
        }
        return DEFAULT_TOOL;
    }

    private record Tool(Supplier<UpgradeItem> itemSupplier, ItemStack tool) {
        private static Tool of(final Supplier<UpgradeItem> item, final Enchantment enchantment, final int level) {
            final ItemStack resultingTool = DEFAULT_TOOL.copy();
            resultingTool.enchant(enchantment, level);
            return new Tool(item, resultingTool);
        }
    }
}
