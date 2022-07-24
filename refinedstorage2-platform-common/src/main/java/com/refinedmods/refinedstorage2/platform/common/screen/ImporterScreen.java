package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ImporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ExactModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.RedstoneModeSideButtonWidget;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.ResourceFilterButtonWidget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ImporterScreen extends AbstractBaseScreen<ImporterContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/importer.png");

    public ImporterScreen(final ImporterContainerMenu menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);

        this.titleLabelX = 7;
        this.titleLabelY = 7;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = 42;
        this.imageWidth = 210;
        this.imageHeight = 137;
    }

    @Override
    protected void init() {
        super.init();

        addSideButton(new RedstoneModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.REDSTONE_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new FilterModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FILTER_MODE),
            this::renderComponentTooltip
        ));
        addSideButton(new ExactModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.EXACT_MODE),
            this::renderComponentTooltip
        ));

        final ResourceFilterButtonWidget resourceFilterButton = new ResourceFilterButtonWidget(
            leftPos + imageWidth - ResourceFilterButtonWidget.WIDTH - 7 - 34,
            topPos + 4,
            menu
        );
        addRenderableWidget(resourceFilterButton);
    }

    @Override
    protected void renderBg(final PoseStack poseStack, final float delta, final int mouseX, final int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);

        super.renderBg(poseStack, delta, mouseX, mouseY);
    }

    @Override
    public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
