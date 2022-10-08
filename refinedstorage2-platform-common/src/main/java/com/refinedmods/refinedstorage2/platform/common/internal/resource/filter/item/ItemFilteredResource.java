package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

public record ItemFilteredResource(ItemResource value, long amount) implements FilteredResource {
    private static final String TAG_AMOUNT = "amt";

    public static long getAmountFromTag(final CompoundTag tag) {
        return tag.getLong(TAG_AMOUNT);
    }

    @Override
    public void writeToPacket(final FriendlyByteBuf buf) {
        PacketUtil.writeItemResource(buf, value);
        buf.writeLong(amount);
    }

    @Override
    public CompoundTag toTag() {
        final CompoundTag tag = ItemResource.toTag(value);
        tag.putLong(TAG_AMOUNT, amount);
        return tag;
    }

    @Override
    public void render(final PoseStack poseStack, final int x, final int y, final int z) {
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(value.toItemStack(), x, y);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public FilteredResource withAmount(final long newAmount) {
        return new ItemFilteredResource(value, newAmount);
    }

    @Override
    public long getMaxAmount() {
        return value.toItemStack().getMaxStackSize();
    }

    @Override
    public String getFormattedAmount() {
        if (amount == 1) {
            return "";
        }
        return QuantityFormatter.formatWithUnits(amount);
    }

    @Override
    public ResourceType getType() {
        return ItemResourceType.INSTANCE;
    }

    @Override
    public List<Component> getTooltipLines(@Nullable final Player player) {
        final TooltipFlag.Default flag = Minecraft.getInstance().options.advancedItemTooltips
            ? TooltipFlag.Default.ADVANCED
            : TooltipFlag.Default.NORMAL;
        return value.toItemStack().getTooltipLines(player, flag);
    }
}
