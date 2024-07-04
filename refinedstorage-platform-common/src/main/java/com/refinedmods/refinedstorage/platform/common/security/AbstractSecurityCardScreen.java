package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.platform.common.support.widget.CustomCheckboxWidget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractSecurityCardScreen<T extends AbstractSecurityCardContainerMenu>
    extends AbstractStretchingScreen<T> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/security_card.png");

    private static final int RESET_BUTTON_WIDTH = 40;
    private static final int RESET_BUTTON_RIGHT_PADDING = 16;
    private static final Component RESET_TITLE = createTranslation("gui", "security_card.permission.reset");
    private static final Component MODIFIED_TITLE = createTranslation("gui", "security_card.permission.modified")
        .withStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.YELLOW));

    private final List<Permission> permissions = new ArrayList<>();

    protected AbstractSecurityCardScreen(final T menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 97;
        this.imageWidth = 193;
        this.imageHeight = 176;
    }

    @Override
    protected void init(final int rows) {
        permissions.clear();
        final List<SecurityCardData.Permission> menuPermissions = getMenu().getPermissions();
        for (int i = 0; i < menuPermissions.size(); ++i) {
            final Permission permission = createPermission(menuPermissions.get(i), i, rows);
            addWidget(permission.checkbox);
            addWidget(permission.resetButton);
            permissions.add(permission);
        }
        updateScrollbar(permissions.size());
    }

    private Permission createPermission(
        final SecurityCardData.Permission menuPermission,
        final int index,
        final int rows
    ) {
        final int y = getPermissionY(index);
        final boolean visible = isPermissionVisible(rows, y);
        final CustomCheckboxWidget checkbox = createPermissionCheckbox(menuPermission, y, visible);
        final Button resetButton = createPermissionResetButton(menuPermission, checkbox, y, visible);
        checkbox.setOnPressed((c, selected) -> updatePermission(menuPermission, resetButton, c, selected));
        return new Permission(checkbox, resetButton);
    }

    private CustomCheckboxWidget createPermissionCheckbox(
        final SecurityCardData.Permission menuPermission,
        final int y,
        final boolean visible
    ) {
        final CustomCheckboxWidget checkbox = new CustomCheckboxWidget(
            leftPos + 10,
            y,
            getPermissionName(menuPermission),
            font,
            menuPermission.allowed()
        );
        checkbox.visible = visible;
        checkbox.setTooltip(getPermissionTooltip(menuPermission));
        return checkbox;
    }

    private void updatePermission(final SecurityCardData.Permission menuPermission,
                                  final Button resetButton,
                                  final CustomCheckboxWidget checkbox,
                                  final boolean allowed) {
        updateCheckboxAndResetButton(checkbox, resetButton, menu.changePermission(
            menuPermission.permission(),
            allowed
        ));
    }

    private Tooltip getPermissionTooltip(final SecurityCardData.Permission menuPermission) {
        final PlatformPermission permission = menuPermission.permission();
        final MutableComponent ownerName = permission.getOwnerName().copy().withStyle(
            Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY)
        );
        final MutableComponent tooltip = permission.getDescription().copy().append("\n").append(ownerName);
        return Tooltip.create(menuPermission.dirty() ? tooltip.append("\n").append(MODIFIED_TITLE) : tooltip);
    }

    private Button createPermissionResetButton(final SecurityCardData.Permission menuPermission,
                                               final CustomCheckboxWidget checkbox,
                                               final int y,
                                               final boolean visible) {
        final Button resetButton = Button.builder(RESET_TITLE, btn -> resetPermission(menuPermission, checkbox, btn))
            .pos(leftPos + imageWidth - RESET_BUTTON_RIGHT_PADDING - RESET_BUTTON_WIDTH - 11, y)
            .size(RESET_BUTTON_WIDTH, 16)
            .build();
        resetButton.visible = visible;
        resetButton.active = menuPermission.dirty();
        return resetButton;
    }

    private void resetPermission(final SecurityCardData.Permission menuPermission,
                                 final CustomCheckboxWidget checkbox,
                                 final Button resetButton) {
        updateCheckboxAndResetButton(checkbox, resetButton, menu.resetPermission(menuPermission.permission()));
    }

    private void updateCheckboxAndResetButton(final CustomCheckboxWidget checkbox,
                                              final Button resetButton,
                                              final SecurityCardData.Permission menuPermission) {
        checkbox.setMessage(getPermissionName(menuPermission));
        checkbox.setTooltip(getPermissionTooltip(menuPermission));
        checkbox.setSelected(menuPermission.allowed());
        resetButton.active = menuPermission.dirty();
    }

    private Component getPermissionName(final SecurityCardData.Permission menuPermission) {
        final Component name = menuPermission.permission().getName();
        if (!menuPermission.dirty()) {
            return name;
        }
        return name.copy().append(" (*)").setStyle(Style.EMPTY.withItalic(true));
    }

    private int getPermissionY(final int index) {
        return topPos + 19 + (index * ROW_SIZE) + 3;
    }

    private boolean isPermissionVisible(final int rows, final int y) {
        return y >= (topPos + 19 - ROW_SIZE) && y < (topPos + 19 + (rows * ROW_SIZE));
    }

    @Override
    protected int getScrollPanePadding() {
        return 4;
    }

    @Override
    protected void scrollbarChanged(final int rows) {
        final int offset = getScrollbarOffset();
        for (int i = 0; i < permissions.size(); ++i) {
            final Permission permission = permissions.get(i);
            final int y = getPermissionY(i) - offset;
            final boolean visible = isPermissionVisible(rows, y);
            permission.setY(y);
            permission.setVisible(visible);
        }
    }

    @Override
    protected void renderRows(final GuiGraphics graphics,
                              final int x,
                              final int y,
                              final int topHeight,
                              final int rows,
                              final int mouseX,
                              final int mouseY) {
        for (final Permission permission : permissions) {
            permission.render(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderStretchingBackground(final GuiGraphics graphics, final int x, final int y, final int rows) {
        for (int row = 0; row < rows; ++row) {
            int textureY = 37;
            if (row == 0) {
                textureY = 19;
            } else if (row == rows - 1) {
                textureY = 55;
            }
            graphics.blit(getTexture(), x, y + (ROW_SIZE * row), 0, textureY, imageWidth, ROW_SIZE);
        }
    }

    @Override
    protected int getBottomHeight() {
        return 99;
    }

    @Override
    protected int getBottomV() {
        return 73;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    private record Permission(CustomCheckboxWidget checkbox, Button resetButton) {
        private void setY(final int y) {
            checkbox.setY(y);
            resetButton.setY(y);
        }

        private void setVisible(final boolean visible) {
            checkbox.visible = visible;
            resetButton.visible = visible;
        }

        private void render(final GuiGraphics graphics, final int mouseX, final int mouseY) {
            checkbox.render(graphics, mouseX, mouseY, 0);
            resetButton.render(graphics, mouseX, mouseY, 0);
        }
    }
}
