package com.refinedmods.refinedstorage2.platform.api.resource.filter;

import java.util.List;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.0")
public interface FilteredResource {
    void writeToPacket(FriendlyByteBuf buf);

    CompoundTag toTag();

    void render(PoseStack poseStack, int x, int y, int z);

    Object getValue();

    long getAmount();

    FilteredResource withAmount(long newAmount);

    long getMaxAmount();

    String getFormattedAmount();

    ResourceType getType();

    List<Component> getTooltipLines(@Nullable Player player);
}
