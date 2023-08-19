package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeMapping;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.ResourceSlot;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.UpgradeSlot;
import com.refinedmods.refinedstorage2.platform.common.screen.amount.ResourceAmountScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.MouseWithIconClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.SmallTextClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.UpgradeItemClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationAsHeading;

public abstract class AbstractBaseScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final SmallTextClientTooltipComponent CLICK_TO_CLEAR = new SmallTextClientTooltipComponent(
        createTranslationAsHeading("gui", "filter_slot.click_to_clear")
    );
    private static final ClientTooltipComponent EMPTY_FILTER = ClientTooltipComponent.create(
        createTranslationAsHeading("gui", "filter_slot.empty_filter").getVisualOrderText()
    );

    private final Inventory playerInventory;
    private final List<Rect2i> exclusionZones = new ArrayList<>();
    private int sideButtonY;

    protected AbstractBaseScreen(final T menu, final Inventory playerInventory, final Component text) {
        super(menu, playerInventory, text);
        this.playerInventory = playerInventory;
        this.titleLabelX = 7;
        this.titleLabelY = 7;
        this.inventoryLabelX = 7;
    }

    @Override
    protected void init() {
        clearWidgets();
        super.init();
        sideButtonY = 6;
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        exclusionZones.clear();
    }

    protected abstract ResourceLocation getTexture();

    @Override
    protected void renderBg(final GuiGraphics graphics, final float delta, final int mouseX, final int mouseY) {
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        graphics.blit(getTexture(), x, y, 0, 0, imageWidth, imageHeight);
        renderResourceSlots(graphics);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);
    }

    protected final void renderResourceSlots(final GuiGraphics graphics) {
        if (!(menu instanceof AbstractResourceContainerMenu resourceContainerMenu)) {
            return;
        }
        for (final ResourceSlot slot : resourceContainerMenu.getResourceSlots()) {
            tryRenderResourceSlot(graphics, slot);
        }
    }

    private void tryRenderResourceSlot(final GuiGraphics graphics, final ResourceSlot slot) {
        final ResourceAmountTemplate<?> resourceAmount = slot.getResourceAmount();
        if (resourceAmount == null) {
            return;
        }
        renderResourceSlot(
            graphics,
            leftPos + slot.x,
            topPos + slot.y,
            resourceAmount,
            slot.shouldRenderAmount()
        );
    }

    private <R> void renderResourceSlot(final GuiGraphics graphics,
                                        final int x,
                                        final int y,
                                        final ResourceAmountTemplate<R> resourceAmount,
                                        final boolean renderAmount) {
        final ResourceRendering<R> rendering = PlatformApi.INSTANCE.getResourceRendering(
            resourceAmount.getResource()
        );
        rendering.render(resourceAmount.getResource(), graphics, x, y);
        if (renderAmount) {
            renderResourceSlotAmount(graphics, x, y, resourceAmount.getAmount(), rendering);
        }
    }

    private <R> void renderResourceSlotAmount(final GuiGraphics graphics,
                                              final int x,
                                              final int y,
                                              final long amount,
                                              final ResourceRendering<R> rendering) {
        renderAmount(
            graphics,
            x,
            y,
            rendering.getDisplayedAmount(amount),
            Objects.requireNonNullElse(ChatFormatting.WHITE.getColor(), 15),
            true
        );
    }

    protected void renderAmount(final GuiGraphics graphics,
                                final int x,
                                final int y,
                                final String amount,
                                final int color,
                                final boolean large) {
        final PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        // Large amounts overlap with the slot lines (see Minecraft behavior)
        poseStack.translate(x + (large ? 1D : 0D), y + (large ? 1D : 0D), 199);
        if (!large) {
            poseStack.scale(0.5F, 0.5F, 1);
        }
        graphics.drawString(font, amount, (large ? 16 : 30) - font.width(amount), large ? 8 : 22, color, true);
        poseStack.popPose();
    }

    public void addSideButton(final AbstractSideButtonWidget button) {
        button.setX(leftPos - button.getWidth() - 2);
        button.setY(topPos + sideButtonY);
        exclusionZones.add(new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
        sideButtonY += button.getHeight() + 2;
        addRenderableWidget(button);
    }

    public List<Rect2i> getExclusionZones() {
        return exclusionZones;
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (hoveredSlot instanceof UpgradeSlot upgradeSlot) {
            final List<ClientTooltipComponent> tooltip = getUpgradeTooltip(menu.getCarried(), upgradeSlot);
            if (!tooltip.isEmpty()) {
                Platform.INSTANCE.renderTooltip(graphics, tooltip, x, y);
                return;
            }
        }
        if (hoveredSlot instanceof ResourceSlot resourceSlot) {
            final List<ClientTooltipComponent> tooltip = getResourceTooltip(menu.getCarried(), resourceSlot);
            if (!tooltip.isEmpty()) {
                Platform.INSTANCE.renderTooltip(graphics, tooltip, x, y);
                return;
            }
        }
        super.renderTooltip(graphics, x, y);
    }

    private List<ClientTooltipComponent> getUpgradeTooltip(final ItemStack carried, final UpgradeSlot upgradeSlot) {
        if (!carried.isEmpty() || upgradeSlot.hasItem()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> lines = new ArrayList<>();
        lines.add(ClientTooltipComponent.create(
            createTranslationAsHeading("gui", "upgrade_slot").getVisualOrderText()
        ));
        for (final UpgradeMapping upgrade : upgradeSlot.getAllowedUpgrades()) {
            lines.add(new UpgradeItemClientTooltipComponent(upgrade));
        }
        return lines;
    }

    public List<ClientTooltipComponent> getResourceTooltip(final ItemStack carried, final ResourceSlot resourceSlot) {
        final ResourceAmountTemplate<?> resourceAmount = resourceSlot.getResourceAmount();
        if (resourceAmount == null) {
            return getTooltipForEmptySlot(carried, resourceSlot);
        }
        return getTooltipForResource(resourceAmount, resourceSlot);
    }

    private List<ClientTooltipComponent> getTooltipForEmptySlot(final ItemStack carried,
                                                                final ResourceSlot resourceSlot) {
        if (resourceSlot.isDisabled() || resourceSlot.supportsItemSlotInteractions()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> tooltip = new ArrayList<>();
        tooltip.add(EMPTY_FILTER);
        tooltip.addAll(getResourceSlotHelpTooltip(carried, resourceSlot));
        tooltip.add(HelpClientTooltipComponent.create(resourceSlot.getHelpText()));
        return tooltip;
    }

    private List<ClientTooltipComponent> getResourceSlotHelpTooltip(final ItemStack carried,
                                                                    final ResourceSlot resourceSlot) {
        if (carried.isEmpty()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> lines = new ArrayList<>();
        resourceSlot.getPrimaryResourceFactory().create(carried).ifPresent(primaryResourceInstance -> lines.add(
            new MouseWithIconClientTooltipComponent(
                MouseWithIconClientTooltipComponent.Type.LEFT,
                getResourceRendering(primaryResourceInstance.getResource()),
                null
            )
        ));
        for (final ResourceFactory<?> alternativeResourceFactory : resourceSlot.getAlternativeResourceFactories()) {
            final var result = alternativeResourceFactory.create(carried);
            result.ifPresent(alternativeResourceInstance -> lines.add(new MouseWithIconClientTooltipComponent(
                MouseWithIconClientTooltipComponent.Type.RIGHT,
                getResourceRendering(alternativeResourceInstance.getResource()),
                null
            )));
        }
        return lines;
    }

    public static <T> MouseWithIconClientTooltipComponent.IconRenderer getResourceRendering(final T resource) {
        return (graphics, x, y) -> PlatformApi.INSTANCE.getResourceRendering(resource).render(resource, graphics, x, y);
    }

    private <R> List<ClientTooltipComponent> getTooltipForResource(final ResourceAmountTemplate<R> resourceAmount,
                                                                   final ResourceSlot resourceSlot) {
        final List<ClientTooltipComponent> tooltip = PlatformApi.INSTANCE
            .getResourceRendering(resourceAmount.getResource())
            .getTooltip(resourceAmount.getResource())
            .stream()
            .map(Component::getVisualOrderText)
            .map(ClientTooltipComponent::create)
            .collect(Collectors.toList());
        if (!resourceSlot.isDisabled() && !resourceSlot.supportsItemSlotInteractions()) {
            tooltip.add(CLICK_TO_CLEAR);
        }
        return tooltip;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        if (hoveredSlot instanceof ResourceSlot resourceSlot
            && !resourceSlot.supportsItemSlotInteractions()
            && !resourceSlot.isDisabled()
            && getMenu() instanceof AbstractResourceContainerMenu containerMenu) {
            if (!tryOpenResourceAmountScreen(resourceSlot)) {
                containerMenu.sendResourceSlotChange(hoveredSlot.index, clickedButton == 1);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    private boolean tryOpenResourceAmountScreen(final ResourceSlot slot) {
        final boolean isFilterSlot = slot.getResourceAmount() != null;
        final boolean canModifyAmount = isFilterSlot && slot.canModifyAmount();
        final boolean isNotTryingToRemoveFilter = !hasShiftDown();
        final boolean isNotCarryingItem = getMenu().getCarried().isEmpty();
        final boolean canOpen =
            isFilterSlot && canModifyAmount && isNotTryingToRemoveFilter && isNotCarryingItem;
        if (canOpen && minecraft != null) {
            minecraft.setScreen(new ResourceAmountScreen(this, playerInventory, slot));
        }
        return canOpen;
    }

    @Nullable
    public ResourceTemplate<?> getHoveredResource() {
        return hoveredSlot instanceof ResourceSlot resourceSlot && resourceSlot.getResourceAmount() != null
            ? resourceSlot.getResourceAmount().getResourceTemplate()
            : null;
    }

    public int getLeftPos() {
        return leftPos;
    }

    public int getTopPos() {
        return topPos;
    }
}
