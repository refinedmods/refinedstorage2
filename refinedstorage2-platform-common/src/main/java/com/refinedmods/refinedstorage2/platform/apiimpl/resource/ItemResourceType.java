package com.refinedmods.refinedstorage2.platform.apiimpl.resource;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ItemResourceType implements ResourceType<ItemResource> {
    private static final ResourceLocation ID = createIdentifier("item_resource_type");
    private static final TranslatableComponent NAME = createTranslation("misc", "resource_type.item");

    public static final ItemResourceType INSTANCE = new ItemResourceType();

    private ItemResourceType() {
    }

    @Override
    public Component getName() {
        return NAME;
    }

    @Override
    public Optional<ItemResource> translate(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(new ItemResource(stack));
    }

    @Override
    public void render(PoseStack poseStack, ItemResource value, int x, int y, int z) {
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(value.toItemStack(), x, y);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public ItemResource readFromPacket(FriendlyByteBuf buf) {
        return PacketUtil.readItemResource(buf);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, ItemResource value) {
        PacketUtil.writeItemResource(buf, value);
    }

    @Override
    public CompoundTag toTag(ItemResource value) {
        return ItemResource.toTag(value);
    }

    @Override
    public Optional<ItemResource> fromTag(CompoundTag tag) {
        return ItemResource.fromTag(tag);
    }

    @Override
    public List<Component> getTooltipLines(ItemResource value, Player player) {
        TooltipFlag.Default flag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        return value.toItemStack().getTooltipLines(player, flag);
    }
}
