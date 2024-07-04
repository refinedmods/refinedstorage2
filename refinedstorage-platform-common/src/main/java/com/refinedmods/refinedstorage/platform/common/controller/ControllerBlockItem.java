package com.refinedmods.refinedstorage.platform.common.controller;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.api.support.energy.AbstractEnergyBlockItem;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class ControllerBlockItem extends AbstractEnergyBlockItem {
    private final Block block;

    ControllerBlockItem(final Block block) {
        super(block, new Item.Properties().stacksTo(1), PlatformApi.INSTANCE.getEnergyItemHelper());
        this.block = block;
    }

    @Override
    public Component getDescription() {
        return block.getName();
    }

    @Override
    public Component getName(final ItemStack stack) {
        return block.getName();
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(createTranslation("item", "controller.help")));
    }

    public EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Platform.INSTANCE.getConfig().getController().getEnergyCapacity()
        );
        return PlatformApi.INSTANCE.asBlockItemEnergyStorage(
            energyStorage,
            stack,
            BlockEntities.INSTANCE.getController()
        );
    }

    @Override
    protected boolean placeBlock(final BlockPlaceContext ctx, final BlockState state) {
        if (ctx.getPlayer() instanceof ServerPlayer serverPlayer
            && !(PlatformApi.INSTANCE.canPlaceNetworkNode(serverPlayer, ctx.getLevel(), ctx.getClickedPos(), state))) {
            return false;
        }
        return super.placeBlock(ctx, state);
    }
}
