package com.refinedmods.refinedstorage.common.api.support.network.item;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface NetworkItemHelper {
    void addTooltip(ItemStack stack, Consumer<Component> builder);

    InteractionResult bind(UseOnContext ctx);

    Optional<TooltipComponent> getTooltipImage(ItemStack stack);

    NetworkItemContext createContext(ItemStack stack, ServerPlayer player, PlayerSlotReference playerSlotReference);

    boolean isBound(ItemStack stack);
}
