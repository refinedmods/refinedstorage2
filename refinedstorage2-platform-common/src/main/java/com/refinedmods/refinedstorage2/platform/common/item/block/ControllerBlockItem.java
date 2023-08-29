package com.refinedmods.refinedstorage2.platform.common.item.block;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.item.AbstractEnergyBlockItem;
import com.refinedmods.refinedstorage2.platform.api.item.HelpTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

// TODO: Make network bound item api accessible.
public class ControllerBlockItem extends AbstractEnergyBlockItem {
    private final Component name;

    public ControllerBlockItem(final Block block, final Component displayName) {
        super(block, new Item.Properties().stacksTo(1));
        this.name = displayName;
    }

    @Override
    public Component getDescription() {
        return name;
    }

    @Override
    public Component getName(final ItemStack stack) {
        return name;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(createTranslation("item", "controller.help")));
    }

    @Override
    public Optional<EnergyStorage> createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Platform.INSTANCE.getConfig().getController().getEnergyCapacity()
        );
        return Optional.of(PlatformApi.INSTANCE.asItemEnergyStorage(energyStorage, stack));
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
        final BlockPos pos,
        final Level level,
        @Nullable final Player player,
        final ItemStack stack,
        final BlockState blockState
    ) {
        final boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, blockState);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ControllerBlockEntity controllerBlockEntity) {
            PlatformApi.INSTANCE.getEnergyStorage(stack).ifPresent(
                energyStorage -> controllerBlockEntity.loadEnergy(energyStorage.getStored())
            );
        }
        return result;
    }
}
