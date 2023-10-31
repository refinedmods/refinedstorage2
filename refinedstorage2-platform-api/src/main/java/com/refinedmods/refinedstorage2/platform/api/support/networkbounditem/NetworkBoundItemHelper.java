package com.refinedmods.refinedstorage2.platform.api.support.networkbounditem;

import java.util.List;
import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface NetworkBoundItemHelper {
    void addTooltip(ItemStack stack, List<Component> lines);

    InteractionResult bind(UseOnContext ctx);

    Optional<TooltipComponent> getTooltipImage(ItemStack stack);

    NetworkBoundItemSession openSession(ItemStack stack, ServerPlayer player, SlotReference slotReference);

    boolean isBound(ItemStack stack);
}
