package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.api.support.energy.AbstractEnergyBlockItem;
import com.refinedmods.refinedstorage.common.content.BlockEntities;

import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ControllerBlockItem extends AbstractEnergyBlockItem {
    private final Block block;

    ControllerBlockItem(final Identifier id, final Block block) {
        super(block, new Item.Properties()
                .stacksTo(1)
                .setId(ResourceKey.create(Registries.ITEM, id))
                .overrideDescription(block.getDescriptionId()),
            RefinedStorageApi.INSTANCE.getEnergyItemHelper());
        this.block = block;
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
        return RefinedStorageApi.INSTANCE.asBlockItemEnergyStorage(
            energyStorage,
            stack,
            BlockEntities.INSTANCE.getController()
        );
    }

    @Override
    protected boolean placeBlock(final BlockPlaceContext ctx, final BlockState state) {
        if (ctx.getPlayer() instanceof ServerPlayer serverPlayer && !(RefinedStorageApi.INSTANCE.canPlaceNetworkNode(
            serverPlayer,
            ctx.getLevel(),
            ctx.getClickedPos(),
            state))
        ) {
            return false;
        }
        return super.placeBlock(ctx, state);
    }
}
