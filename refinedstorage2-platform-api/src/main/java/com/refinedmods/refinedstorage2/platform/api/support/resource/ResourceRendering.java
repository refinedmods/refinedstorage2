package com.refinedmods.refinedstorage2.platform.api.support.resource;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.13")
public interface ResourceRendering<T> {
    String getDisplayedAmount(long amount, boolean withUnits);

    Component getDisplayName(T resource);

    List<Component> getTooltip(T resource);

    void render(T resource, GuiGraphics graphics, int x, int y);

    void render(T resource, PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light, Level level);
}
