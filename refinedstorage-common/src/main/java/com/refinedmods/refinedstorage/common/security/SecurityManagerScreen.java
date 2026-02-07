package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class SecurityManagerScreen extends AbstractBaseScreen<SecurityManagerContainerMenu> {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/security_manager.png");

    public SecurityManagerScreen(final SecurityManagerContainerMenu menu,
                                 final Inventory playerInventory,
                                 final Component title) {
        super(menu, playerInventory, title, 197, 154);
        this.inventoryLabelY = 59;
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
    protected void extractTooltip(final GuiGraphicsExtractor graphics, final int x, final int y) {
        if (hoveredSlot != null && hoveredSlot == menu.getFallbackSecurityCardSlot() && !hoveredSlot.hasItem()) {
            graphics.tooltip(font, List.of(
                ClientTooltipComponent.create(
                    createTranslation("gui", "security_manager.fallback_security_card_slot_hint").getVisualOrderText()
                ),
                HelpClientTooltipComponent.create(
                    createTranslation("gui", "security_manager.no_fallback_security_card_consequence")
                )
            ), x, y, DefaultTooltipPositioner.INSTANCE, null);
            return;
        }
        super.extractTooltip(graphics, x, y);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }
}
