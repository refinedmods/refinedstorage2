package com.refinedmods.refinedstorage.common.autocrafting.patterngrid;

import com.refinedmods.refinedstorage.common.api.autocrafting.PatternOutputRenderingScreen;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.widget.CustomButton;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNull;

public class PatternGridScreen extends AbstractGridScreen<PatternGridContainerMenu>
    implements PatternGridContainerMenu.PatternGridListener, PatternOutputRenderingScreen {
    static final int INSET_PADDING = 4;
    static final int INSET_WIDTH = 138;
    static final int INSET_HEIGHT = 71;

    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/pattern_grid.png");
    private static final MutableComponent CREATE_PATTERN = createTranslation("gui", "pattern_grid.create_pattern");
    private static final MutableComponent CLEAR = createTranslation("gui", "pattern_grid.clear");

    private static final int CREATE_PATTERN_BUTTON_SIZE = 16;
    private static final WidgetSprites CREATE_PATTERN_BUTTON_SPRITES = new WidgetSprites(
        createIdentifier("widget/create_pattern"),
        createIdentifier("widget/create_pattern_disabled"),
        createIdentifier("widget/create_pattern_focused"),
        createIdentifier("widget/create_pattern_disabled")
    );
    private static final WidgetSprites CLEAR_BUTTON_SPRITES = new WidgetSprites(
        createIdentifier("widget/clear"),
        createIdentifier("widget/clear_disabled"),
        createIdentifier("widget/clear_focused"),
        createIdentifier("widget/clear_disabled")
    );

    @Nullable
    private Button createPatternButton;
    @Nullable
    private Button clearButton;
    @Nullable
    private PatternGridRenderer renderer;

    private final Map<PatternType, PatternTypeButton> patternTypeButtons = new EnumMap<>(PatternType.class);
    private final Inventory playerInventory;
    private final Map<Pair<PlatformResourceKey, Set<ResourceLocation>>, ProcessingMatrixInputClientTooltipComponent>
        processingMatrixInputTooltipCache = new HashMap<>();
    private final Map<PatternType, PatternGridRenderer> renderers = new EnumMap<>(PatternType.class);

    public PatternGridScreen(final PatternGridContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title, 177);
        this.inventoryLabelY = 153;
        this.imageWidth = 193;
        this.imageHeight = 249;
        this.playerInventory = inventory;
    }

    @Override
    protected void init() {
        super.init();
        initRenderers();
        this.createPatternButton = createCreatePatternButton(leftPos + 152, topPos + imageHeight - bottomHeight + 32);
        addRenderableWidget(createPatternButton);
        addPatternTypeButtons(getMenu().getPatternType());
        this.clearButton = createClearButton();
        addRenderableWidget(clearButton);
        menu.setListener(this);
    }

    private void initRenderers() {
        for (final PatternType type : PatternType.values()) {
            final PatternGridRenderer typeRenderer = type.createRenderer(
                menu,
                leftPos,
                topPos,
                getInsetX(),
                getInsetY()
            );
            if (type == getMenu().getPatternType()) {
                this.renderer = typeRenderer;
            }
            typeRenderer.addWidgets(this::addWidget, this::addRenderableWidget);
            renderers.put(type, typeRenderer);
        }
    }

    private CustomButton createCreatePatternButton(final int x, final int y) {
        final CustomButton button = new CustomButton(
            x,
            y,
            CREATE_PATTERN_BUTTON_SIZE,
            CREATE_PATTERN_BUTTON_SIZE,
            CREATE_PATTERN_BUTTON_SPRITES,
            b -> getMenu().sendCreatePattern(),
            CREATE_PATTERN
        );
        button.setTooltip(Tooltip.create(CREATE_PATTERN));
        button.active = getMenu().canCreatePattern();
        return button;
    }

    private void addPatternTypeButtons(final PatternType currentPatternType) {
        final PatternType[] patternTypes = PatternType.values();
        for (int i = 0; i < patternTypes.length; ++i) {
            final PatternType patternType = patternTypes[i];
            final PatternTypeButton button = new PatternTypeButton(
                leftPos + 172,
                topPos + imageHeight - bottomHeight + 4 + (i * (16 + 3)),
                btn -> getMenu().setPatternType(patternType),
                patternType,
                patternType == currentPatternType
            );
            patternTypeButtons.put(patternType, button);
            addRenderableWidget(button);
        }
    }

    private CustomButton createClearButton() {
        final CustomButton button = new CustomButton(
            requireNonNull(renderer).getClearButtonX(),
            requireNonNull(renderer).getClearButtonY(),
            CLEAR_BUTTON_SIZE,
            CLEAR_BUTTON_SIZE,
            CLEAR_BUTTON_SPRITES,
            b -> getMenu().sendClear(),
            CLEAR
        );
        button.setTooltip(Tooltip.create(CLEAR));
        return button;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (createPatternButton != null) {
            createPatternButton.active = getMenu().canCreatePattern();
        }
        if (renderer != null) {
            renderer.tick();
        }
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (renderer != null) {
            renderer.render(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);
        if (renderer != null) {
            renderer.renderBackground(graphics, partialTicks, mouseX, mouseY);
        }
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        super.renderTooltip(graphics, x, y);
        if (renderer != null) {
            renderer.renderTooltip(font, hoveredSlot, graphics, x, y);
        }
    }

    @Override
    protected void renderResourceSlots(final GuiGraphics graphics) {
        // no op, we render them in the scissor rendering
    }

    @Override
    protected boolean canInteractWithResourceSlot(final ResourceSlot resourceSlot,
                                                  final double mouseX,
                                                  final double mouseY) {
        return renderer != null && renderer.canInteractWithResourceSlot(resourceSlot, mouseX, mouseY);
    }

    @Override
    protected void addResourceSlotTooltips(final ResourceSlot resourceSlot,
                                           final List<ClientTooltipComponent> tooltip) {
        if (resourceSlot instanceof ProcessingMatrixResourceSlot matrixSlot && matrixSlot.isInput()) {
            final Set<ResourceLocation> allowedAlternatives = getMenu().getAllowedAlternatives(
                matrixSlot.getContainerSlot()
            );
            if (matrixSlot.getResource() != null && !allowedAlternatives.isEmpty()) {
                final Pair<PlatformResourceKey, Set<ResourceLocation>> cacheKey = Pair.of(
                    matrixSlot.getResource(),
                    allowedAlternatives
                );
                final ProcessingMatrixInputClientTooltipComponent cached = processingMatrixInputTooltipCache
                    .computeIfAbsent(cacheKey,
                        k -> new ProcessingMatrixInputClientTooltipComponent(k.getFirst(), k.getSecond()));
                tooltip.add(cached);
            }
        }
        super.addResourceSlotTooltips(resourceSlot, tooltip);
    }

    @Override
    protected Screen createResourceAmountScreen(final ResourceSlot slot) {
        if (slot instanceof ProcessingMatrixResourceSlot matrixSlot && matrixSlot.isInput()) {
            return new AlternativesScreen(
                this,
                playerInventory,
                getMenu().getAllowedAlternatives(matrixSlot.getContainerSlot()),
                slot
            );
        }
        return super.createResourceAmountScreen(slot);
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (renderer != null) {
            renderer.renderLabels(graphics, font, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int clickedButton) {
        final boolean clickedInRecipe = renderer != null && renderer.mouseClicked(mouseX, mouseY, clickedButton);
        if (clickedInRecipe) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, clickedButton);
    }

    @Override
    public void mouseMoved(final double mx, final double my) {
        if (renderer != null) {
            renderer.mouseMoved(mx, my);
        }
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseReleased(final double mx, final double my, final int button) {
        return (renderer != null && renderer.mouseReleased(mx, my, button))
            || super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        return (renderer != null && renderer.mouseScrolled(x, y, scrollX, scrollY))
            || super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    public void patternTypeChanged(final PatternType newPatternType) {
        patternTypeButtons.values().forEach(button -> button.setSelected(false));
        patternTypeButtons.get(newPatternType).setSelected(true);
        if (renderer != null) {
            renderer.patternTypeChanged(newPatternType);
        }
        this.renderer = requireNonNull(renderers.get(newPatternType));
        this.renderer.patternTypeChanged(newPatternType);
        if (clearButton != null) {
            clearButton.setPosition(renderer.getClearButtonX(), renderer.getClearButtonY());
        }
    }

    @Override
    public void fuzzyModeChanged(final boolean newFuzzyMode) {
        if (renderer != null) {
            renderer.fuzzyModeChanged(newFuzzyMode);
        }
    }

    private int getInsetX() {
        return leftPos + 8;
    }

    private int getInsetY() {
        return topPos + imageHeight - bottomHeight + 5;
    }

    @Override
    public boolean canDisplayOutput(final ItemStack stack) {
        return getMenu().isPatternInOutput(stack);
    }
}
