package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.item;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

public class ItemFilteredResource implements FilteredResource {
    private final ItemResource value;

    public ItemFilteredResource(ItemResource value) {
        this.value = value;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf) {
        PacketUtil.writeItemResource(buf, value);
    }

    @Override
    public CompoundTag toTag() {
        return ItemResource.toTag(value);
    }

    @Override
    public void render(PoseStack poseStack, int x, int y, int z) {
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(value.toItemStack(), x, y);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public ResourceType getType() {
        return ItemResourceType.INSTANCE;
    }

    @Override
    public List<Component> getTooltipLines(Player player) {
        TooltipFlag.Default flag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        return value.toItemStack().getTooltipLines(player, flag);
    }
}
