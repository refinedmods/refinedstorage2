package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.support.widget.RedstoneModeSideButtonWidget;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class SecurityManagerScreen extends AbstractBaseScreen<SecurityManagerContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/security_manager.png");

    public SecurityManagerScreen(final SecurityManagerContainerMenu menu,
                                 final Inventory playerInventory,
                                 final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 59;
        this.imageWidth = 197;
        this.imageHeight = 154;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.REDSTONE_MODE),
            createTranslation("gui", "security_manager.redstone_mode_help")
        ));
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (hoveredSlot != null && hoveredSlot == menu.getFallbackSecurityCardSlot() && !hoveredSlot.hasItem()) {
            Platform.INSTANCE.renderTooltip(graphics, List.of(
                ClientTooltipComponent.create(
                    createTranslation("gui", "security_manager.fallback_security_card_slot_hint").getVisualOrderText()
                ),
                HelpClientTooltipComponent.create(
                    createTranslation("gui", "security_manager.no_fallback_security_card_consequence")
                )
            ), x, y);
            return;
        }
        super.renderTooltip(graphics, x, y);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
