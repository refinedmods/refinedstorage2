package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeMapping;
import com.refinedmods.refinedstorage.common.support.amount.ResourceAmountScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.MouseClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallTextClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeItemClientTooltipComponent;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslationAsHeading;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public abstract class AbstractBaseScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected static final int TITLE_MAX_WIDTH = 162;

    private static final SmallTextClientTooltipComponent CLICK_TO_CLEAR = new SmallTextClientTooltipComponent(
        createTranslationAsHeading("gui", "filter_slot.click_to_clear")
    );
    private static final SmallTextClientTooltipComponent SHIFT_CLICK_TO_CLEAR = new SmallTextClientTooltipComponent(
        createTranslationAsHeading("gui", "filter_slot.shift_click_to_clear")
    );
    private static final ClientTooltipComponent EMPTY_FILTER = ClientTooltipComponent.create(
        createTranslationAsHeading("gui", "filter_slot.empty_filter").getVisualOrderText()
    );
    private static final ClientTooltipComponent EMPTY_UPGRADE_SLOT = ClientTooltipComponent.create(
        createTranslationAsHeading("gui", "empty_upgrade_slot").getVisualOrderText()
    );

    protected final TextMarquee titleMarquee;

    private final Inventory playerInventory;
    private final List<Rect2i> exclusionZones = new ArrayList<>();

    private int sideButtonY;

    @Nullable
    private List<ClientTooltipComponent> deferredTooltip;

    protected AbstractBaseScreen(final T menu, final Inventory playerInventory, final Component title,
                                 final int width, final int height) {
        this(menu, playerInventory, new TextMarquee(title, TITLE_MAX_WIDTH), width, height);
    }

    protected AbstractBaseScreen(final T menu, final Inventory playerInventory, final TextMarquee title,
                                 final int width, final int height) {
        super(menu, playerInventory, title.getText(), width, height);
        this.playerInventory = playerInventory;
        this.titleLabelX = 7;
        this.titleLabelY = 7;
        this.inventoryLabelX = 7;
        this.titleMarquee = title;
    }

    protected int getSideButtonY() {
        return 6;
    }

    protected int getSideButtonX() {
        return leftPos - AbstractSideButtonWidget.SIZE - 2;
    }

    @Override
    protected void init() {
        clearWidgets();
        super.init();
        sideButtonY = getSideButtonY();
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        exclusionZones.clear();
    }

    protected abstract Identifier getTexture();

    @Override
    protected void extractSlot(final GuiGraphicsExtractor graphics, final Slot slot, final int mouseX,
                               final int mouseY) {
        // This slot is already rendered somewhere else. Don't render items twice.
        if (slot instanceof ResourceSlot resourceSlot && resourceSlot.supportsItemSlotInteractions()) {
            return;
        }
        super.extractSlot(graphics, slot, mouseX, mouseY);
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        extractDefaultBackground(graphics);
    }

    protected void extractDefaultBackground(final GuiGraphicsExtractor graphics) {
        final int x = (width - imageWidth) / 2;
        final int y = (height - imageHeight) / 2;
        graphics.blit(GUI_TEXTURED, getTexture(), x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        final boolean hoveringOverTitle = isHovering(
            titleLabelX,
            titleLabelY,
            titleMarquee.getEffectiveWidth(font),
            font.lineHeight,
            mouseX,
            mouseY
        );
        titleMarquee.render(graphics, titleLabelX, titleLabelY, font, hoveringOverTitle);
        renderPlayerInventoryTitle(graphics);
    }

    protected final void renderPlayerInventoryTitle(final GuiGraphicsExtractor graphics) {
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, -12566464, false);
    }

    @Override
    protected void extractSlots(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        super.extractSlots(graphics, mouseX, mouseY);
        renderResourceSlots(graphics);
    }

    protected void renderResourceSlots(final GuiGraphicsExtractor graphics) {
        if (!(menu instanceof AbstractResourceContainerMenu resourceContainerMenu)) {
            return;
        }
        for (final ResourceSlot slot : resourceContainerMenu.getResourceSlots()) {
            ResourceSlotRendering.render(graphics, slot);
        }
    }

    public void addSideButton(final AbstractSideButtonWidget button) {
        button.setX(getSideButtonX());
        button.setY(topPos + sideButtonY);
        exclusionZones.add(new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
        sideButtonY += button.getHeight() + 2;
        addRenderableWidget(button);
    }

    @API(status = API.Status.INTERNAL)
    public List<Rect2i> getExclusionZones() {
        return exclusionZones;
    }

    @Override
    protected void extractTooltip(final GuiGraphicsExtractor graphics, final int x, final int y) {
        if (hoveredSlot instanceof UpgradeSlot upgradeSlot) {
            final List<ClientTooltipComponent> tooltip = getUpgradeTooltip(menu.getCarried(), upgradeSlot);
            if (!tooltip.isEmpty()) {
                graphics.tooltip(font, tooltip, x, y, DefaultTooltipPositioner.INSTANCE, null);
                return;
            }
        }
        if (hoveredSlot instanceof ResourceSlot resourceSlot && canInteractWithResourceSlot(resourceSlot, x, y)) {
            final List<ClientTooltipComponent> tooltip = getResourceSlotTooltip(menu.getCarried(), resourceSlot);
            if (!tooltip.isEmpty()) {
                graphics.tooltip(font, tooltip, x, y, DefaultTooltipPositioner.INSTANCE, null);
                return;
            }
        }
        if (deferredTooltip != null) {
            graphics.tooltip(font, deferredTooltip, x, y, DefaultTooltipPositioner.INSTANCE, null);
            deferredTooltip = null;
        }
        super.extractTooltip(graphics, x, y);
    }

    public void setDeferredTooltip(@Nullable final List<ClientTooltipComponent> deferredTooltip) {
        this.deferredTooltip = deferredTooltip;
    }

    private List<ClientTooltipComponent> getUpgradeTooltip(final ItemStack carried, final UpgradeSlot upgradeSlot) {
        if (!carried.isEmpty() || upgradeSlot.hasItem()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> lines = new ArrayList<>();
        lines.add(EMPTY_UPGRADE_SLOT);
        for (final UpgradeMapping upgrade : upgradeSlot.getAllowedUpgrades()) {
            lines.add(new UpgradeItemClientTooltipComponent(upgrade));
        }
        return lines;
    }

    public final List<ClientTooltipComponent> getResourceSlotTooltip(final ItemStack carried, final ResourceSlot slot) {
        final ResourceKey resource = slot.getResource();
        if (resource == null) {
            return getTooltipForEmptyResourceSlot(carried, slot);
        }
        return getResourceSlotTooltip(resource, slot);
    }

    protected List<ClientTooltipComponent> getResourceSlotTooltip(final ResourceKey resource, final ResourceSlot slot) {
        final List<ClientTooltipComponent> tooltip = RefinedStorageClientApi.INSTANCE
            .getResourceRendering(resource.getClass())
            .getTooltip(resource)
            .stream()
            .map(Component::getVisualOrderText)
            .map(ClientTooltipComponent::create)
            .collect(Collectors.toList());
        if (!slot.isDisabled() && !slot.supportsItemSlotInteractions()) {
            addResourceSlotTooltips(slot, tooltip);
        }
        if (slot.supportsItemSlotInteractions()) {
            RefinedStorageApi.INSTANCE.getResourceContainerInsertStrategies()
                .stream()
                .flatMap(strategy -> strategy.getConversionInfo(resource, getMenu().getCarried()).stream())
                .map(conversionInfo -> MouseClientTooltipComponent.itemConversion(
                    MouseClientTooltipComponent.Type.LEFT,
                    conversionInfo.from(),
                    conversionInfo.to(),
                    null
                ))
                .forEach(tooltip::add);
        }
        return tooltip;
    }

    private List<ClientTooltipComponent> getTooltipForEmptyResourceSlot(final ItemStack carried,
                                                                        final ResourceSlot slot) {
        if (slot.isDisabled() || slot.supportsItemSlotInteractions()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> tooltip = new ArrayList<>();
        tooltip.add(EMPTY_FILTER);
        tooltip.addAll(getResourceSlotHelpTooltip(carried, slot));
        tooltip.add(HelpClientTooltipComponent.create(slot.getHelpText()));
        return tooltip;
    }

    private List<ClientTooltipComponent> getResourceSlotHelpTooltip(final ItemStack carried,
                                                                    final ResourceSlot resourceSlot) {
        if (carried.isEmpty()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> lines = new ArrayList<>();
        resourceSlot.getPrimaryResourceFactory().create(carried).ifPresent(primaryResourceInstance -> lines.add(
            MouseClientTooltipComponent.resource(
                MouseClientTooltipComponent.Type.LEFT,
                primaryResourceInstance.resource(),
                null
            )
        ));
        for (final ResourceFactory alternativeResourceFactory : resourceSlot.getAlternativeResourceFactories()) {
            final var result = alternativeResourceFactory.create(carried);
            result.ifPresent(alternativeResourceInstance -> lines.add(MouseClientTooltipComponent.resource(
                MouseClientTooltipComponent.Type.RIGHT,
                alternativeResourceInstance.resource(),
                null
            )));
        }
        return lines;
    }

    protected void addResourceSlotTooltips(final ResourceSlot slot, final List<ClientTooltipComponent> tooltip) {
        if (slot.canModifyAmount()) {
            tooltip.add(new SmallTextClientTooltipComponent(slot.getClickToConfigureAmountHelpTooltip()));
            tooltip.add(SHIFT_CLICK_TO_CLEAR);
        } else {
            tooltip.add(CLICK_TO_CLEAR);
        }
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        if (hoveredSlot instanceof ResourceSlot resourceSlot
            && !resourceSlot.supportsItemSlotInteractions()
            && !resourceSlot.isDisabled()
            && canInteractWithResourceSlot(resourceSlot, event.x(), event.y())) {
            if (!tryOpenResourceAmountScreen(resourceSlot)
                && getMenu() instanceof AbstractResourceContainerMenu resourceMenu) {
                resourceMenu.sendResourceSlotChange(hoveredSlot.index, event.button() == 1);
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean tryOpenResourceAmountScreen(final ResourceSlot slot) {
        final boolean isFilterSlot = slot.getResource() != null;
        final boolean canModifyAmount = isFilterSlot && slot.canModifyAmount();
        final boolean isNotTryingToRemoveFilter = !minecraft.hasShiftDown();
        final boolean isNotCarryingItem = getMenu().getCarried().isEmpty();
        final boolean canOpen = isFilterSlot
            && canModifyAmount
            && isNotTryingToRemoveFilter
            && isNotCarryingItem;
        if (canOpen) {
            minecraft.setScreen(createResourceAmountScreen(slot));
        }
        return canOpen;
    }

    protected Screen createResourceAmountScreen(final ResourceSlot slot) {
        return new ResourceAmountScreen(this, playerInventory, slot);
    }

    protected boolean canInteractWithResourceSlot(final ResourceSlot resourceSlot,
                                                  final double mouseX,
                                                  final double mouseY) {
        return true;
    }

    @Nullable
    @API(status = API.Status.INTERNAL)
    public PlatformResourceKey getHoveredResource() {
        if (hoveredSlot instanceof ResourceSlot resourceSlot) {
            return resourceSlot.getResource();
        }
        return null;
    }

    @API(status = API.Status.INTERNAL)
    public int getLeftPos() {
        return leftPos;
    }

    @API(status = API.Status.INTERNAL)
    public int getTopPos() {
        return topPos;
    }
}
